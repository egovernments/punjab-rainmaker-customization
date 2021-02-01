package org.egov.migrationkit.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.egov.migrationkit.constants.WSConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.client.model.Demand;
import io.swagger.client.model.Demand.StatusEnum;
import io.swagger.client.model.DemandDetail;
import io.swagger.client.model.DemandRequest;
import io.swagger.client.model.DemandResponse;
import io.swagger.client.model.OwnerInfo;
import io.swagger.client.model.RequestInfo;
import io.swagger.client.model.RequestInfoWrapper;
import io.swagger.client.model.User;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class DemandService {
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Value("${egov.billingservice.host}")
	private String billingServiceHost;
	
	@Value("${egov.demand.create.url}")
	private String demandCreateEndPoint;
	
	@Value("${egov.bill.fetch.endpoint}")
	private String fetchBillEndPoint;
	
	@Autowired
	private RestTemplate restTemplate;

	public List<Demand> prepareDemandRequest(Map data, String businessService, String consumerCode, String tenantId, OwnerInfo owner) {
		
		
		Map<Integer, List<DemandDetail>> instaWiseDemandMap = new HashMap<>();
		List<Map> dcbDataList = (List) data.get("dcb") != null ? (List) data.get("dcb") : new ArrayList<Map>();
		List<Demand> demands = new LinkedList<>();
		
//		dcbDataList
		for (Map dcbData : dcbDataList) {
			
			String taxHeadMaster = WSConstants.TAX_HEAD_MAP.get((String)dcbData.get("demand_reason"));
			if(WSConstants.EXCLUDE_TAX_HEAD_MASTERS.contains(taxHeadMaster)) {
				continue;
			}
			DemandDetail dd = DemandDetail.builder()
					.taxAmount(BigDecimal.valueOf((Integer)dcbData.get("amount")))
					.taxHeadMasterCode(taxHeadMaster)
					.collectionAmount(BigDecimal.valueOf((Integer)dcbData.get("collected_amount")))
//					.fromDate((Long)dcbData.get("from_date"))
//					.toDate((Long)dcbData.get("to_date"))
					.tenantId(tenantId)
					.build();
			
			Integer installmentId = (Integer)dcbData.get("insta_id");
			if(instaWiseDemandMap.containsKey(installmentId)) {
				instaWiseDemandMap.get(installmentId).add(dd);
			} else {
				List<DemandDetail> ddList = new ArrayList<>();
				ddList.add(dd);
				instaWiseDemandMap.put(installmentId, ddList);
			}
				
		}
		instaWiseDemandMap.forEach((insta_id, ddList) -> {
			
			demands.add(Demand.builder()
					.businessService(businessService)
					.consumerCode(consumerCode)
					.demandDetails(ddList)
					.payer(User.builder().uuid(owner.getUuid()).name(owner.getName()).build())
					.tenantId(tenantId)
//					There is no tax periods configured for all the previous year in PB QA environments as of now giving dummy configured tax period. 
//					.taxPeriodFrom(ddList.get(0).getFromDate())
//					.taxPeriodTo(ddList.get(0).getToDate())
					.taxPeriodFrom(1554076800000l)
					.taxPeriodTo(1617175799000l)
					.consumerType("waterConnection")
					.status(StatusEnum.valueOf("ACTIVE"))
					.build());	
			
		});

		return demands;
		
	}
	
    /**
     * Creates demand
     * @param requestInfo The RequestInfo of the calculation Request
     * @param demands The demands to be created
     * @return The list of demand created
     */
    public List<Demand> saveDemand(RequestInfo requestInfo, List<Demand> demands){
        String url = billingServiceHost + demandCreateEndPoint;
        DemandRequest request = new DemandRequest(requestInfo,demands);
        Object result = restTemplate.postForObject(url , request, String.class);
        try{
           return  objectMapper.convertValue(result,DemandResponse.class).getDemands();
        }
        catch(IllegalArgumentException e){
            log.error("PARSING_ERROR","Failed to parse response of create demand");
        }
		return demands;
    }
    
    public boolean fetchBill(List<Demand> demandResponse, RequestInfo requestInfo) {
		boolean notificationSent = false;
		for (Demand demand : demandResponse) {
			try {

				String url = getFetchBillURL(demand.getTenantId(), demand.getConsumerCode()).toString();
				RequestInfoWrapper request = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
				
				Object result = restTemplate.postForObject(url , request, String.class);
				
				HashMap<String, Object> billResponse = new HashMap<>();
				billResponse.put("requestInfo", requestInfo);
				billResponse.put("billResponse", result);
			} catch (Exception ex) {
				log.error("Fetch Bill Error", ex);
			}
		}
		return notificationSent;
	}
    
	/**
	 * 
	 * @param tenantId
	 *            Tenant Id
	 * @param consumerCode
	 *            Consumer Code
	 * @return uri of fetch bill
	 */
	public StringBuilder getFetchBillURL(String tenantId, String consumerCode) {

		return new StringBuilder().append(billingServiceHost)
				.append(fetchBillEndPoint).append(WSConstants.URL_PARAMS_SEPARATER)
				.append(WSConstants.TENANT_ID_FIELD_FOR_SEARCH_URL).append(tenantId)
				.append(WSConstants.SEPARATER).append(WSConstants.CONSUMER_CODE_SEARCH_FIELD_NAME)
				.append(consumerCode).append(WSConstants.SEPARATER)
				.append(WSConstants.BUSINESSSERVICE_FIELD_FOR_SEARCH_URL)
				.append(WSConstants.WATER_TAX_SERVICE_CODE);
	}
}
