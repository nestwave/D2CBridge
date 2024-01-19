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
import com.nestwave.device.model.*;
import com.nestwave.device.repository.position.PositionRecord;
import com.nestwave.device.repository.position.PositionRepository;
import com.nestwave.device.repository.thintrack.ThinTrackPlatformBarometerStatusRecord;
import com.nestwave.device.repository.thintrack.ThinTrackPlatformStatusRecord;
import com.nestwave.device.repository.thintrack.ThinTrackPlatformStatusRepository;
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
import java.util.List;

import static java.util.Arrays.copyOf;
import static org.springframework.http.HttpStatus.*;

@Slf4j
@Service
public class NavigationService extends GnssService{
	private final PositionRepository positionRepository;
	private final ThinTrackPlatformStatusRepository thintrackPlatformStatusRepository;
	private PartnerService[] partnerServices;

	public NavigationService(JwtTokenUtil jwtTokenUtil,
	                         PositionRepository positionRepository,
	                         ThinTrackPlatformStatusRepository thintrackPlatformStatusRepository,
                           @Value("${navigation.base_url}") String uri,
	                         ObjectMapper objectMapper,
	                         RestTemplate restTemplate){
		super(jwtTokenUtil, uri, restTemplate, objectMapper);
		this.positionRepository = positionRepository;
		this.thintrackPlatformStatusRepository = thintrackPlatformStatusRepository;
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
		byte[] jsonResponse = serializeResponse(responseEntity);
		if(jsonResponse == null){
			response = new GnssServiceResponse(INTERNAL_SERVER_ERROR, "Cloud not serialize navigation results:\n" + responseEntity);
		}else{
			GnssPositionResults navResults = responseEntity.getBody();
			response = savePosition(payload, navResults);
		}
		return response;
    }

	public GnssServiceResponse dropPositionsFromDatabase(long deviceId)
	{
		log.debug("Drop all positions for deviceId = {}", deviceId);
		positionRepository.dropAllPositionRecordsWithId(deviceId);
		return new GnssServiceResponse(HttpStatus.OK, (byte[])null);
	}

	public GnssServiceResponse dropPlatformStatusFromDatabase(long deviceId)
	{
		log.debug("Drop all platform status records for deviceId = {}", deviceId);
		thintrackPlatformStatusRepository.dropAllRecordsWithId(deviceId);
		return new GnssServiceResponse(HttpStatus.OK, (byte[])null);
	}

	public GnssServiceResponse locate(String apiVer, Payload payload, String clientIpAddr){
		HybridNavPayload hybridNavPayload;
		HybridNavigationParameters hybridNavigationParameters;
		ResponseEntity<GnssPositionResults> responseEntity;
		GnssServiceResponse response;
		GnssPositionResults navResults = null;
		String api = "locate";

		try{
			hybridNavPayload = new HybridNavPayload(payload);
		}catch(InvalidHybridNavPayloadException e){
			return new GnssServiceResponse(NOT_ACCEPTABLE, e.getMessage());
		}
		ThinTrackPlatformStatusRecord[] thinTrackPlatformStatusRecords = ThinTrackPlatformStatusRecord.of(payload.deviceId, null, hybridNavPayload);
		hybridNavigationParameters = new HybridNavigationParameters(payload, hybridNavPayload, thinTrackPlatformStatusRecords);
		for(ThinTrackPlatformStatusRecord record : thinTrackPlatformStatusRecords){
			if(record instanceof ThinTrackPlatformBarometerStatusRecord){
				String[] features = {"PAAN"};
				hybridNavigationParameters.features = features;
				break;
			}
		}
		try{
			log.info("hybridNavigationParameters = {}", objectMapper.writeValueAsString(hybridNavigationParameters));
		}catch(Exception e){
			log.error("Error when processing JSON: {}", e.getMessage());
		}
		responseEntity = remoteApi(apiVer, api, hybridNavigationParameters, clientIpAddr, GnssPositionResults.class);
		byte[] jsonResponse = serializeResponse(responseEntity);
		if(jsonResponse == null){
			return new GnssServiceResponse(INTERNAL_SERVER_ERROR, "Cloud not serialize navigation results:\n" + responseEntity);
		}
		navResults = responseEntity.getBody();
		for(ThinTrackPlatformStatusRecord thinTrackPlatformStatusRecord : thinTrackPlatformStatusRecords){
			thinTrackPlatformStatusRecord.setKey(payload.deviceId, navResults.utcTime);
			log.info("ThinkTrack platform status: {}", thinTrackPlatformStatusRecord);
			if(thinTrackPlatformStatusRecord != null){
				navResults.thintrackPlatformStatus = thintrackPlatformStatusRepository.insertNewRecord(thinTrackPlatformStatusRecord);
			}
		}
		response = savePosition(payload, navResults);
		if(response.status == OK && response.message != null){
			response = new GnssServiceResponse(OK, hybridNavPayload.addTechno(navResults.technology, response.message));
		}
		return response;
	}

	public GnssServiceResponse retrievePositionsFromDatabase(long deviceId)
	{
		String csv;

		log.debug("Query all positions for deviceId = {}", deviceId);
		csv = positionRepository.getAllPositionRecordsWithId(deviceId);
		return new GnssServiceResponse(HttpStatus.OK, csv.getBytes());
	}

	public GnssServiceResponse retrievePositionsAndPlatofrmStatusFromDatabase(long deviceId)
	{
		String csv;
		List<PositionRecord> positionRecords = positionRepository.findAllPositionRecordsById(deviceId);

		if(positionRecords.isEmpty()){
			return retrievePositionsFromDatabase(deviceId);
		}
		log.debug("Query all positions and status records for deviceId = {}", deviceId);
		csv = thintrackPlatformStatusRepository.getAllRecordsWithId(deviceId, positionRecords);
		return new GnssServiceResponse(HttpStatus.OK, csv.getBytes());
	}

	public GnssServiceResponse savePosition(Payload payload, GnssPositionResults navResults){
		GnssServiceResponse response;
		int customerId = payload.customerId();
		long deviceId = payload.deviceId;

		if(deviceId == 0){
			response = new GnssServiceResponse(OK, navResults.payload);
		}else{
			response = savePositionIntoDatabase(payload.deviceId, navResults);
			for(PartnerService service : partnerServices){
				GnssServiceResponse resp;
				try{
					resp = service.onGnssPosition(customerId, deviceId, navResults);
					log.info("Partner's service {} returned status {} and content {}.", service.getClass().getName(), resp.status, new String(resp.message));
				}catch(RestClientException e){
					log.error("Unexpected partner server error:\n{}", e.getMessage());
				}
			}
		}
		return response;
	}

	public GnssServiceResponse savePositionIntoDatabase(long deviceId, GnssPositionResults navResults){
		PositionRecord positionRecord = new PositionRecord(deviceId, navResults.utcTime,
				navResults.confidence,
				navResults.position[0], navResults.position[1], navResults.position[2],
				navResults.velocity[0], navResults.velocity[1], navResults.velocity[2]);

		positionRepository.insertNavigationRecord(positionRecord);
		log.info("New position inserted in positions database.");
		return new GnssServiceResponse(HttpStatus.OK, navResults.payload, navResults.gpsTime);
	}

	<T> byte[] serializeResponse(ResponseEntity<T> responseEntity){
		byte[] jsonResponse;
		T body = responseEntity.getBody();

		try{
			jsonResponse = objectMapper.writeValueAsBytes(body);
		}catch(JsonProcessingException e){
			log.error("Error when processing JSON: {}", e.getMessage());
			jsonResponse = null;
		}
		return jsonResponse;
	}
}

@Data
class NavigationParameters extends GnssServiceParameters{
	@NotNull
	@Schema(description = "GNSS raw measurements data as sent by the Iot device",
			example = "AAAAAA4AAAA=", required = true)
	public byte[] rawMeas;
	public String[] features;
	public NavigationParameters(Payload payload){
		super(payload);
		rawMeas = payload.content;
	}
}

@Data
class HybridNavigationParameters extends NavigationParameters{
	@Schema(description = "Hybrid navigation data as expected by the third party services")
	public HybridNavParameters hybrid;

	public HybridNavigationParameters(Payload payload, HybridNavPayload hybridNavPayload, ThinTrackPlatformStatusRecord[] thinTrackPlatformStatusRecord){
		super(payload);

		rawMeas = hybridNavPayload.rawMeas();
		hybrid = new HybridNavParameters(hybridNavPayload, thinTrackPlatformStatusRecord);
	}
}
