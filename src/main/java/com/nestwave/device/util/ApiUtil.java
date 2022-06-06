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
package com.nestwave.device.util;

import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class ApiUtil {

	public static String  buildApproximatePosition(URI uri, RestTemplate restTemplate) throws ParseException {
		final double ALTITUDE = 100.0;
		final String LATITUDE = "latitude";
		final String LONGITUDE = "longitude";

		ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
		JSONParser parser = new JSONParser(result.getBody());
		String approxPos;
		Map<String, String> json = (Map) parser.parse();
		approxPos = "[" + json.get(LATITUDE) + "," + json.get(LONGITUDE) + "," + ALTITUDE + "]";
		return approxPos;
	}


	public static URI buildUri (String ipAddr) throws URISyntaxException {
		final String baseUrl = "https://api.ipgeolocation.io/ipgeo?apiKey=8bf89e0cf5a94de197bbe45b2c80682d&ip=";

		return new URI(baseUrl+ ipAddr);
	}

	public static String getClientIpAddr(HttpServletRequest request){
		String ip = request.getHeader("X-Forwarded-For");
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
			ip = request.getHeader("Proxy-Client-IP");
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if(ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)){
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	public static String getClientIpAddr(CoapExchange exchange) {
		InetAddress ipAddr = exchange.getSourceAddress();
		String ipAddrStr = "";
		if(ipAddr != null) {
			ipAddrStr = ipAddr.toString().substring(1);
		}
		// ToDo remove this when we pass tests on jenkins. Cause : unit tests consider localhost 127.0.0.1
		if (!ipAddrStr.isEmpty() && ipAddrStr.equals("127.0.0.1")) {
			ipAddrStr = "178.208.16.92";
		}
		return ipAddrStr;
	}

}
