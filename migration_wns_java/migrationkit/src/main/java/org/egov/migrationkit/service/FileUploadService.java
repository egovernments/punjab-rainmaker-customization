package org.egov.migrationkit.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import io.swagger.client.model.DocumentDetails;
import io.swagger.client.model.LocalDocument;
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
	

	private static final Map<String, String> documentMap = new HashMap<>();
	static {
		documentMap.put("Self Declaration by Applicant", "APPLICANT.SELF_DECLARATION");
		documentMap.put("Building Plan/Completion Certificate", "BUILDING_PLAN_OR_COMPLETION_CERTIFICATE");
		documentMap.put("ID includes Aadhar Card/Voter Id Card/DL", "OWNER.IDENTITYPROOF.AADHAAR");
		documentMap.put("", "OWNER.ADDRESSPROOF.PAN");
		documentMap.put("", "OWNER.ADDRESSPROOF.VOTERID");
		documentMap.put("", "OWNER.ADDRESSPROOF.DL");
		documentMap.put("Ownership proof/Power of Attorney/Lease Agreement", "OWNER.AUTHORIZATION.AGREEMENT");
		documentMap.put("", "OWNER.ADDRESSPROOF.AADHAAR");
		documentMap.put("", "OWNER.ADDRESSPROOF.ELECTRICITYBILL");
		documentMap.put("", "OWNER.IDENTITYPROOF.VOTERID");
		documentMap.put("", "OWNER.IDENTITYPROOF.PAN");
		documentMap.put("", "OWNER.NOC");
		documentMap.put("", "OWNER.IDENTITYPROOF.DRIVING");
		documentMap.put("Plumber Report/Drawing", "PLUMBER_REPORT_DRAWING");
		documentMap.put("Property Tax Receipt", "PROPERTY_TAX_RECIEPT");
		documentMap.put("Electricity Bill", "ELECTRICITY_BILL");

	}
	
	public List<DocumentDetails> uploadImages(LocalDocument document,String module,String cityCode,String city)  {
		List<DocumentDetails> docs=new ArrayList<>();
		final String tenantId="pb."+city;
		try {
			HttpHeaders headers = new HttpHeaders();
		    //String fileStoreId="0a5b93d4-9eaa-4605-aaf1-970026ec3606";
	        String moduleName="";
		    if(module.equalsIgnoreCase("water"))
		    	moduleName="WTMS";
		    else
		    	moduleName="STMS";
		    	
			String uri = nfsLocation + "/" + cityCode + "/" + moduleName + "/" + document.getFilestoreid();
			// MultipartFile multipartFile = new MockMultipartFile(fileStoreId,
			// new FileInputStream(new File(uri)));
			String newName = document.getFilestoreid()+"."+document.getContenttype().split("/")[1];
			File file=new File(uri);
			file.getName();
			String newPath = nfsLocation+"/"+cityCode+"/"+moduleName+"/" + newName;
            file.renameTo(new File(newPath));
			
            log.info(String.format("Uploaded file   %s   with size  %s " ,file.getName() , file.length()));
			
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
			headers.set("boundary", "----WebKitFormBoundaryEcfz5dm0NVcJ1Jrx34");
			MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
			map.add("file", new FileSystemResource(newPath));
			map.add("tenantId", tenantId);  
			map.add("module", moduleName);
			HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<MultiValueMap<String, Object>>(map,
					headers);
			
			String url = host+"/filestore/v1/files";
			   ResponseEntity<StorageResponse> result  = restTemplate.postForEntity(url, request, StorageResponse.class);  
			log.info(result.toString());

			log.info("FilestoreID: " + result.getBody().getFiles().get(0).getFileStoreId());
			String newFileStore = result.getBody().getFiles().get(0).getFileStoreId();
			DocumentDetails doc = new DocumentDetails();
			doc.setFileStoreId(result.getBody().getFiles().get(0).getFileStoreId());
			doc.setConnectionNo(document.getConnectionNo());
			doc.setDocumentType(documentMap.get(document.getDocumentname()) == null ? document.getDocumentname()
					: documentMap.get(document.getDocumentname()));
			doc.setTenantId(tenantId);
			docs.add(doc);

			recordService.saveMigratedFilestoreDetails(module,document.getFilestoreid(), newFileStore,
					document.getConnectionNo(), true, null, tenantId);
		} catch (RestClientException e) {
			e.printStackTrace();
			recordService.saveMigratedFilestoreDetails(module,document.getFilestoreid(), null, document.getConnectionNo(),
					false, e.getMessage(), tenantId);
			throw new RuntimeException("Failed to migrate files.");

		}
		return docs;

	}
}
