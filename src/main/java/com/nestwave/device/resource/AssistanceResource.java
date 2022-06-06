/******************************************************************************
 * Copyright 2022 - NESTWAVE SAS
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
 * USE OR OTHER DEALINGS IN THE SOFTWARE.
 *****************************************************************************/
package com.nestwave.device.resource;

import com.nestwave.device.service.AssistanceService;
import com.nestwave.device.service.GnssServiceResponse;
import com.nestwave.device.util.ApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.ParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;

import static com.nestwave.device.service.GnssService.buildResponse;
import static com.nestwave.device.util.ApiUtil.getClientIpAddr;
import static com.nestwave.device.util.GpsTime.getGpsTime;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Slf4j
@RestController
@RequestMapping("/{apiVer}")
@Validated
public class AssistanceResource extends EndPoint{

	private final RestTemplate restTemplate;

	@ExceptionHandler(javax.validation.ConstraintViolationException.class)
	public ResponseEntity<Object> inputValidationException(Exception e) {
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad parameters in request not accepted");
	}

	public AssistanceResource(AssistanceService assistanceService, RestTemplate restTemplate){
		super(assistanceService);
		this.restTemplate = restTemplate;
	}

	@PostMapping(value = {"/gpsTime"}, produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<Object> gpsTime(HttpServletRequest request){
		Long time = getGpsTime();
		String ipAddr = getClientIpAddr(request);
		log.info("Request: gpsTime, IP: " + ipAddr + ", result: " + time);
		return ResponseEntity.status(HttpStatus.OK).body(time.toString());
	}

	@PostMapping(value = {"/gpsApproxPos"}, produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> gpsApproximatePosition(HttpServletRequest request){
		String ipAddr = getClientIpAddr(request);
		URI uri;
		try {
			uri = ApiUtil.buildUri(ipAddr);	
		} catch (URISyntaxException e) {
			log.error("Unable to create URL. cause : " + e);
			return buildResponse(NOT_ACCEPTABLE, "Unable to create URL. cause not valid url format");
		}
		String approxPos;
		try {
			approxPos = ApiUtil.buildApproximatePosition(uri, restTemplate);
			log.info("Request: gptApproxPos, IP: " + ipAddr + ", result: " + approxPos);
		} catch (ParseException e){
			log.error("Unable to parse position in string format. cause : " + e);
			return buildResponse(NOT_ACCEPTABLE, "Unable to parse position in string format");
		}
		return buildResponse(HttpStatus.OK, approxPos);
	}

	/**
	 *
	 * @return get assistance by file time
	 */
	@PostMapping(value = {"/gnssAssistance", "a"}, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<Object> gnssAssistance(@PathVariable String apiVer,
	                                            @RequestBody byte [] assistanceParams,
	                                            HttpServletRequest request) {
		GnssServiceResponse response;

		response = assistanceService.remoteApi(apiVer,  "gnssAssistance", assistanceParams, getClientIpAddr(request));
		return buildResponse(response);
	}
}
