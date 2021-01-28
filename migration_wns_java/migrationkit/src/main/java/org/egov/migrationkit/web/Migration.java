package org.egov.migrationkit.web;

import org.egov.migrationkit.service.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class Migration {

	@Value("${egov.services.hosturl}")
	private String host = null;

	@Value("${egov.services.water.url}")
	private String waterUrl = null;

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private ConnectionService service;

	@PostMapping("/water/connection")
	@ResponseStatus(HttpStatus.CREATED)
	public String migrateeWater(@RequestBody String req,@RequestParam String tenantId) {
		try {
			
			
			
			service.migrate(tenantId, req);

		} catch (Exception e) { 

			e.printStackTrace();
		}
		return "sunam";
	}

}
