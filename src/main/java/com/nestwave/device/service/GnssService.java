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
import com.nestwave.model.Payload;
import com.nestwave.device.util.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.Charset;

import static java.lang.Integer.toUnsignedLong;
import static org.apache.tomcat.util.codec.binary.Base64.encodeBase64String;

@Slf4j
public abstract class GnssService {
    public final JwtTokenUtil jwtTokenUtil;
    public final String uriBase;
    public final ObjectMapper objectMapper;
    public final RestTemplate restTemplate;

    public GnssService(JwtTokenUtil jwtTokenUtil, String uri, RestTemplate restTemplate, ObjectMapper om){
        this.jwtTokenUtil = jwtTokenUtil;
        uriBase = uri;
        objectMapper = om;
        this.restTemplate = restTemplate;
    }
    public abstract boolean supports(String apiVer);

    public static ResponseEntity<Object> buildResponse(GnssServiceResponse response) {
        return ResponseEntity.status(response.status).body(response.message);
    }

    public static <T> ResponseEntity<T> buildResponse(HttpStatus httpStatus, T msg) {
        return ResponseEntity.status(httpStatus).body(msg);
    }
    
    public <T> ResponseEntity<T> remoteApi(String apiVer, String api, byte[] payloadContent, String clientIpAddr, Class<T> responseType){
		ResponseEntity<T> responseEntity;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtTokenUtil.getSecret());
        HttpEntity<byte[]> requestEntity = new HttpEntity<>(payloadContent, headers);
        Payload payload;
        String uri;
        String strResponse;

        log.info("Request from IP: {}, API: /{}/{}}", clientIpAddr, apiVer, api);
        payload = new Payload(payloadContent);
        log.info("deviceId = {},  chkWord = {}, assistanceParams = \"{}\"", toUnsignedLong(payload.deviceId), toUnsignedLong(payload.chkWord), encodeBase64String(payload.content));
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(uriBase + apiVer + "/" + api)
                .queryParam("payload", payload);
        restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
        uri = builder.toUriString();
        log.info("Request forwarded to: {}", uri);
        responseEntity = restTemplate.postForEntity(uri, requestEntity, responseType);
        if(responseType == byte[].class){
            strResponse = encodeBase64String((byte[])responseEntity.getBody());
        }else{
            T response = responseEntity.getBody();
            strResponse = response.toString();
            try{
                strResponse = objectMapper.writeValueAsString(response);
            }catch(JsonProcessingException e){
                log.error("Error: {}", e);
            }
        }
        log.info("Received answer: status: {}, payload: {}", responseEntity.getStatusCode(), strResponse);
        return responseEntity;
    }

	public GnssServiceResponse remoteApi(String apiVer, String api, byte[] payloadContent, String clientIpAddr){
		ResponseEntity<byte[]> responseEntity = remoteApi(apiVer, api, payloadContent, clientIpAddr, byte[].class);

		return new GnssServiceResponse(responseEntity.getStatusCode(), responseEntity.getBody());
	}
}
