package org.egov.migrationkit.web;
import io.swagger.client.*;
import io.swagger.client.auth.*;
import io.swagger.client.model.*;
import io.swagger.client.api.SewerageApi;

import java.io.File;
import java.util.*;

public class Sample {
	  public static void main(String[] args) {
	        
	        SewerageApi apiInstance = new SewerageApi();
	        SewerageConnectionRequest body = new SewerageConnectionRequest(); // SewerageConnectionRequest | Details for the new Sewerage Connection + RequestHeader meta data.
	        try {
	            SewerageConnectionResponse result = apiInstance.swcCreatePost(body);
	            System.out.println(result);
	        } catch (ApiException e) {
	            System.err.println("Exception when calling SewerageApi#swcCreatePost");
	            e.printStackTrace();
	        }
	    }
}





 