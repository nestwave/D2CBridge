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

import com.nestwave.model.Payload;
import com.nestwave.device.service.AssistanceService;
import com.nestwave.device.service.GnssService;
import com.nestwave.device.service.GnssServiceResponse;
import com.nestwave.device.service.NavigationService;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import static java.lang.String.format;
import static org.apache.tomcat.util.codec.binary.Base64.encodeBase64String;

@Slf4j
public class EndPoint{
	protected final AssistanceService assistanceService;
	protected final NavigationService navigationService;

	public EndPoint(AssistanceService assistanceService){
		this(assistanceService, null);
	}
	public EndPoint(AssistanceService assistanceService, NavigationService navigationService){
		this.assistanceService = assistanceService;
		this.navigationService = navigationService;
	}

	boolean supports(GnssService gnssService, String apiVerion){
		/* If gnssService is null , then we don't need it. We can, safely, assume version is valid then. */
		return gnssService == null || gnssService.supports(apiVerion);
	}

	public GnssServiceResponse supports(@NonNull String apiVer){
		String msg;

		if(supports(assistanceService, apiVer) && supports(navigationService, apiVer)){
			return new GnssServiceResponse(HttpStatus.OK, (byte[])null);
		}else{
			msg = format("This API version (%s) is invalid.", apiVer);
			log.error("Unsupported API version {}. {}", apiVer, msg);
			return new GnssServiceResponse(HttpStatus.NOT_IMPLEMENTED, msg);
		}
	}

	public GnssServiceResponse supports(@NonNull String apiVer, @NonNull String min){
		String msg;

		if(apiVer.compareTo(min) < 0){
			msg = format("This API requires FW upgrade to at least version %s.", min);
			log.error("Unsupported API version {}. {}", apiVer, msg);
			return new GnssServiceResponse(HttpStatus.UPGRADE_REQUIRED, msg);
		}else{
			return supports(apiVer);
		}
	}

	public GnssServiceResponse supports(@NonNull String apiVer, @NonNull String min, @NonNull String max){
		String msg;

		if(apiVer.compareTo(max) > 0){
			msg = format("This API is gone since version %s and will not be supported anymore.", max);
			log.error("Unsupported API version {}. {}", apiVer, msg);
			return new GnssServiceResponse(HttpStatus.GONE, msg);
		}else{
			return supports(apiVer, min);
		}
	}

	public GnssServiceResponse gnssPosition(@NonNull String apiVer, byte [] rawResults, String clientIpAddr, boolean noc){
		Payload payload;
		GnssServiceResponse response;
		String strPayload;

		//request from IP: 178.208.16.92, getAssistance by date : 2018-06-02T18:00:25 (GPS Time: 1211997625)
		log.info("Request from IP: {}, API: /{}/gnssPosition", clientIpAddr, apiVer);
		if(apiVer.compareTo("v1.7") < 0){
			payload = new Payload(rawResults, 4);
		}else{
			payload = new Payload(rawResults);
		}
		strPayload = encodeBase64String(payload.content);
		log.info("deviceId = {}, chkWord = {}, rawResults = \"{}\"", payload.deviceId, payload.chkWord, strPayload);
		response = navigationService.gnssPosition(apiVer, rawResults, clientIpAddr, noc);
		return response;
	}

	public GnssServiceResponse gnssPositionsGet(@NonNull String apiVer, Long deviceId, String clientIpAddr, boolean drop){
		GnssServiceResponse response;

		log.info("Request from IP: {}, API: /{}/gnssPositionsGet?deviceId={}&drop={}", clientIpAddr, apiVer, deviceId, drop);
		if(deviceId == null){
			log.error("Rejected due to missing mandatory parameter deviceId");
			response = new GnssServiceResponse(HttpStatus.EXPECTATION_FAILED, "Please supply a deviceId parameter.");
		}else{
			response = navigationService.retrievePositionsFromDatabase(deviceId);
			if(drop){
				navigationService.dropPositionsFromDatabase(deviceId);
			}
		}
		return response;
	}
}
