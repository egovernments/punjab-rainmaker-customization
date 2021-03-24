package org.egov.migrationkit.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.egov.migrationkit.constants.WSConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.client.model.BillResponseV2;
import io.swagger.client.model.BillV2;
import io.swagger.client.model.CollectionPayment;
import io.swagger.client.model.CollectionPaymentDetail;
import io.swagger.client.model.CollectionPaymentRequest;
import io.swagger.client.model.CollectionPaymentResponse;
import io.swagger.client.model.RequestInfo;
import io.swagger.client.model.RequestInfoWrapper;
import lombok.extern.slf4j.Slf4j;

@Service
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

		long startTime = System.currentTimeMillis();
		CollectionPayment payment = null ;

		recordService.initiateCollection(tenantId);

		jdbcTemplate.execute(Sqls.WATER_COLLECTION_MIGRATION_TABLE);

		String digitTenantId = requestInfo.getUserInfo().getTenantId();
		log.info("Water collectin query is: " + Sqls.WATER_COLLECTION_QUERY);
		List<String> queryForList = jdbcTemplate.queryForList(Sqls.WATER_COLLECTION_QUERY, String.class);

		for (String json : queryForList) {

			try {

				 payment = objectMapper.readValue(json, CollectionPayment.class);
				log.info("Initiating  for" + payment.getConsumerCode());
				payment.setTenantId(digitTenantId);
				payment.getPaymentDetails().get(0).setTenantId(digitTenantId);
				payment.getPaymentDetails().get(0).setTotalDue(payment.getTotalDue());
				boolean isPaymentMigrated = recordService.recordWtrCollMigration(payment, tenantId);
				if(isPaymentMigrated) 
					continue;
					
				List<BillV2> bills = null;
				try {
					Long fromDate = WSConstants.TIME_PERIOD_MAP.get(payment.getPeriodFrom());
				    Long toDate = WSConstants.TIME_PERIOD_MAP.get(payment.getPeriodFrom());

					bills = fetchBill(requestInfo, digitTenantId, payment.getBusinessService(),
							payment.getConsumerCode(),payment.getPaymentDetails().get(0).getReceiptNumber(),
							fromDate, toDate);

				} catch (Exception exception) {
					log.error("Exception occurred while fetching the bills with business service:"
							+ payment.getBusinessService() + " and consumer code: " + payment.getConsumerCode());
					recordService.recordError("Wtrcollection", tenantId,
							"Error while fetching bill:" + exception.getMessage(), payment.getPaymentDetails().get(0).getReceiptNumber());
				}
				if (bills != null && !bills.isEmpty() && !payment.getPaymentDetails().isEmpty()) {
					List<CollectionPaymentDetail> detailList = new ArrayList<CollectionPaymentDetail>();
					detailList.add(payment.getPaymentDetails().get(0));
					payment.setPaymentDetails(detailList);
					payment.getPaymentDetails().get(0).setBillId(bills.get(0).getId());
					CollectionPaymentRequest paymentRequest = CollectionPaymentRequest.builder()
							.requestInfo(requestInfo).payment(payment).build();

					String uri = collectionserviceHost + paymentCreatePath;
					Optional<Object> response = Optional
							.ofNullable(restTemplate.postForObject(uri, paymentRequest, JsonNode.class));
					if (response.isPresent()) {
						try {
							CollectionPaymentResponse paymentResponse = objectMapper.convertValue(response.get(),
									CollectionPaymentResponse.class);
							if (!CollectionUtils.isEmpty(paymentResponse.getPayments())) {
								log.info("Collection migration done for consumer code: " + payment.getConsumerCode());
								recordService.updateWtrCollMigration(payment, tenantId, paymentResponse.getPayments()
										.get(0).getPaymentDetails().get(0).getReceiptNumber());
							} else {
								log.error("Failed to register this payment at collection-service for consumer code: "
										+ payment.getConsumerCode());
								recordService.recordError("Wtrcollection", tenantId,
										"Failed to register this payment at collection-service for consumer code: "
												+ payment.getConsumerCode(),
										payment.getId());
							}
						} catch (Exception e) {
							log.error("Failed to register this payment for consumer code: " + payment.getConsumerCode(),
									e);
							recordService.recordError("Wtrcollection", tenantId, e.getMessage(), payment.getPaymentDetails().get(0).getReceiptNumber());

						}

					} else {
						log.error("Failed to register this payment at collection-service");
						recordService.recordError("Wtrcollection", tenantId,
								"Failed to register this payment at collection-service", payment.getPaymentDetails().get(0).getReceiptNumber());
					}

					log.debug("waterResponse" + response);
				}

			} catch (Exception e) {
				log.error(e.getMessage());
				recordService.recordError("Wtrcollection", tenantId,
						"Failed to register this payment at collection-service", payment.getPaymentDetails().get(0).getReceiptNumber());
		

			}

		}
		long duration = System.currentTimeMillis()-startTime;
		
		log.info("Water Collection Migration completed for " + tenantId + " took " + duration/1000 + " Secs to run");
	}

	public List<BillV2> fetchBill(RequestInfo requestInfo, String tenantId, String businessService,
			String consumerCode, String erpReceiptNumber, Long periodFrom, Long periodTo) {
		List<BillV2> bills = new ArrayList<>();
		try {

			String url = commonService.getFetchBillURL(tenantId, consumerCode, businessService, periodFrom, periodTo).toString();
			RequestInfoWrapper request = RequestInfoWrapper.builder().requestInfo(requestInfo).build();

			String response = restTemplate.postForObject(url, request, String.class);
			log.debug("Bill Request URL: " + url + "Bill RequestInfo: " + request + "Bill Response: " + response);
			BillResponseV2 waterResponse = objectMapper.readValue(response, BillResponseV2.class);

			return waterResponse.getBill();

		} catch (Exception ex) {
			String module=null;
			if(businessService.equalsIgnoreCase("WS")) {
				module="Wtrcollection";
			}else {
				module="Swcollection";
			}
			recordService.recordError(module, tenantId, ex.getMessage(), erpReceiptNumber);
			log.error("Fetch Bill Error", ex);
		}
		return bills;
	}

	public void migrateSwgCollection(String tenantId, RequestInfo requestInfo) {
		long startTime = System.currentTimeMillis();

		jdbcTemplate.execute("set search_path to " + tenantId);

		jdbcTemplate.execute(Sqls.SEWERAGE_COLLECTION_MIGRATION_TABLE);

		String digitTenantId = requestInfo.getUserInfo().getTenantId();

		List<String> queryForList = jdbcTemplate.queryForList(Sqls.SEWERAGE_COLLECTION_QUERY, String.class);

		for (String json : queryForList) {

			try {
				CollectionPayment payment = objectMapper.readValue(json, CollectionPayment.class);
				payment.setTenantId(digitTenantId);
				payment.getPaymentDetails().get(0).setTenantId(digitTenantId);
				payment.getPaymentDetails().get(0).setTotalDue(payment.getTotalDue());
				boolean isPaymentMigrated = recordService.recordSwgCollMigration(payment, tenantId);
				
				if(isPaymentMigrated)
					continue;
				
				List<BillV2> bills = null;
				try {

					Long fromDate = WSConstants.TIME_PERIOD_MAP.get(payment.getPeriodFrom());
				    Long toDate = WSConstants.TIME_PERIOD_MAP.get(payment.getPeriodFrom());

					bills = fetchBill(requestInfo, digitTenantId, payment.getBusinessService(),
							payment.getConsumerCode(),payment.getPaymentDetails().get(0).getReceiptNumber(),
							fromDate, toDate);

				} catch (Exception exception) {
					log.error("Exception occurred while fetching the bills with business service:"
							+ payment.getBusinessService() + " and consumer code: " + payment.getConsumerCode());
					recordService.recordError("Swcollection", tenantId,
							"Error while fetching bill:" + exception.toString(), payment.getId());
				}
				if (bills != null && !bills.isEmpty() && !payment.getPaymentDetails().isEmpty()) {
					List<CollectionPaymentDetail> detailList = new ArrayList<CollectionPaymentDetail>();
					detailList.add(payment.getPaymentDetails().get(0));
					payment.setPaymentDetails(detailList);
					payment.getPaymentDetails().get(0).setBillId(bills.get(0).getId());
					CollectionPaymentRequest paymentRequest = CollectionPaymentRequest.builder()
							.requestInfo(requestInfo).payment(payment).build();

					String uri = collectionserviceHost + paymentCreatePath;
					Optional<Object> response = Optional
							.ofNullable(restTemplate.postForObject(uri, paymentRequest, JsonNode.class));
					if (response.isPresent()) {
						try {
							CollectionPaymentResponse paymentResponse = objectMapper.convertValue(response.get(),
									CollectionPaymentResponse.class);
							if (!CollectionUtils.isEmpty(paymentResponse.getPayments())) {
								log.info("Collection migration done for consumer code: " + payment.getConsumerCode());
								recordService.updateSwgCollMigration(payment, tenantId, paymentResponse.getPayments()
										.get(0).getPaymentDetails().get(0).getReceiptNumber());
							} else {
								log.error("Failed to register this payment at collection-service for consumer code: "
										+ payment.getConsumerCode());
								recordService.recordError("Swcollection", tenantId,
										"Failed to register this payment at collection-service for consumer code: "
												+ payment.getConsumerCode(),
										payment.getId());
							}
						} catch (Exception e) {
							log.error("Failed to register this payment for consumer code: " + payment.getConsumerCode(),
									e);
							recordService.recordError("Swcollection", tenantId, e.getMessage(), payment.getId());

						}

					} else {
						log.error("Failed to register this payment at collection-service");
						recordService.recordError("Swcollection", tenantId,
								"Failed to register this payment at collection-service", payment.getId());
					}

					log.debug("sewerageResponse" + response);
				}

			} catch (Exception e) {
				log.error(e.getMessage());
			}

		}
		long duration = System.currentTimeMillis()-startTime;
		
		log.info("Sewerage Collection Migration completed for " + tenantId + " took " + duration/1000 + " Secs to run");
	}

}
