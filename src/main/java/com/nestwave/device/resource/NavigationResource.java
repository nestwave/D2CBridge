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
import com.nestwave.device.service.NavigationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import static com.nestwave.device.util.ApiUtil.getClientIpAddr;
import static com.nestwave.device.service.GnssService.buildResponse;

@Slf4j
@RestController
@RequestMapping("/{apiVer}")
@Validated
public class NavigationResource extends EndPoint{
	public NavigationResource(AssistanceService assistanceService, NavigationService navigationService){
		super(assistanceService, navigationService);
	}

  @ExceptionHandler(javax.validation.ConstraintViolationException.class)
  public ResponseEntity<Object> inputValidationException(Exception e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad parameters in request not accepted");
  }



	@PostMapping(value = {"/gnssDevicePosition", "d"}, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<Object> gnssDevicePosition(@PathVariable String apiVer, @RequestBody byte [] rawResults, HttpServletRequest request) {
		GnssServiceResponse response;

		response = gnssPosition(apiVer, rawResults, getClientIpAddr(request), false);
		return buildResponse(response);
	}

	@PostMapping(value = {"/gnssPosition", "p"}, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	public ResponseEntity<Object> gnssPosition(@PathVariable String apiVer, @RequestBody byte [] rawResults, HttpServletRequest request) {
		GnssServiceResponse response;

		response = gnssPosition(apiVer, rawResults, getClientIpAddr(request), true);
		return buildResponse(response);
	}

	@GetMapping(value = {"gnssDevicePositionsGet"}, produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<Object> gnssDevicePositionsGet(@PathVariable String apiVer, long deviceId, HttpServletRequest request){
		GnssServiceResponse response;

		response = gnssPositionsGet(apiVer, deviceId, getClientIpAddr(request), false);
		return buildResponse(response);
	}

	@GetMapping(value = {"gnssDevicePositionsDelete"}, produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<Object> gnssDevicePositionsDelete(@PathVariable String apiVer, Long deviceId, HttpServletRequest request){
		GnssServiceResponse response;

		response = gnssPositionsGet(apiVer, deviceId, getClientIpAddr(request), true);
		return buildResponse(response);
	}
}
