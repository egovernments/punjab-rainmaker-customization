package io.swagger.client.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A object holds a demand and collection values for a tax head and period.
 */

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

		public String getId() {
			return id;
		}

		public String getDemandId() {
			return demandId;
		}

		public String getTaxHeadMasterCode() {
			return taxHeadMasterCode;
		}

		public BigDecimal getTaxAmount() {
			return taxAmount;
		}

		public BigDecimal getCollectionAmount() {
			return collectionAmount;
		}

		public Object getAdditionalDetails() {
			return additionalDetails;
		}

		public AuditDetails getAuditDetails() {
			return auditDetails;
		}

		public String getTenantId() {
			return tenantId;
		}

		public void setId(String id) {
			this.id = id;
		}

		public void setDemandId(String demandId) {
			this.demandId = demandId;
		}

		public void setTaxHeadMasterCode(String taxHeadMasterCode) {
			this.taxHeadMasterCode = taxHeadMasterCode;
		}

		public void setTaxAmount(BigDecimal taxAmount) {
			this.taxAmount = taxAmount;
		}

		public void setCollectionAmount(BigDecimal collectionAmount) {
			this.collectionAmount = collectionAmount;
		}

		public void setAdditionalDetails(Object additionalDetails) {
			this.additionalDetails = additionalDetails;
		}

		public void setAuditDetails(AuditDetails auditDetails) {
			this.auditDetails = auditDetails;
		}

		public void setTenantId(String tenantId) {
			this.tenantId = tenantId;
		}
        
        
}
