package io.swagger.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WaterMigrateRequest {
    
	@JsonProperty("RequestInfo")
	private RequestInfo RequestInfo;

	public RequestInfo getRequestInfo() {
		return RequestInfo;
	}

	public void setRequestInfo(RequestInfo requestInfo) {
		this.RequestInfo = requestInfo;
	}

}
