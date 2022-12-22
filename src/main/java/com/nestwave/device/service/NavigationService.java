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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nestwave.device.repository.position.PositionRecord;
import com.nestwave.device.repository.position.PositionRepository;
import com.nestwave.device.util.JwtTokenUtil;
import com.nestwave.model.GnssPositionResults;
import com.nestwave.model.Payload;
import com.nestwave.service.PartnerService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.io.IOException;

import static com.nestwave.device.util.GpsTime.getUtcAssistanceTime;
import static java.util.Arrays.copyOf;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
public class NavigationService extends GnssService{
	private final PositionRepository positionRepository;
	private PartnerService[] partnerServices;

	public NavigationService(JwtTokenUtil jwtTokenUtil,
	                         PositionRepository positionRepository,
                           @Value("${navigation.base_url}") String uri,
	                         ObjectMapper objectMapper,
                           RestTemplate restTemplate,
                           @Value("${background.base_url}") String backgroundUrl) {
		super(jwtTokenUtil, uri, restTemplate, objectMapper);
		this.positionRepository = positionRepository;
	partnerServices = new PartnerService[0];
  }

	public boolean supports(String apiVer){
		return apiVer != null && apiVer.compareTo("v1.4") >= 0;
	}

	public void register(PartnerService partnerService){
		int len = partnerServices.length;

		partnerServices = copyOf(partnerServices, len + 1);
		partnerServices[len] = partnerService;
	}

	public GnssServiceResponse gnssPosition(String apiVer, byte[] rawResults, String clientIpAddr, boolean noc){
		NavigationParameters navigationParameters;
		Payload payload;
	    GnssServiceResponse response;
	    String api;

	    if(noc){
		    api = "gnssPosition";
	    }else{
		    api = "gnssDevicePosition";
	    }
		if(apiVer.compareTo("v1.7") < 0){
			payload = new Payload(rawResults, 4);
		}else{
			payload = new Payload(rawResults);
		}
		navigationParameters = new NavigationParameters(payload);
		ResponseEntity<GnssPositionResults> responseEntity = remoteApi(apiVer, api, navigationParameters, clientIpAddr, GnssPositionResults.class);
		response = savePosition(apiVer, payload, responseEntity);
		return response;
    }

	public GnssServiceResponse dropPositionsFromDatabase(long deviceId)
	{
		log.info("Drop all positions for deviceId = {}", deviceId);
		positionRepository.dropAllPositionRecordsWithId(deviceId);
		return new GnssServiceResponse(HttpStatus.OK, (byte[])null);
	}

	public GnssServiceResponse retrievePositionsFromDatabase(long deviceId)
	{
		String csv;

		log.info("Query all positions for deviceId = {}", deviceId);
		csv = positionRepository.getAllPositionRecordsWithId(deviceId);
		return new GnssServiceResponse(HttpStatus.OK, csv.getBytes());
	}

	public GnssServiceResponse savePosition(String apiVer, Payload payload, ResponseEntity<GnssPositionResults> responseEntity){
		GnssServiceResponse response;
		GnssPositionResults gnssPositionResults = responseEntity.getBody();

		try{
			response = new GnssServiceResponse(responseEntity.getStatusCode(), objectMapper.writeValueAsBytes(gnssPositionResults));
		}catch(JsonProcessingException e){
			log.error("Error when processing JSON: {}", e.getMessage());
			response = new GnssServiceResponse(INTERNAL_SERVER_ERROR, "Cloud not serialize navigation results:\n" + gnssPositionResults);
		}
		if(response.status == HttpStatus.OK){
			response = savePositionIntoDatabase(apiVer, payload.deviceId, response.message);
			for(PartnerService service : partnerServices){
				int customerId = payload.customerId();
				long deviceId = payload.deviceId;
				GnssServiceResponse resp;
				try{
					resp = service.onGnssPosition(customerId, deviceId, gnssPositionResults);
					log.info("Partner's service {} returned status {} and content {}.", service.getClass().getName(), resp.status, new String(resp.message));
				}catch(RestClientException e){
					log.error("Unexpected partner server error:\n{}", e.getMessage());
				}
			}
		}
		return response;
	}
	public GnssServiceResponse savePositionIntoDatabase(String apiVer, long deviceId, byte[] json){
		log.info("Decoded position:\n{}", new String(json));
		GnssPositionResults navResults;
		try{
			navResults = objectMapper.readValue(json, GnssPositionResults.class);
		}catch(IOException e){
			log.error("Error parsing json : {}", e.getMessage());
			return new GnssServiceResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Parsing position failed");
		}
		log.info("gpsTime = {}", navResults.gpsTime);
		if(deviceId != 0){
			PositionRecord positionRecord = new PositionRecord(deviceId, getUtcAssistanceTime(navResults.gpsTime),
					navResults.confidence,
					navResults.position[0], navResults.position[1], navResults.position[2],
					navResults.velocity[0], navResults.velocity[1], navResults.velocity[2]);
			positionRepository.insertNavigationRecord(positionRecord);
			log.info("New position inserted in positions database.");
		}
		return new GnssServiceResponse(HttpStatus.OK, navResults.payload);
	}
}

@Data
class NavigationParameters extends GnssServiceParameters{
	@NotNull
	@Schema(description = "GNSS raw measurements data as sent by the Iot device",
			example = "AAAAAA4AAAA=", required = true)
	public byte[] rawMeas;

	public NavigationParameters(Payload payload){
		super(payload);
		rawMeas = payload.content;
	}
}
