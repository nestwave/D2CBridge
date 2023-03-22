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

import com.nestwave.device.repository.position.PositionRepository;
import com.nestwave.device.repository.thintrack.ThinTrackPlatformStatusRepository;
import com.nestwave.device.util.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@WebAppConfiguration()
@DirtiesContext
class NavigationServiceTest {

  private final static int MAX_SIZE_ARRAY_OUTPUT_POSITION_AFTER = 12000;

	@Value("${navigation.jwt.file}")
	String file;
    @Value("${navigation.jwt.period}")
    int period;

  @Value("${navigation.base_url}")
  String navigationUrl="http://navigation:8088/navigation/";

  @Value("${position.directory}")
  String scriptDirectory="/opt/app/gpsNavAppTest";

  @Value("${position.executable}")
  String positionExecutable="./gpsNavAppTest";

  @Mock
  AssistanceService assistanceService;

  @Mock
  private RestTemplate restTemplate= mock(RestTemplate.class);

	@Mock
    PositionRepository navigationRepository;

	@Mock
	ThinTrackPlatformStatusRepository thinTrackPlatformStatusRepository;

  @InjectMocks
  NavigationService navigationService;

  @BeforeEach
  public void init() {
    navigationService = new NavigationService(new JwtTokenUtil(file, period),
		                                      navigationRepository,
		                                      thinTrackPlatformStatusRepository,
                                              navigationUrl, null, restTemplate);
  }
}
