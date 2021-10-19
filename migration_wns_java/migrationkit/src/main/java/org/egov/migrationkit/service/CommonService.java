package org.egov.migrationkit.service;

import org.egov.migrationkit.constants.WSConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import io.swagger.client.model.DemandCriteria;
import io.swagger.client.model.OwnerInfo;
import io.swagger.client.model.PropertyResponse;
import io.swagger.client.model.RequestInfo;
import io.swagger.client.model.RequestInfoWrapper;
import io.swagger.client.model.SewerageConnectionResponse;
import io.swagger.client.model.WaterConnectionResponse;

@Component
public class CommonService {
	
	@Value("${egov.bill.search.endpoint}")
	private String searchBillEndPoint;

	@Value("${egov.bill.fetch.endpoint}")
	private String fetchBillEndPoint;
	
	@Value("${egov.billingservice.host}")
	private String billingServiceHost;
	
	@Value("${egov.demand.search.endpoint}")
	private String searchDemandEndPoint;
	
	@Value("${egov.water.search.endpoint}")
	private String searchWaterEndPoint;
	
	@Value("${egov.sewerage.search.endpoint}")
	private String searchSewerageEndPoint;
	
	@Value("${egov.services.ptsearch.url}")
	private String searchPropertyEndPoint;
	
	@Value("${egov.services.hosturl}")
	private String host;
	
	@Autowired
	private RestTemplate restTemplate;
	
	
	/**
	 * 
	 * @param tenantId
	 *            Tenant Id
	 * @param consumerCode
	 *            Consumer Code
	 * @return uri of fetch bill
	 */
	public StringBuilder getFetchBillURL(String tenantId, String consumerCode, String businessService, Long periodFrom, Long periodTo) {

		return new StringBuilder().append(billingServiceHost)
				.append(fetchBillEndPoint).append(WSConstants.URL_PARAMS_SEPARATER)
				.append(WSConstants.TENANT_ID_FIELD_FOR_SEARCH_URL).append(tenantId)
				.append(WSConstants.SEPARATER).append(WSConstants.CONSUMER_CODE_SEARCH_FIELD_NAME)
				.append(consumerCode).append(WSConstants.SEPARATER)
				.append(WSConstants.BUSINESSSERVICE_FIELD_FOR_SEARCH_URL)
				.append(businessService)
				.append(WSConstants.SEPARATER)
				.append(WSConstants.PERIOD_FROM_FIELD_NAME)
				.append(periodFrom)
				.append(WSConstants.SEPARATER)
				.append(WSConstants.PERIOD_TO_FIELD_NAME)
				.append(periodTo);
	}
	
	/**
	 * 
	 * @param tenantId
	 *            Tenant Id
	 * @param consumerCode
	 *            Consumer Code
	 * @return uri of fetch bill
	 */
	public StringBuilder getSearchBillURL(String tenantId, String consumerCode, String businessService, String billNumber) {

		return new StringBuilder().append(billingServiceHost)
				.append(searchBillEndPoint).append(WSConstants.URL_PARAMS_SEPARATER)
				.append(WSConstants.TENANT_ID_FIELD_FOR_SEARCH_URL).append(tenantId)
				.append(WSConstants.SEPARATER).append(WSConstants.CONSUMER_CODE_SEARCH_FIELD_NAME)
				.append(consumerCode).append(WSConstants.SEPARATER)
				.append(WSConstants.SERVICE_FIELD_FOR_SEARCH_URL)
				.append(businessService)
				.append(WSConstants.SEPARATER)
				.append(WSConstants.BILL_NUMBER_FIELD_NAME)
				.append(billNumber);
	}
	
	/**
	 * 
	 * @param tenantId
	 *            Tenant Id
	 * @param consumerCode
	 *            Consumer Code
	 * @return uri of fetch bill
	 */
	public StringBuilder getDemandSearchURL(DemandCriteria demandCriteria) {

		return new StringBuilder().append(billingServiceHost)
				.append(searchDemandEndPoint).append(WSConstants.URL_PARAMS_SEPARATER)
				.append(WSConstants.TENANT_ID_FIELD_FOR_SEARCH_URL).append(demandCriteria.getTenantId())
				.append(WSConstants.SEPARATER).append(WSConstants.CONSUMER_CODE_SEARCH_FIELD_NAME)
				.append(demandCriteria.getConsumerCode()).append(WSConstants.SEPARATER)
				.append(WSConstants.BUSINESSSERVICE_FIELD_FOR_SEARCH_URL)
				.append(demandCriteria.getBusinessService()).append(WSConstants.SEPARATER)
				.append(WSConstants.PERIOD_FROM_FIELD_NAME)
				.append(demandCriteria.getPeriodFrom()).append(WSConstants.SEPARATER)
				.append(WSConstants.PERIOD_TO_FIELD_NAME)
				.append(demandCriteria.getPeriodTo());
	}
	
	public OwnerInfo searchConnection(RequestInfo requestInfo, String connectionNo,String tenantId, String type) {
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		String propertyId=null;
		if(type.equalsIgnoreCase("water")) {
			String searchURL = new StringBuilder().append(searchWaterEndPoint)
			                   .append(WSConstants.URL_PARAMS_SEPARATER)
			                   .append(WSConstants.TENANT_ID_FIELD_FOR_SEARCH_URL)
			                   .append(tenantId).append(WSConstants.SEPARATER)
			                   .append(WSConstants.SEARCHTYPE_URL)
			                   .append(WSConstants.SEPARATER)
			                   .append(WSConstants.CONNECTION_NUMBER_URL)
			                   .append(connectionNo).toString();
			
			WaterConnectionResponse response = restTemplate.postForObject(host + searchURL, requestInfoWrapper,
					WaterConnectionResponse	.class);
			if(response != null && response.getWaterConnection() != null && !response.getWaterConnection().isEmpty()) {
				propertyId = response.getWaterConnection().get(0).getPropertyId();
				return searchProperty(requestInfoWrapper, propertyId, tenantId);
				
			}
		} else if(type.equalsIgnoreCase("sewerage")) {
			
			String searchURL = new StringBuilder().append(searchSewerageEndPoint)
	                   .append(WSConstants.URL_PARAMS_SEPARATER)
	                   .append(WSConstants.TENANT_ID_FIELD_FOR_SEARCH_URL)
	                   .append(tenantId).append(WSConstants.SEPARATER)
	                   .append(WSConstants.SEARCHTYPE_URL)
	                   .append(WSConstants.SEPARATER)
	                   .append(WSConstants.CONNECTION_NUMBER_URL)
	                   .append(connectionNo).toString();
	
			SewerageConnectionResponse response = restTemplate.postForObject(host + searchURL, requestInfoWrapper,
					SewerageConnectionResponse.class);
			if(response != null && response.getSewerageConnections() != null && !response.getSewerageConnections().isEmpty()) {
				propertyId = response.getSewerageConnections().get(0).getPropertyId();
				return searchProperty(requestInfoWrapper, propertyId, tenantId);
			}
			
		}
		
		return null;
	}
	
	
	public OwnerInfo searchProperty(RequestInfoWrapper requestInfoWrapper, String uuid,String tenantId) {
		    OwnerInfo ownerInfo = null;
			String searchURL = new StringBuilder().append(searchPropertyEndPoint)
			                   .append(WSConstants.URL_PARAMS_SEPARATER)
			                   .append(WSConstants.TENANT_ID_FIELD_FOR_SEARCH_URL)
			                   .append(tenantId)
			                   .append(WSConstants.SEPARATER)
			                   .append(WSConstants.PROPERTY_IDS_URL)
			                   .append(uuid).toString();
			
			PropertyResponse response = restTemplate.postForObject(host +"/"+ searchURL, requestInfoWrapper,
					PropertyResponse.class);
			if(response != null && response.getProperties() != null && !response.getProperties().isEmpty()) {
				ownerInfo = response.getProperties().get(0).getOwners().get(0);
			}
			
			return ownerInfo;
	}
	

}
