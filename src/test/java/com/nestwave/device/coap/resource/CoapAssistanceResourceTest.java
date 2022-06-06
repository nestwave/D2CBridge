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

import com.nestwave.device.DeviceApplication;
import com.nestwave.device.config.DeviceConfig;
import com.nestwave.device.resource.AssistanceResource;
import com.nestwave.device.service.AssistanceService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.elements.exception.ConnectorException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static com.nestwave.device.util.GpsTime.getGpsTime;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DeviceApplication.class)
@ContextConfiguration(classes = DeviceConfig.class)
@Slf4j
class CoapAssistanceResourceTest {


    @Test
    public void should_return_gps_time_when_call_assistance_resource_coap_server() {
        CoapClient client = new CoapClient("coap://localhost:5683/v1/gpsTime");
        CoapResponse response = null;
        try {
            response = client.post("",1);
        } catch (ConnectorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response!=null) {
            String responseTxt = response.getResponseText();
            assertEquals(responseTxt, "" + getGpsTime());
        } else {
            log.error("Request failed");
        }
    }

    @Test
    public void should_return_gps_approximate_position_when_call_assistance_resource_coap_server() {
        CoapClient client = new CoapClient("coap://localhost:5683/v1/gpsApproxPos");
        CoapResponse response = null;
        try {
            response = client.post("",1);
        } catch (ConnectorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response!=null) {
            String responseTxt = response.getResponseText();
            assertEquals(responseTxt, "[48.86956,2.34374,100.0]");
        } else {
            log.error("Request failed");
        }
    }

}