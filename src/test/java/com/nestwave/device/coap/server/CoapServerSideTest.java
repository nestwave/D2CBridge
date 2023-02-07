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

import com.nestwave.device.DeviceApplication;
import com.nestwave.device.config.DeviceConfig;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DeviceApplication.class)
@ContextConfiguration(classes = DeviceConfig.class)
@Slf4j
class CoapServerSideTest {
    @Test
    public void should_call_coap_server(){

       // when(clockService.getGpsClockFileByDate(any())).thenReturn(getClock());
        //CoapClient client = new CoapClient("coap://loclahost:5683/gpsPosition?gpsPositionTime=1211997625");
        CoapClient client = new CoapClient("coap://aidefix.nestwave.com:5683/gpsClockParams?time=1211997625");
        //String paylod = "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";
        //Request request = new Request(CoAP.Code.POST);
        //request.setURI("coap://localhost/gpsPosition?gpsPositionTime=1211997625");
        //request.setPayload(paylod);
        CoapResponse response = null;
        try {
            //response = client.post(paylod.getBytes(StandardCharsets.UTF_8),1);
            response = client.get();
        } catch (ConnectorException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (response!=null) {
            System.out.println("##################################################################");
            System.out.println( response.getCode());
            System.out.println( response.getOptions());
            System.out.println( response.getResponseText());
            System.out.println("##################################################################");
        } else {
            System.out.println("Request failed");
        }
    }

    @Test
    public void test_coap_navigation_ss(){

            //CoapClient client = new CoapClient("coap://aidefix.nestwave.com:5683/ionosphereParams?time=1211997625");
            CoapClient client = new CoapClient("coap://localhost:5683/gpsPosition?gpsPositionTime=1211997625");
            String paylod = "HdgccwoACADL/wcACAAAAAAAiwTyZv9HAAAKApUEGwDs//oACgAAAAAA4TLPxt5AAAA0gLP6EgAgAB4GCQAAAAAAc5N4QtIsAADTC1wLCgDg/xAECwAAAAAAOHuDEtU8AABaoRb8CwAZAKUHDQAAAAAAhTeDvf4cAABBEY8OAQDc/7UBEAAAAAAAAEJplC8TAAD2BHMOIADu/2cDEwAAAAAAYF4CdT4ZAAAm7/0MFgAXAFEFEwAAAAAAoVx6zJQEAABHi58QHACd/38FAQABAAAA8HSeQvsJAAA19jINFABJAGMGAAABAAAAeD4qy3QBAADIx271AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAi2xPSEAAAACABEUDQAAAAAAAgFRAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAZAMAAAAAAAAAAAAACgKVBOEB";
            //Request request = new Request(CoAP.Code.POST);
            //request.setURI("coap://localhost/gpsPosition?gpsPositionTime=1211997625");
            //request.setPayload(paylod);
            CoapResponse response = null;
            try {
                //response = client.get();
                //response = client.post(paylod.getBytes(StandardCharsets.UTF_8),1);
                response = client.post(paylod,1);
            } catch (ConnectorException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (response!=null) {
                System.out.println("##################################################################");
                System.out.println( response.getCode());
                System.out.println( response.getOptions());
                System.out.println( response.getResponseText());
                System.out.println("##################################################################");
            } else {
                System.out.println("Request failed");
            }
        }

}
