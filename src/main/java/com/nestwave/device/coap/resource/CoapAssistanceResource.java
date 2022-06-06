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
package com.nestwave.device.coap.resource;

import com.nestwave.device.service.AssistanceService;
import com.nestwave.device.service.GnssServiceResponse;
import com.nestwave.device.util.ApiUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.json.ParseException;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

import static com.nestwave.device.util.ApiUtil.getClientIpAddr;
import static com.nestwave.device.util.GpsTime.getGpsTime;
import static org.eclipse.californium.core.coap.MediaTypeRegistry.APPLICATION_OCTET_STREAM;

@Slf4j
@Component
public class CoapAssistanceResource {
    private final AssistanceService assistanceService;
    private final RestTemplate restTemplate;

    public CoapAssistanceResource(AssistanceService assistanceService, RestTemplate restTemplate) {
        this.assistanceService = assistanceService;
        this.restTemplate = restTemplate;
    }

    public class GpsTime extends CoapResource {
        public GpsTime() {
            super("gpsTime");
            getAttributes().setTitle("GET-GPS-TIME");
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            log.info("COAP : Get gps time ");
            Long time = getGpsTime();
            String ipAddr = exchange.getSourceAddress().toString();
            log.info("Request: gpsTime, IP: " + ipAddr + ", result: " + time);
            exchange.respond(CoAP.ResponseCode.VALID, time.toString());
            log.info("COAP : End get gps time: " + time.toString());
        }
    }

    public class GpsApproximatePosition extends CoapResource {
        public GpsApproximatePosition() {
            super("gpsApproxPos");
            getAttributes().setTitle("GET-GPS-APPROXIMATE-POSITION");
        }

        @Override
        public void handlePOST(CoapExchange exchange) {

            String ipAddr = getClientIpAddr(exchange);
            URI uri = null;
            try {
                uri = ApiUtil.buildUri(ipAddr);
            } catch (URISyntaxException e){
                log.error("Unable to create URL. cause : " + e);
                buildResponse(exchange, CoAP.ResponseCode.NOT_ACCEPTABLE, "Unable to create URL");
            }
            String approxPos ="";
            try {
                // ToDo uri not null if
                approxPos = ApiUtil.buildApproximatePosition(uri, restTemplate);
                log.info("Request: gptApproxPos, IP: " + ipAddr + ", result: " + approxPos);
            } catch (ParseException e){
                log.error("Unable to parse position in string format. cause : " + e);
                buildResponse(exchange, CoAP.ResponseCode.NOT_ACCEPTABLE, "Unable to parse position in string format");
            }
            buildResponse(exchange, CoAP.ResponseCode.VALID, approxPos);
        }

        private void buildResponse(CoapExchange exchange, CoAP.ResponseCode responseCode, String msg){
            exchange.respond(responseCode, msg);
        }
    }

    public class GnssAssistance extends CoapResource {
        public GnssAssistance(){
            super("gnssAssistance");
            getAttributes().setTitle("GET-GNSS-ASSISTANCE");
        }

		@Override
		public void handlePOST(CoapExchange exchange) {
        	String apiVer = exchange.getRequestOptions().getUriPath().get(0);
        	byte [] assistanceParams = exchange.getRequestPayload();
			GnssServiceResponse response;

			response = assistanceService.remoteApi(apiVer, "gnssAssistance", assistanceParams, getClientIpAddr(exchange));
			exchange.respond(response.getCoapStatus(), response.message, APPLICATION_OCTET_STREAM);
		}
	}
}
