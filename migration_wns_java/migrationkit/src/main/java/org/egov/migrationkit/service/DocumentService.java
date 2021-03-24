package org.egov.migrationkit.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.swagger.client.model.DocumentDetails;
import io.swagger.client.model.DocumentRequest;
import io.swagger.client.model.LocalDocument;
import io.swagger.client.model.RequestInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DocumentService {

	private static final String MODULE_NAME_WATER = "water";
	
	@Autowired
	private RestTemplate restTemplate;

	@Value("${erp.services.nfs.location}")
	private String nfsLocation = null;

	@Value("${egov.services.hosturl}")
	private String host = null;

	@Autowired
	private RecordService recordService;

	@Autowired
	private FileUploadService fileUploadService;

	public void migrateWtrDocuments(String city, RequestInfo requestInfo) {
		List<DocumentDetails> uploadedDocuments = new ArrayList<>();
		List<LocalDocument> allDocs = recordService.getAllWSFilesByTenantId(city);
		String cityCode = recordService.getCityCodeByName(city);
		for (LocalDocument document : allDocs) {
			List<DocumentDetails> documents = fileUploadService.uploadImages(document, MODULE_NAME_WATER, cityCode,
					city);
			uploadedDocuments.addAll(documents);
		}
		String wsUrl = host + "/ws-services/wc/documents/_create?tenantId=pb." + city;
		storeFileStoreIds(uploadedDocuments,wsUrl, requestInfo);

	}

	public void migrateSWDocuments(String city, RequestInfo requestInfo) {
		List<DocumentDetails> uploadedDocuments = new ArrayList<>();
		List<LocalDocument> allDocs = recordService.getAllSWFilesByTenantId(city);
		String cityCode = recordService.getCityCodeByName(city);
		for (LocalDocument document : allDocs) {
			List<DocumentDetails> documents = fileUploadService.uploadImages(document,"sewerage", cityCode,
					city);
			uploadedDocuments.addAll(documents);
		}
		String swUrl = host + "/sw-services/swc/documents/_create?tenantId=pb." + city;
		storeFileStoreIds(uploadedDocuments, swUrl, requestInfo);

	}

	private void storeFileStoreIds(List<DocumentDetails> uploadedDocuments, String url, RequestInfo requestInfo) {
		uploadedDocuments.forEach(doc -> doc.setUserUid(requestInfo.getUserInfo().getUuid()));
		DocumentRequest documentReq = DocumentRequest.builder().requestInfo(requestInfo).documents(uploadedDocuments)
				.build();
		ResponseEntity<String> result = restTemplate.postForEntity(url, documentReq, String.class);
		log.info("result:" + result.toString());
	}

}
