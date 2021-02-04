package org.egov.migrationkit.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.client.model.User;

@Service
public class UserService {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Value("${egov.user.host}")
	private String userHost = null;

	@Value("${egov.user.token.url}")
	private String userTokenEndpoint = null;
	
	@Autowired
	private ObjectMapper objectMapper;


	public String getAccessToken(String superUsername, String superUserPassword, String tenantId) {

		String access_token = null;
		Object record = getAccess(superUsername, superUserPassword, tenantId);
		Map tokenObject = objectMapper.convertValue(record, Map.class);

		if (tokenObject.containsKey("access_token")) {
			access_token = (String) tokenObject.get("access_token");
			System.out.println("Access token: " + access_token);
		}

		return access_token;

	}
	
    public Object getAccess(String userName, String password, String tenantId) {
       System.out.println("Fetch access token for register with login flow");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic ZWdvdi11c2VyLWNsaWVudDplZ292LXVzZXItc2VjcmV0");
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("username", userName);
            map.add("password", password);
            map.add("grant_type", "password");
            map.add("scope", "read");
            map.add("tenantId", tenantId);
            map.add("userType", UserType.EMPLOYEE.name());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map,
                    headers);
            return restTemplate.postForEntity(userHost + userTokenEndpoint, request, Map.class).getBody();

        } catch (Exception e) {
            System.out.println("Error occurred while logging-in via register flow" + e);
            throw e;
        }
    }
    
    public enum UserType {
        CITIZEN, EMPLOYEE, SYSTEM, BUSINESS;

        public static UserType fromValue(String text) {
            for (UserType userType : UserType.values()) {
                if (String.valueOf(userType).equalsIgnoreCase(text)) {
                    return userType;
                }
            }
            return null;
        }
    }

}
