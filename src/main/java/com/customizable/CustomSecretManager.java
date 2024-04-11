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
package com.customizable;

import com.nestwave.device.util.JwtTokenUtil;
import com.nestwave.service.SecretManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Path;

import static java.nio.file.Files.readString;
import static java.nio.file.Files.writeString;

@Slf4j
@Service
public class CustomSecretManager implements SecretManager{
	public final Path path;

	public CustomSecretManager(JwtTokenUtil jwtTokenUtil, @Value("${jwt.file}") String file){
		path = Path.of(file);
		jwtTokenUtil.register(this);
	}

	@Override
	public String getSecret(String tag){
		String secret = null;

		if(tag == "D2CB_JWT"){
			try{
				secret = readString(path);
			}catch(IOException e){
				log.error("Could not open {} for reading token.", path);
			}
		}
		return secret;
	}

	@Override
	public boolean setSecret(String tag, String secret){
		boolean status = false;

		if(tag == "D2CB_JWT"){
			try{
				writeString(path, secret);
				status = true;
			}catch(IOException e){
				log.error("Could not open {} for writing token.", path);
			}
		}
		return status;
	}
}
