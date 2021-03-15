package org.egov.migrationkit.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.client.model.Document;

@Service
public class FileUploadService {

	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private RecordService recordService;
	
	/*public List<Document> uploadImages(String connNo,String module) throws IOException {
	    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
	    String response;
	    HttpStatus httpStatus = HttpStatus.CREATED;
	    recordService.getFiles(connNo,module);

	    
	    
	    try {
	        for (MultipartFile file : files) {
	            if (!file.isEmpty()) {
	                map.add("images", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));
	            }
	        }

	        HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

	        String url = "http://example.com/upload";

	        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
	        response = restTemplate.postForObject(url, requestEntity, String.class);

	    } catch (HttpStatusCodeException e) {
	        httpStatus = HttpStatus.valueOf(e.getStatusCode().value());
	        response = e.getResponseBodyAsString();
	    } catch (Exception e) {
	        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
	        response = e.getMessage();
	    }

	    return new ResponseEntity<>(response, httpStatus);
	}

	class MultipartInputStreamFileResource extends InputStreamResource {

	    private final String filename;

	    MultipartInputStreamFileResource(InputStream inputStream, String filename) {
	        super(inputStream);
	        this.filename = filename;
	    }

	    @Override
	    public String getFilename() {
	        return this.filename;
	    }

	    @Override
	    public long contentLength() throws IOException {
	        return -1; // we do not want to generally read the whole stream into memory ...
	    }
	}*/

}
