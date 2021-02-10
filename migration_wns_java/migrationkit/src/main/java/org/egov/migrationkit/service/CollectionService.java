package org.egov.migrationkit.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.client.model.BillResponseV2;
import io.swagger.client.model.BillV2;
import io.swagger.client.model.CollectionPayment;
import io.swagger.client.model.CollectionPaymentRequest;
import io.swagger.client.model.CollectionPaymentResponse;
import io.swagger.client.model.RequestInfo;
import io.swagger.client.model.RequestInfoWrapper;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class CollectionService {
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Value("${egov.collectionservice.host}")
	private String collectionserviceHost = null;

	@Value("${egov.collectionservice.payment.create.path}")
	private String paymentCreatePath = null;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RecordService recordService;

	@Autowired
	private CommonService commonService;


	public void migrateWtrCollection(String tenantId, RequestInfo requestInfo) {

		jdbcTemplate.execute("set search_path to " + tenantId);

		jdbcTemplate.execute(Sqls.WATER_COLLECTION_TABLE);

		String digitTenantId = requestInfo.getUserInfo().getTenantId();

		List<String> queryForList = jdbcTemplate.queryForList(Sqls.WATER_COLLECTION_QUERY, String.class);

		for (String json : queryForList) {

			try {
				CollectionPayment payment = objectMapper.readValue(json, CollectionPayment.class);
				payment.setTenantId(digitTenantId);
				payment.getPaymentDetails().get(0).setTenantId(digitTenantId);
				recordService.recordWtrCollMigration(payment);
				List<BillV2> bills = null;
				try {

					bills = fetchBill(requestInfo, digitTenantId, payment.getBusinessService(), payment.getConsumerCode());

				}catch(Exception exception) {
					log.error("Exception occurred while fetching the bills with business service:"+payment.getBusinessService()+ " and consumer code: " + payment.getConsumerCode());

				}
				if(bills != null && !bills.isEmpty() && !payment.getPaymentDetails().isEmpty()) {
					payment.getPaymentDetails().get(0).setBillId(bills.get(0).getId());
					CollectionPaymentRequest paymentRequest = CollectionPaymentRequest.builder()
							.requestInfo(requestInfo).payment(payment).build();

					String uri = collectionserviceHost + paymentCreatePath;
					Optional<Object> response =  Optional.ofNullable(restTemplate.postForObject(uri, paymentRequest, JsonNode.class));
					if(response.isPresent()) {
						try {
							CollectionPaymentResponse paymentResponse = objectMapper.convertValue(response.get(), CollectionPaymentResponse.class);
							if(!CollectionUtils.isEmpty(paymentResponse.getPayments()))
								log.info("Collection migration done for consumer code: "+ payment.getConsumerCode());
							else
								log.error("Failed to register this payment at collection-service for consumer code: "+ payment.getConsumerCode());						
						}catch(Exception e) {
							log.error("Failed to register this payment for consumer code: "+ payment.getConsumerCode(), e);						

						}

					}else {
						log.error("Failed to register this payment at collection-service");
					}

					recordService.updateWtrCollMigration(payment);
					log.info("waterResponse" + response); 
				}



			} catch (Exception e) {
				log.error(e.getMessage()); 
			}

		}
		log.info("Collection Migration is completed for "+tenantId);
	}

	public List<BillV2> fetchBill(RequestInfo requestInfo, String tenantId, String businessService, String consumerCode) {
		List<BillV2> bills = new ArrayList<>();	
		try {

			String url = commonService.getFetchBillURL(tenantId, consumerCode, businessService).toString();
			RequestInfoWrapper request = RequestInfoWrapper.builder().requestInfo(requestInfo).build();

			String response = restTemplate.postForObject(url , request, String.class);
			log.info("Bill Request URL: " + url + "Bill RequestInfo: " + request + "Bill Response: " + response);
			BillResponseV2 waterResponse=	objectMapper.readValue(response, BillResponseV2.class);

			return waterResponse.getBill();

		} catch (Exception ex) {
			log.error("Fetch Bill Error", ex);
		}
		return bills;
	}
	
	public void migrateSwgCollection(String tenantId, RequestInfo requestInfo) {

		jdbcTemplate.execute("set search_path to " + tenantId);

		jdbcTemplate.execute(Sqls.SEWERAGE_COLLECTION_TABLE);

		String digitTenantId = requestInfo.getUserInfo().getTenantId();

		List<String> queryForList = jdbcTemplate.queryForList(Sqls.SEWERAGE_COLLECTION_QUERY, String.class);

		for (String json : queryForList) {

			try {
				CollectionPayment payment = objectMapper.readValue(json, CollectionPayment.class);
				payment.setTenantId(digitTenantId);
				payment.getPaymentDetails().get(0).setTenantId(digitTenantId);
				recordService.recordSwgCollMigration(payment);
				List<BillV2> bills = null;
				try {

					bills = fetchBill(requestInfo, digitTenantId, payment.getBusinessService(), payment.getConsumerCode());

				}catch(Exception exception) {
					log.error("Exception occurred while fetching the bills with business service:"+payment.getBusinessService()+ " and consumer code: " + payment.getConsumerCode());

				}
				if(bills != null && !bills.isEmpty() && !payment.getPaymentDetails().isEmpty()) {
					payment.getPaymentDetails().get(0).setBillId(bills.get(0).getId());
					CollectionPaymentRequest paymentRequest = CollectionPaymentRequest.builder()
							.requestInfo(requestInfo).payment(payment).build();

					String uri = collectionserviceHost + paymentCreatePath;
					Optional<Object> response =  Optional.ofNullable(restTemplate.postForObject(uri, paymentRequest, JsonNode.class));
					if(response.isPresent()) {
						try {
							CollectionPaymentResponse paymentResponse = objectMapper.convertValue(response.get(), CollectionPaymentResponse.class);
							if(!CollectionUtils.isEmpty(paymentResponse.getPayments()))
								log.info("Collection migration done for consumer code: "+ payment.getConsumerCode());
							else
								log.error("Failed to register this payment at collection-service for consumer code: "+ payment.getConsumerCode());						
						}catch(Exception e) {
							log.error("Failed to register this payment for consumer code: "+ payment.getConsumerCode(), e);						

						}

					}else {
						log.error("Failed to register this payment at collection-service");
					}

					recordService.updateSwgCollMigration(payment);
					log.info("sewerageResponse" + response); 
				}



			} catch (Exception e) {
				log.error(e.getMessage()); 
			}

		}
		log.info("Collection Migration is completed for "+tenantId);
	}

}
