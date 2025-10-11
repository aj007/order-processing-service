package com.egov.profileservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class ProfileServiceApplication
{

    public static void main(String[] args)
    {
        SpringApplication.run(ProfileServiceApplication.class, args);
    }

}
