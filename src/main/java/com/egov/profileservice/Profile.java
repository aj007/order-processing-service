package com.egov.profileservice;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "profiles")
@Getter
@Setter
public class Profile
{
    @Id
    String phone;
    String firstname;
    String lastname;
    String email;

    @Override
    public String toString() {
        return "Profile{" +
                "phone='" + phone + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
