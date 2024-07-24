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
package com.nestwave.device;

import com.nestwave.device.coap.server.CoapServerSide;
import com.nestwave.device.config.DeviceConfig;
import com.nestwave.device.util.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootApplication
public class DeviceApplication implements CommandLineRunner {
	//HTTP port
	@Value("${server.http.port}")
	private int httpPort;
	private final CoapServerSide coapServerSide;
	private final DeviceConfig deviceConfig;
	final JwtTokenUtil jwtTokenUtil;
	final String url;
	final RestTemplate restTemplate;

	public DeviceApplication(CoapServerSide coapServerSide,
	                         DeviceConfig deviceConfig,
	                         JwtTokenUtil jwtTokenUtil,
	                         @Value("${jwt.url}") String url,
	                         RestTemplate restTemplate){
		this.coapServerSide = coapServerSide;
		this.deviceConfig = deviceConfig;
		this.jwtTokenUtil = jwtTokenUtil;
		this.url = url;
		this.restTemplate = restTemplate;
	}

	public static void main(String[] args) {
		SpringApplication.run(DeviceApplication.class, args);
	}


	// Let's configure additional connector to enable support for both HTTP and HTTPS
	@Bean
	public ServletWebServerFactory servletContainer() {
		TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory();
		tomcat.addAdditionalTomcatConnectors(createStandardConnector());
		return tomcat;
	}

	private Connector createStandardConnector() {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setPort(httpPort);
		return connector;
	}

	@Override
	public void run(String... args) throws Exception {
			coapServerSide.startServer();

			log.info("Start periodic update of the JWT.");
			deviceConfig
					.threadPoolTaskScheduler()
					.schedule(new JwtUpdateSchedulerService(jwtTokenUtil, url, restTemplate), initPeriodicTrigger());

	}

	private PeriodicTrigger initPeriodicTrigger(){
		PeriodicTrigger periodicTrigger
				= new PeriodicTrigger(TimeUnit.DAYS.toSeconds(jwtTokenUtil.period), TimeUnit.SECONDS);
		periodicTrigger.setFixedRate(true);
		periodicTrigger.setInitialDelay(120); /* Give enough time for AµS to start. */
		return periodicTrigger;
	}
}

@Slf4j
class JwtUpdateSchedulerService implements  Runnable{
	final JwtTokenUtil jwtTokenUtil;
	final String url;
	final RestTemplate restTemplate;

	public JwtUpdateSchedulerService(JwtTokenUtil jwtTokenUtil, String url, RestTemplate restTemplate){
		this.jwtTokenUtil = jwtTokenUtil;
		this.url = url;
		this.restTemplate = restTemplate;
	}

	public void run(){
		String secret = jwtRenew(jwtTokenUtil.getSecret());

		if(secret != null){
			log.info("Updating JWT from NextNavCloud was successful. Saving it into secret manager plugin.");
			jwtTokenUtil.jwtUpdate(secret);
		}else{
			log.error("Failed to update JWT from NextNav Cloud.");
		}
	}

	String jwtRenew(String secret){
		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + secret);
		HttpEntity<String> requestEntity = new HttpEntity<>("", headers);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
		String uri = builder.toUriString();

		log.info("Updating JWT from: {}", uri);
		ResponseEntity<JwtResponse> responseEntity = restTemplate.postForEntity(uri, requestEntity, JwtResponse.class);
		switch(responseEntity.getStatusCode()){
			case OK:
				JwtResponse jwtResponse = responseEntity.getBody();
				return jwtResponse.token;
			case UNAUTHORIZED:
				log.error("Current JWT seems to be expired. Please consider restarting this service after manually updating it.");
			default:
				log.error("Failed to update JWT. Remote server returned: {}", requestEntity.getBody());
				return null;
		}
	}
}

class JwtResponse{
	public String token;
}
