package com.anupa.orderservice;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TokenService
{
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);

    @Autowired
    private ApplicationContext ctx;

    public String validateToken(String token)
    {

        log.info("TokenService.validateToken() called with token: " + token);

        WebClient authValidateWebClient = ctx.getBean("authValidateWebClient", WebClient.class);
        log.info("Calling auth-service to validate token: " + token);
        // forward a request to the auth service for validation
        String authResponse = authValidateWebClient.get()
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(String.class)// bodyToFlux
                .block(); // Thread is Blocked until the response is received | SYNC
        // THREAD will pause at this point till a response is received

        log.info("Response from auth-service: " + authResponse);
        return authResponse;
    }

    String getAuthCookieValue(HttpServletRequest request)
    {
        List<Cookie> cookies = new ArrayList<>();

        if(!(request.getCookies() == null))
        {
            cookies = List.of(request.getCookies());
        }

        Optional<Cookie> authcookie =  cookies.stream().filter(cookie -> cookie.getName().equals("AUTH-TOKEN")).findFirst();

        return authcookie.map(Cookie::getValue).orElse(null);
    }


}
