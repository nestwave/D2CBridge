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

import com.nestwave.device.resource.EndPoint;
import com.nestwave.device.service.AssistanceService;
import com.nestwave.device.service.GnssServiceResponse;
import com.nestwave.device.service.NavigationService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.springframework.stereotype.Component;

import static com.nestwave.device.util.ApiUtil.getClientIpAddr;

@Slf4j
@Component
public class CoapNavigationResource extends EndPoint{
    public CoapNavigationResource(AssistanceService assistanceService, NavigationService navigationService){
        super(assistanceService, navigationService);
    }

    public class NavigationResource extends CoapResource {
        public final boolean noc;

        public NavigationResource(boolean noc){
            super("gnssPosition");
            this.noc = noc;
            getAttributes().setTitle("GET-GNSS-POSITION");
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            String apiVer = exchange.getRequestOptions().getUriPath().get(0);
			byte [] rawResults = exchange.getRequestPayload();
	        GnssServiceResponse response;

			response = gnssPosition(apiVer, rawResults, getClientIpAddr(exchange), noc);
			exchange.respond(response.getCoapStatus(), response.message);
        }
    }

	public class HybridNavigationResource extends CoapResource {
		public HybridNavigationResource(){
			super("locate");
			getAttributes().setTitle("LOCATE");
		}

		@Override
		public void handlePOST(CoapExchange exchange) {
			String apiVer = exchange.getRequestOptions().getUriPath().get(0);
			byte [] rawResults = exchange.getRequestPayload();
			GnssServiceResponse response;

			response = navigationService.locate(apiVer, rawResults, getClientIpAddr(exchange));
			exchange.respond(response.getCoapStatus(), response.message);
		}
	}
}
