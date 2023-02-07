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
package com.nestwave.device.resource;

import com.nestwave.device.DeviceApplication;
import com.nestwave.device.config.DeviceConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.http.HttpServletRequest;

import static com.nestwave.device.util.GpsTime.getGpsTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DeviceApplication.class)
@ContextConfiguration(classes = DeviceConfig.class)
@WebAppConfiguration()
class AssistanceResourceTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private WebApplicationContext wac;

    @Mock
    HttpServletRequest request;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    public void should_return_approximate_position() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/v1/gpsApproxPos").header("X-Forwarded-For","178.208.16.92"))
                .andReturn();
        assertEquals(result.getResponse().getContentAsString(), "[48.86956,2.34374,100.0]");
    }

    @Test
    public void should_return_error_when_parse_error() throws Exception {
        Assertions.assertThrows(org.springframework.web.util.NestedServletException.class, () -> {
            mockMvc.perform(MockMvcRequestBuilders
                    .post("/v1/gpsApproxPos").header("X-Forwarded-For","dddddddd"))
                    .andReturn();
        });
    }

    @Test
    public void should_return_gps_time() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/v1/gpsTime").header("X-Forwarded-For","178.208.16.92"))
                .andReturn();
        assertEquals("" + getGpsTime(), result.getResponse().getContentAsString());
    }

}
