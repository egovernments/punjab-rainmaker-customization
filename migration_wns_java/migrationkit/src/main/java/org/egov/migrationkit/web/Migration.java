package org.egov.migrationkit.web;

import java.util.Map;

import org.egov.migrationkit.service.ConnectionService;
import org.egov.migrationkit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.client.model.UserInfo;
import io.swagger.client.model.WaterMigrateRequest;

@RestController
public class Migration {

	@Value("${egov.services.hosturl}")
	private String host = null;

	@Value("${egov.services.water.url}")
	private String waterUrl = null;

	@Autowired
	private ConnectionService service;
	
	@Autowired
	private UserService userService;
	
	 @Autowired
	 private ObjectMapper objectMapper;

	@PostMapping("/water/connection")
	public ResponseEntity migrateWater(@RequestParam String tenantId, @RequestBody WaterMigrateRequest waterMigrateRequest) {
		try {
			
			UserInfo userInfo = waterMigrateRequest.getRequestInfo().getUserInfo();
			String accessToken = getAccessToken(userInfo.getUserName(), userInfo.getPassword(), userInfo.getTenantId());
			if(accessToken != null) {
				waterMigrateRequest.getRequestInfo().setAuthToken(accessToken);
				service.migrate(tenantId, waterMigrateRequest.getRequestInfo());
			
			} else {
				return new ResponseEntity(HttpStatus.UNAUTHORIZED);
			}

		} catch (Exception e) { 

			e.printStackTrace();
		}
		return new ResponseEntity(HttpStatus.CREATED);
	}
	
	public String getAccessToken(String superUsername,String superUserPassword,String tenantId) {
		
		String access_token=null;
		Object record = userService.getAccess(superUsername, superUserPassword, tenantId);
		Map tokenObject = objectMapper.convertValue(record, Map.class);

		if(tokenObject.containsKey("access_token")) {
			access_token = (String) tokenObject.get("access_token");
			System.out.println("Access token: "+ access_token);
		}
		
		return access_token;
		
	}
	

}
