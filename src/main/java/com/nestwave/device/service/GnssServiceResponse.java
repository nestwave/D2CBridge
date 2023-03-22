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

import lombok.extern.slf4j.Slf4j;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.springframework.http.HttpStatus;

import static com.nestwave.device.util.GpsTime.getGpsTime;
import static java.lang.String.format;

@Slf4j
public class GnssServiceResponse {
	public final HttpStatus status;
	public final byte[] message;
	public final long gpsTime;

	public GnssServiceResponse(HttpStatus status, byte[] message, long gpsTime)
	{
		this.status = status;
		this.message = message;
		this.gpsTime = gpsTime;
	}

	public GnssServiceResponse(HttpStatus status, byte[] message)
	{
		this(status, message, getGpsTime());
	}

	public GnssServiceResponse(HttpStatus status, String message){
		this(status, message.getBytes());
		message = format("Rejected with code: %s.\n Error message: %s", status.toString(), message);
		log.error(message);
	}

	public ResponseCode getCoapStatus(){
		switch(status){
			case OK: return ResponseCode.VALID;
			case EXPECTATION_FAILED: return ResponseCode.BAD_OPTION;
			case GONE: return ResponseCode.CHANGED;
			case UPGRADE_REQUIRED: return ResponseCode.NOT_IMPLEMENTED;
			default: return ResponseCode.INTERNAL_SERVER_ERROR;
		}
	}
}
