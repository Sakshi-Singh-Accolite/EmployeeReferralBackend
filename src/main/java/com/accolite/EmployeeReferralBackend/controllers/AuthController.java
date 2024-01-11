package com.accolite.EmployeeReferralBackend.controllers;

import com.accolite.EmployeeReferralBackend.models.GoogleTokenPayload;
import com.accolite.EmployeeReferralBackend.models.Role;
import com.accolite.EmployeeReferralBackend.models.User;
import com.accolite.EmployeeReferralBackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private String googleTokenInfoUrl = "https://www.googleapis.com/oauth2/v3/tokeninfo";

    @PostMapping("/LoginWithGoogle")
    public ResponseEntity<Map<String, Object>> loginWithGoogle(@RequestBody String googleToken) {
        try {
            // Validate Google token
            String tokenPayload = validateGoogleToken(googleToken);

            if (tokenPayload != null) {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("status", "success");
                responseMap.put("tokenPayload", tokenPayload);
                return ResponseEntity.ok(responseMap);
            } else {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("status", "error");
                errorMap.put("message", "Invalid Google token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMap);
            }
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", "error");
            errorMap.put("message", "An error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMap);
        }
    }

    private String validateGoogleToken(String googleToken){
        RestTemplate restTemplate = new RestTemplate();
      //  System.out.println(googleToken);

        String accessTokenValue = googleToken.substring(1);

        String tokenInfoUrl = googleTokenInfoUrl + "?id_token=" + accessTokenValue;
        ResponseEntity<GoogleTokenPayload> response = restTemplate.getForEntity(tokenInfoUrl, GoogleTokenPayload.class);

        // System.out.println(response.getBody().getEmail()); To get the email

        if(response.getBody()!=null)
        {
            String email = response.getBody().getEmail();
            User user = userRepository.findByEmail(email)
                    .orElse(null);

            if(user==null){
                var userEntry = User.builder().email(response.getBody().getEmail()).role(Role.EMPLOYEE).build();
                userRepository.save(userEntry);
            }

        }else{
            return null;
        }

        if (response.getStatusCode() == HttpStatus.OK) {
            return accessTokenValue;
        } else {
            return null;
        }
    }
}
