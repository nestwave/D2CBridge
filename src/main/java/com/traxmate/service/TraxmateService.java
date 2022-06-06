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
package com.traxmate.service;

import com.nestwave.service.PartnerService;
import com.nestwave.device.service.GnssServiceResponse;
import com.nestwave.device.service.NavigationService;
import com.nestwave.model.GnssPositionResults;
import com.traxmate.model.TraxmateSubmitPositionParameters;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.Charset;

import static com.nestwave.model.Payload.customerId;
import static com.nestwave.model.Payload.deviceId;
import static java.lang.Integer.toHexString;
import static java.lang.Integer.toUnsignedString;
import static java.lang.String.format;
import static org.springframework.http.HttpStatus.CONTINUE;
import static org.springframework.http.HttpStatus.NOT_IMPLEMENTED;

@Slf4j
@Service
public class TraxmateService implements PartnerService{
	final RestTemplate restTemplate;
	final int[] customerIdList;
	final String uriBase;
	final String token;
	final Environment environment;

	public TraxmateService(RestTemplate restTemplate,
	                       @Value("${partners.traxmate.customerIdList}") int[] customerIdList,
	                       @Value("${partners.traxmate.url}") String uriBase,
	                       NavigationService navigationService, Environment environment){
		this.restTemplate = restTemplate;
		this.customerIdList = customerIdList;
		this.uriBase = uriBase;
		this.environment = environment;
		this.token = environment.getProperty("partners.traxmate.token");
		/* Disable the plugin if there is no token. */
		if(token != null && !token.equals("")){
			/* Token is provided. Enable the plugin by registering it. */
			navigationService.register(this);
		}
	}

	public GnssServiceResponse remoteApi(String api, Object data, String... apiParameters){
		ResponseEntity<byte[]> responseEntity;
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer This request is from Nestwave");
		HttpEntity<?> requestEntity = new HttpEntity<>(data, headers);
		String uri;

		api = format(api, apiParameters);
		log.info("Request to API: {}/{}", uriBase, api);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(uriBase + "/" + api);
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
		uri = builder.toUriString();
		log.info("Request forwarded to: {}", uri);
		responseEntity = restTemplate.postForEntity(uri, requestEntity, byte[].class, data);
		return new GnssServiceResponse(responseEntity.getStatusCode(), responseEntity.getBody());
	}

	public GnssServiceResponse submitPosition(int deviceId, TraxmateSubmitPositionParameters data){
		final String api = environment.getProperty("partners.traxmate.api.submitPosition");
		final int customerId = customerId(deviceId);

		log.info("deviceId: {} = {} = {}, customerId: {}", toUnsignedString(deviceId), toHexString(deviceId), deviceId(deviceId), customerId);
		if(api != null){
			for(int cId : customerIdList){
				if(cId == customerId){
					return remoteApi(api.replace("@{deviceId}", toUnsignedString(deviceId)), data);
				}
			}
			return new GnssServiceResponse(CONTINUE, "Not for us!".getBytes());
		}else{
			return new GnssServiceResponse(NOT_IMPLEMENTED, "No url for third party service.\n${partners.traxmate.api.submitPosition} expanded to null.");
		}
	}

	@Override
	public GnssServiceResponse onGnssPosition(int deviceId, GnssPositionResults gnssPositionResults){
		float[] position = gnssPositionResults.position;
		float confidence = gnssPositionResults.confidence;
		TraxmateSubmitPositionParameters data = new TraxmateSubmitPositionParameters(position, confidence, gnssPositionResults);

		return submitPosition(deviceId, data);
	}
}
