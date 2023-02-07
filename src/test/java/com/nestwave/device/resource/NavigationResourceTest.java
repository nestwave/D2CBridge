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
import com.nestwave.device.service.NavigationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = DeviceApplication.class)
@ContextConfiguration(classes = DeviceConfig.class)
@WebAppConfiguration()
class NavigationResourceTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    NavigationResource navigationResource;

    @Autowired
    private WebApplicationContext wac;

    @MockBean
    private NavigationService navigationService;

    private MockMvc mockMvc;

    private LocalDateTime localDateTime1;

    @BeforeEach
    void setup() {
        localDateTime1 = LocalDateTime.now().minus(1, ChronoUnit.HOURS);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    void should_return_message_activated_off() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/v1/gpsPosition")
                .content("content"))
                .andReturn();
        String response = result.getResponse().getContentAsString();
        assertEquals("Get navigation of now is not already activated : You should add gpsPositionTime parameter", response);
    }

    private String getSecondsFromFirstJanuary1980UntilNow(){
        return ""+Math.abs(ChronoUnit.SECONDS.between(LocalDateTime.now(), LocalDateTime.of(1980, 1, 1,0,0,0)));
    }

    @Test
    void should_return_error_if_not_valid_parameter_navigation_of_date() throws Exception {

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders
                .post("/v1/gpsPosition")
                .param("gpsPositionTimei", "1211997625")
                .content("content")).andReturn();
        String response = result.getResponse().getContentAsString();
        assertEquals("Bad parameters in request not accepted", response);
    }

    private String getTimeWithoutNanoSeconds(String time) {
        return time.substring(0, time.indexOf("."));
    }

}
