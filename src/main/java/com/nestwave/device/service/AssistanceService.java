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
package com.nestwave.device.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nestwave.device.util.JwtTokenUtil;
import com.nestwave.model.Payload;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;

@Slf4j
@Service
public class AssistanceService extends GnssService{
	public AssistanceService(RestTemplate restTemplate,
	                         JwtTokenUtil jwtTokenUtil,
	                         ObjectMapper objectMapper,
	                         @Value("${navigation.base_url}") String uri){
		super(jwtTokenUtil, uri, restTemplate, objectMapper);
	}

	public boolean supports(String apiVer){
		return apiVer != null && apiVer.compareTo("v1.4") >= 0;
	}

	public GnssServiceResponse remoteApi(String apiVer, String api, byte[] payloadContent, String clientIpAddr){
		AssistanceParameters assistanceParameters;
		Payload payload;
		ResponseEntity<byte[]> responseEntity;

		if(apiVer.compareTo("v1.7") < 0){
			payload = new Payload(payloadContent, 4);
		}else{
			payload = new Payload(payloadContent);
		}
		assistanceParameters = new AssistanceParameters(payload);
		responseEntity = remoteApi(apiVer, api, assistanceParameters, clientIpAddr, byte[].class);

		return new GnssServiceResponse(responseEntity.getStatusCode(), responseEntity.getBody());
	}
}

@Data
class AssistanceParameters extends GnssServiceParameters{
	@NotNull
	@Schema(description = "Assistance data as sent by the Iot device",
			example = "AAAAAA4AAAA=", required = true)
	public byte[] assistancePayload;

	public AssistanceParameters(Payload payload){
		super(payload);
		assistancePayload = payload.content;
	}
}
