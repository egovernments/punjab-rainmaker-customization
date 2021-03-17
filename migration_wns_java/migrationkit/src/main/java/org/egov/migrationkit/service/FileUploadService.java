package org.egov.migrationkit.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import io.swagger.client.model.Document;
import io.swagger.client.model.StorageResponse;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class FileUploadService {

	@Autowired
	private RestTemplate restTemplate;
	
	@Value("${erp.services.nfs.location}")
	private String nfsLocation = null;
	
	@Value("${egov.services.hosturl}")
	private String host = null;
	
	@Autowired
	private RecordService recordService;
	
	public List<Document> uploadImages(String connNo,String module,String cityCode)  {
		List<Document> docs=new ArrayList<>();
		
		try {
			HttpHeaders headers = new HttpHeaders();
		    String fileStoreId="0a5b93d4-9eaa-4605-aaf1-970026ec3606";
	        String moduleName="";
		    if(module.equalsIgnoreCase("water"))
		    	moduleName="WTMS";
		    else
		    	moduleName="WCMS";
		    	
			
			  String uri= 	nfsLocation+"/"+cityCode+"/"+moduleName+"/"+fileStoreId;
	        //	MultipartFile multipartFile = new MockMultipartFile(fileStoreId, new FileInputStream(new File(uri)));
			File file=new File(uri);
			file.getName();
		 
			log.debug(String.format("Uploaded file   %s   with size  %s " ,file.getName() , file.length()));
			
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
			map.add("file", new FileSystemResource(uri));
			map.add("tenantId", "pb");  
			map.add("module", moduleName);
			HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(map,
					headers);
			
			   String url = host+"/filestore/v1/files";
			   ResponseEntity<StorageResponse> result  = restTemplate.postForEntity(url, request, StorageResponse.class);  
			log.info(result.toString());
					
		 
		} catch (RestClientException e) {
		e.printStackTrace();	 

		}  
		
		
	 return docs;  

	   // return new ResponseEntity<>(response, httpStatus);
	}

	
}
