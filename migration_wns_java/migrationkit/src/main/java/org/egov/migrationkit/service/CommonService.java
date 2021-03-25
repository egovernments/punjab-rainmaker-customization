package org.egov.migrationkit.service;

import org.egov.migrationkit.constants.WSConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.swagger.client.model.DemandCriteria;

@Component
public class CommonService {

	@Value("${egov.bill.fetch.endpoint}")
	private String fetchBillEndPoint;
	
	@Value("${egov.billingservice.host}")
	private String billingServiceHost;
	
	@Value("${egov.demand.search.endpoint}")
	private String searchDemandEndPoint;
	
	
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
	

}
