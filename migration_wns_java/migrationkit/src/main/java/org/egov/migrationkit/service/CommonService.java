package org.egov.migrationkit.service;

import org.egov.migrationkit.constants.WSConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CommonService {

	@Value("${egov.bill.fetch.endpoint}")
	private String fetchBillEndPoint;
	
	@Value("${egov.billingservice.host}")
	private String billingServiceHost;
	
	/**
	 * 
	 * @param tenantId
	 *            Tenant Id
	 * @param consumerCode
	 *            Consumer Code
	 * @return uri of fetch bill
	 */
	public StringBuilder getFetchBillURL(String tenantId, String consumerCode, String businessService) {

		return new StringBuilder().append(billingServiceHost)
				.append(fetchBillEndPoint).append(WSConstants.URL_PARAMS_SEPARATER)
				.append(WSConstants.TENANT_ID_FIELD_FOR_SEARCH_URL).append(tenantId)
				.append(WSConstants.SEPARATER).append(WSConstants.CONSUMER_CODE_SEARCH_FIELD_NAME)
				.append(consumerCode).append(WSConstants.SEPARATER)
				.append(WSConstants.BUSINESSSERVICE_FIELD_FOR_SEARCH_URL)
				.append(businessService);
	}
}
