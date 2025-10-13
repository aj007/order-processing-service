package com.anupa.orderservice;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/")
public class MainRestController
{
    private static final Logger logger = LoggerFactory.getLogger(MainRestController.class);
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    TokenService tokenService;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    private View error;

    @PostMapping("order/create") // Secured Endpoint need JWT Token in Header
    ResponseEntity<?> createOrder(@RequestBody Order order,
                                   HttpServletRequest request,
                                   HttpServletResponse httpServletResponse)
    {
        logger.info("Order received");
        // TOKEN VALIDATION IS REQUIRED
        Optional<String> token =  Optional.ofNullable(tokenService.getAuthCookieValue(request));
        logger.info("Token extracted for order {}: ",token);
        Optional<String> principal =  Optional.ofNullable(tokenService.validateToken(token.orElse(null)));
        logger.info("Principal extracted for order {}: ",principal);

        if(principal.isPresent())
        {
            String orderCookieValue = getAuthCookieValue(request, "order-1");
            // FRESH OR FOLLOW UP REQUEST DECIPHERING LOGIC CAN BE PLACED HERE
            if(orderCookieValue == null)
            {
                logger.info("Fresh order received");
                logger.info("Proceeding with order processing {}: ",principal.get());
                order.setStatus("CREATED");
                Order orderSaved =  orderRepository.save(order);
                logger.info("Saved order {}: ",order.getId());
                // SET A COOKIE IN THE RESPONSE
                Cookie orderStage = new Cookie("order-1", io.opentelemetry.api.trace.Span.current().getSpanContext().getTraceId());
                orderStage.setMaxAge(60);

                redisTemplate.opsForValue().set(orderStage.getValue(), "Order received for "+orderSaved.getId()+", checking inventory ");
                //forward request to payment-service for payment [ service-fee ] creation
                WebClient inventoryCheckWebClient = (WebClient) applicationContext.getBean("inventoryCheckWebClient");
                inventoryCheckWebClient.get().
                        uri("/"+orderSaved.getProductId()+"/availability").
                        header("Authorization", token.get()).
                        retrieve().
                        bodyToMono(String.class). // ASYNC HANDLER LOGIC STARTS FROM THE NEXT LINE - WILL BE EXECUTED IN A SEPARATE THREAD
                        subscribe( response -> {

                    logger.info("Response from inventory-service for order {}: is {} ",orderSaved.getId(),response);
                    redisTemplate.opsForValue().set(orderStage.getValue(),"Inventory available for order "+orderSaved.getId()+" with PAYMENT_ID_"+response+" TRANSACTION COMPLETE");
                    // CACHE UPDATION TAKES PLACE HERE

                    redisTemplate.opsForValue().set(orderStage.getValue(), "Order received "+orderSaved.getId()+" processing payment");

                    //forward request to payment-service for payment [ service-fee ] creation
                    WebClient paymentCreateWebClient = (WebClient) applicationContext.getBean("paymentCreateWebClient");
                    paymentCreateWebClient.post().
                            uri("/"+orderSaved.getId()+"/"+(orderSaved.getTotalAmount()/10)).
                            header("Authorization", token.get()).
                            retrieve().
                            bodyToMono(String.class). // ASYNC HANDLER LOGIC STARTS FROM THE NEXT LINE - WILL BE EXECUTED IN A SEPARATE THREAD
                            subscribe( responsePayment -> {

                        logger.info("Response from payment-service for order {}: is {} ",orderSaved.getId(),responsePayment);
                        redisTemplate.opsForValue().set(orderStage.getValue(),"Payment completed for order "+orderSaved.getId()+" with PAYMENT_ID_"+responsePayment+" TRANSACTION COMPLETE");
                        // CACHE UPDATION TAKES PLACE HERE

                    }, error -> {
                        logger.error("Error in payment-service for project {}: ",orderSaved.getId(),error);
                    }); // ASYNC HANDLER LOGIC ENDS HERE

                }, error -> {
                    logger.error("Error in inventory-service for project {}: ",orderSaved.getId(),error);
                }); // ASYNC HANDLER LOGIC ENDS HERE

                httpServletResponse.addCookie(orderStage);
                return ResponseEntity.ok("Order processing triggered with id: "+orderSaved.getId());
            }
            else
            {
                // FOLLOW UP REQUEST LOGIC CAN BE PLACED HERE
                logger.info("Follow up request for order received");

                String cacheValue = redisTemplate.opsForValue().get(orderCookieValue);

                assert cacheValue != null;
                if(cacheValue.contains(" processing payment"))
                {
                    return ResponseEntity.ok("REQUEST IS STILL UNDER PROCESS");
                }
                else
                {
                    return ResponseEntity.ok(cacheValue);
                }


            }
        }
        else
        {
            return ResponseEntity.status(401).body("BAD CREDENTIALS");
        }


    }

    String getAuthCookieValue(HttpServletRequest request, String cookiename)
    {
        List<Cookie> cookies = new ArrayList<>();

        if(!(request.getCookies() == null))
        {
            cookies = List.of(request.getCookies());
        }

        Optional<Cookie> authcookie =  cookies.stream().filter(cookie -> cookie.getName().equals(cookiename)).findFirst();

        return authcookie.map(Cookie::getValue).orElse(null);
    }

}
