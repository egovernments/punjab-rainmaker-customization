package org.egov.migrationkit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class CollectionService {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@JsonProperty("host")
	@Value("${egov.services.hosturl}")
	private String host = null;

	@Value("${egov.services.water.url}")
	private String waterUrl = null;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PropertyService propertyService;
	@Autowired
	private RecordService recordService;
	
	@Autowired
	private DemandService demandService;
}
