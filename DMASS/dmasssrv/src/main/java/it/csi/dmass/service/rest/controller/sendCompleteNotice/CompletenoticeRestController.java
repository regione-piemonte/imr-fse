package it.csi.dmass.service.rest.controller.sendCompleteNotice;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;

import it.csi.dmass.service.rest.service.sendCompleteNotice.CompleteNoticeService;
import it.csi.dmass.specification.api.CompleteNoticeApi;

@RestController
public class CompletenoticeRestController implements CompleteNoticeApi {

	@Autowired
	@Qualifier("completeNoticeService")	
	private CompleteNoticeService completeNoticeService;
	
	@Autowired
	HttpServletRequest request;
	
	@Override
	public ResponseEntity<Void> completeNotice(String zipName, String jobUID, String status, String requestID,
			String checksum, String dist, String codeError) {

		try {						
			completeNoticeService.completeNotice(zipName, jobUID, status, requestID, checksum, dist,codeError,request);			
			return new ResponseEntity<Void>(HttpStatus.OK); 

		} catch (Exception e) {
			return new ResponseEntity<Void>(HttpStatus.BAD_REQUEST);
		}
	}		
	
	

}
