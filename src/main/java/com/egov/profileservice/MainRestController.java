package com.egov.profileservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
public class MainRestController
{
    private static final Logger logger = LoggerFactory.getLogger(MainRestController.class);

    @Autowired
    ProfileRepository profileRepository;

    @Autowired
    TokenService tokenService;

    @PostMapping("update/user/profile")
    public ResponseEntity<?> updateUserDetails(@RequestBody Profile profile,
                                               @RequestHeader ("Authorization") String token)
    {
        logger.info("Request received to update user profile: " + profile);
        // Authenticate the Request
        // 1. Check weather the phone number exists in the database ?? but not from the payload, so from where ??? Authentication
        // 2. If step 1 is successful, then check the password
        // 3. If step 2 is successful, then compare the phone from the auth data and the payload | Authorization
        // 4. If step 3 is successful, then update the profile
        Principal principal =  tokenService.validateToken(token);
        if(principal.getState().equals("VALID"))
        {
            logger.info("Token validated successfully");
            // Token is valid, proceed with the update

            if(profile.getPhone().equals(principal.getUsername())) // AUTHORIZATION OF REQUEST HAPPENS HERE
            {
                logger.info("Phone number matches with the token");
                profileRepository.save(profile);
                logger.info("Profile updated successfully in the database: " + profile);
                return  ResponseEntity.ok("Profile Updated Successfully");
            }
            else
            {
                logger.info("Phone number does not match with the token");
                return ResponseEntity.status(401).body("Unauthorized: Phone number does not match with the token");
            }

        }
        else
        {
            logger.info("Token not valid");
            return ResponseEntity.status(401).body("Unauthorized: Invalid Token");
        }


    }
}
