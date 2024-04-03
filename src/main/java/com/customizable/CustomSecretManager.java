package com.customizable;

import com.nestwave.device.util.JwtTokenUtil;
import com.nestwave.service.SecretManager;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;
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
