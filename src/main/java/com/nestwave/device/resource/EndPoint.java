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
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

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

	public GnssServiceResponse gnssAssistance(@NonNull String apiVer, byte [] reqPayload, String clientIpAddr){
		Payload payload;
		GnssServiceResponse response;
		String strPayload;

		log.info("Request from IP: {}, API: /{}/gnssAsssitance, reqPayload = {}", clientIpAddr, apiVer, encodeBase64String(reqPayload));
		if(apiVer.compareTo("v1.7") < 0){
			payload = new Payload(reqPayload, 4);
		}else{
			payload = new Payload(reqPayload);
		}
		strPayload = encodeBase64String(payload.content);
		log.info("deviceId = {}, chkWord = {}, rawResults = \"{}\"", payload.deviceId, payload.chkWord, strPayload);
		if(!payload.isValid){
			return new GnssServiceResponse(UNPROCESSABLE_ENTITY, format("Payload integrity check failed!"));
		}
		response = assistanceService.remoteApi(apiVer,  "gnssAssistance", reqPayload, clientIpAddr);
		return response;
	}

	public GnssServiceResponse gnssPosition(@NonNull String apiVer, byte [] reqPayload, String clientIpAddr, boolean noc){
		Payload payload;
		GnssServiceResponse response;
		String strPayload;

		log.info("Request from IP: {}, API: /{}/gnssPosition, reqPayload = {}", clientIpAddr, apiVer, encodeBase64String(reqPayload));
		if(apiVer.compareTo("v1.7") < 0){
			payload = new Payload(reqPayload, 4);
		}else{
			payload = new Payload(reqPayload);
		}
		strPayload = encodeBase64String(payload.content);
		log.info("deviceId = {}, chkWord = {}, rawResults = \"{}\"", payload.deviceId, payload.chkWord, strPayload);
		if(!payload.isValid){
			return new GnssServiceResponse(UNPROCESSABLE_ENTITY, format("Payload integrity check failed!"));
		}
		response = navigationService.gnssPosition(apiVer, reqPayload, clientIpAddr, noc);
		return response;
	}

	public GnssServiceResponse gnssPositionsGet(@NonNull String apiVer, Long deviceId, String clientIpAddr, boolean drop){
		GnssServiceResponse response;

		log.debug("Request from IP: {}, API: /{}/gnssPositionsGet?deviceId={}&drop={}", clientIpAddr, apiVer, deviceId, drop);
		if(deviceId == null){
			log.error("Rejected due to missing mandatory parameter deviceId");
			response = new GnssServiceResponse(HttpStatus.EXPECTATION_FAILED, "Please supply a deviceId parameter.");
		}else{
			if(apiVer.compareTo("v1.8") > 0){
				response = navigationService.retrievePositionsAndPlatofrmStatusFromDatabase(deviceId, apiVer);
			}else{
				response = navigationService.retrievePositionsFromDatabase(deviceId);
			}
			if(drop){
				navigationService.dropPositionsFromDatabase(deviceId);
				if(apiVer.compareTo("v1.8") > 0){
					navigationService.dropPlatformStatusFromDatabase(deviceId);
				}
			}
		}
		return response;
	}

	public GnssServiceResponse locate(@NonNull String apiVer, byte [] reqPayload, String clientIpAddr){
		Payload payload;
		GnssServiceResponse response;
		String strPayload;

		log.info("Request from IP: {}, API: /{}/locate, reqPayload = {}", clientIpAddr, apiVer, encodeBase64String(reqPayload));
		if(apiVer.compareTo("v1.7") < 0){
			payload = new Payload(reqPayload, 4);
		}else{
			payload = new Payload(reqPayload);
		}
		strPayload = encodeBase64String(payload.content);
		log.info("deviceId = {}, chkWord = {}, payload = \"{}\"", payload.deviceId, payload.chkWord, strPayload);
		if(!payload.isValid){
			return new GnssServiceResponse(UNPROCESSABLE_ENTITY, format("Payload integrity check failed!"));
		}
		if(payload.deviceId == 0){
			return new GnssServiceResponse(UNAUTHORIZED, "Invalid device ID");
		}
		response = navigationService.locate(apiVer, payload, clientIpAddr);
		return response;
	}
}
