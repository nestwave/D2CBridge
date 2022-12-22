package com.nestwave.device.model;

import com.nestwave.model.Payload;
import org.apache.tomcat.util.codec.binary.Base64;

import static java.lang.String.format;

public class InvalidHybridNavPayloadException extends Exception{
	public InvalidHybridNavPayloadException(String message, Object... varargs){
		super(format(message, varargs));
	}
}
