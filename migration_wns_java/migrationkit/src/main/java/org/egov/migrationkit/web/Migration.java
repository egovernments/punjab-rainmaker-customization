package org.egov.migrationkit.web;

import org.egov.migrationkit.service.CollectionService;
import org.egov.migrationkit.service.ConnectionService;
import org.egov.migrationkit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.client.model.RequestInfoWrapper;
import io.swagger.client.model.UserInfo;

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
	private CollectionService collectionService;


	@PostMapping("/water/connection")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity migrateWater(@RequestParam String tenantId,
			@RequestBody RequestInfoWrapper waterMigrateRequest,BindingResult result) {

		try {

			UserInfo userInfo = waterMigrateRequest.getRequestInfo().getUserInfo();
			String accessToken = userService.getAccessToken(userInfo.getUserName(), userInfo.getPassword(), userInfo.getTenantId());
			if (accessToken != null) {
				waterMigrateRequest.getRequestInfo().setAuthToken(accessToken);
				service.migrateWtrConnection(tenantId, waterMigrateRequest.getRequestInfo());

			} else {
				return new ResponseEntity(HttpStatus.UNAUTHORIZED);
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
		return new ResponseEntity(HttpStatus.CREATED);
	}

	@PostMapping("/water/connection/v2")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity migrateeWater(@RequestBody RequestInfoWrapper req, @RequestParam String tenantId) {
		try {

			service.migratev2(tenantId, req.getRequestInfo());

		} catch (Exception e) {

			e.printStackTrace();
		}
		return new ResponseEntity(HttpStatus.CREATED);
	}


	
	@PostMapping("/water/connection/collection")
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity migrateWaterCollection(@RequestParam String tenantId,
			@RequestBody RequestInfoWrapper waterMigrateRequest,BindingResult result) {

		try {

			UserInfo userInfo = waterMigrateRequest.getRequestInfo().getUserInfo();
			String accessToken = userService.getAccessToken(userInfo.getUserName(), userInfo.getPassword(), userInfo.getTenantId());
			if (accessToken != null) {
				waterMigrateRequest.getRequestInfo().setAuthToken(accessToken);
				collectionService.migrateWtrCollection(tenantId, waterMigrateRequest.getRequestInfo());

			} else {
				return new ResponseEntity(HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
		return new ResponseEntity(HttpStatus.CREATED);
	}

}
