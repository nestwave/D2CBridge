package com.nestwave.device.resource;

import com.nestwave.device.service.AssistanceService;
import com.nestwave.device.service.GnssServiceResponse;
import com.nestwave.device.service.NavigationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.nestwave.device.service.GnssService.buildResponse;
import static com.nestwave.device.util.ApiUtil.getClientIpAddr;

@Slf4j
@RestController
@RequestMapping("/{apiVer}")
@Validated
public class HybridNavigationResource extends EndPoint{
	public HybridNavigationResource(AssistanceService assistanceService, NavigationService navigationService){
		super(assistanceService, navigationService);
	}

	@PostMapping(value = {"/locate", "p"}, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<Object> locate(@PathVariable String apiVer, @RequestBody byte [] reqPayload, HttpServletRequest request) {
		GnssServiceResponse response;

		response = locate(apiVer, reqPayload, getClientIpAddr(request));
		return buildResponse(response);
	}

}
