package io.swagger.client.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A object holds a demand and collection values for a tax head and period.
 */

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
public class DemandDetail   {
	
        @JsonProperty("id")
        private String id;
        
        @JsonProperty("demandId")
        private String demandId;

        @JsonProperty("taxHeadMasterCode")
        private String taxHeadMasterCode;

        @JsonProperty("taxAmount")
        private BigDecimal taxAmount;

        @JsonProperty("collectionAmount") 
        private BigDecimal collectionAmount = BigDecimal.ZERO;

        @JsonProperty("additionalDetails")
        private Object additionalDetails;

        @JsonProperty("auditDetails")
        private AuditDetails auditDetails;

        @JsonProperty("tenantId")
        private String tenantId;
        
        @JsonProperty("fromDate")
        private Long fromDate;

        @JsonProperty("toDate")
        private Long toDate;
        
        /**
         * This is used to migrate the collection migration
         */
        @JsonProperty("amountPaid")
        private BigDecimal amountPaid;

        
}
