package com.nestwave.service;

import org.jvnet.hk2.annotations.Service;

@Service
public interface SecretManager{
	String getSecret(String tag);
	boolean setSecret(String tag, String secret);
}
