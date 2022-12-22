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
package com.nestwave.device.coap.server;

import com.nestwave.device.coap.resource.CoapAssistanceResource;
import com.nestwave.device.coap.resource.CoapNavigationResource;
import org.eclipse.californium.core.coap.Request;
import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.server.ServerMessageDeliverer;
import org.eclipse.californium.core.server.resources.Resource;

import java.util.List;

public class ServerSideMessageDeliverer extends ServerMessageDeliverer {
    private final CoapNavigationResource coapNavigationResource;
    private final CoapAssistanceResource coapAssistanceResource;

    public ServerSideMessageDeliverer(Resource root,
                                      CoapNavigationResource coapNavigationResource,
                                      CoapAssistanceResource coapAssistanceResource) {
        super(root);
        this.coapNavigationResource = coapNavigationResource;
        this.coapAssistanceResource = coapAssistanceResource;
    }

    protected Resource findResource(Exchange exchange) {
        Request request = exchange.getRequest();
        List<String> path = request.getOptions().getUriPath();

        if (path.size() > 1) {
            String redirectPathName = path.get(1);
            if (redirectPathName != null && !redirectPathName.isEmpty()) {
                switch(redirectPathName){
					case "gpsTime": return coapAssistanceResource.new GpsTime();
					case "gpsApproxPos": return coapAssistanceResource.new GpsApproximatePosition();
					case "gnssAssistance": return coapAssistanceResource.new GnssAssistance();
	                case "gnssDevicePosition": return coapNavigationResource.new NavigationResource(false);
	                case "gnssPosition": return coapNavigationResource.new NavigationResource(true);
	                case "locate": return coapNavigationResource.new HybridNavigationResource();
                }
            }
        }
        return super.findResource(exchange);
    }
}
