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
package com.nestwave.model;

import lombok.extern.slf4j.Slf4j;

import static java.lang.Integer.parseInt;
import static java.lang.Integer.toUnsignedLong;
import static java.lang.Math.min;
import static java.nio.ByteBuffer.wrap;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.copyOfRange;

@Slf4j
public class Payload{
	public final long deviceId;
	public final byte[] content;
	public final int chkWord;
	public final boolean isValid;
	static private final int classMask = 0x888080C0;
	static private final int[] deviceIdShift = {32, 28, 24, 16, 8, 7, 6, 0};
	static private final int[] deviceIdMasks = {0xF0000000, 0x0F000000, 0x00FF0000, 0x0000FF00, 0x00000080, 0x00000040, 0x0000003F};
	static private final int N = deviceIdMasks.length;

	public Payload(byte[] payload)
	{
		this(payload, 8);
	}

	public Payload(byte[] payload, int deviceIdLen)
	{
		int fletcher32DataLength = payload.length - 4;
		int chkWordComputed;
		if(deviceIdLen == 8) {
			deviceId = wrap(copyOfRange(payload, 0, deviceIdLen)).order(LITTLE_ENDIAN).getLong();
		} else{
			deviceId = wrap(copyOfRange(payload, 0, deviceIdLen)).order(LITTLE_ENDIAN).getInt();
		}
		this.content = copyOfRange(payload, deviceIdLen, fletcher32DataLength);
		chkWord = wrap(copyOfRange(payload, fletcher32DataLength, payload.length)).order(LITTLE_ENDIAN).getInt();
		chkWordComputed = fletcher32(payload, fletcher32DataLength);
		isValid = chkWord == chkWordComputed;
		if(!isValid){
			log.error("Integrity check failed: expected: {}, computed: {}, payload.length: {}", toUnsignedLong(chkWord), toUnsignedLong(chkWordComputed), payload.length);
		}
	}

	public Payload(int deviceId, byte[] payload, int chkWord)
	{
		this.deviceId = deviceId;
		this.content = payload;
		this.chkWord = chkWord;
		isValid = true;
	}

	public static int fletcher32(byte[] data, int len)
	{
		int c0 = 0;
		int c1 = 0;
		int x = 0;
		int wQty = (len + 1) / 2;

		assert data.length >= len;
		while(wQty > 0){
			for(int i = 0; i < min(wQty, 360) - 1; i += 1){
				/* Byte stream was passed in Little Endian form. */
				c0 += (data[x] & 0xFF) | ((data[x + 1] & 0xFF) << 8);
				c1 += c0;
				x += 2;
			}
			/* Take care of buffer overflow if len was passed with an odd value. */
			c0 += (data[x] & 0xFF) | (x + 1 < len ? (data[x + 1] & 0xFF) << 8 : 0);
			c1 += c0;
			x += 2;
			c0 %= 65535;
			c1 %= 65535;
			wQty -= 360;
		}
		return c1 << 16 | c0;
	}

	public int customerId()
	{
		return isLegacy() ? customerId((int)deviceId) : (int)((deviceId >> 48) & 0xFFFFL);
	}

	public static int customerId(int deviceId){
		final int classIndicator = deviceId & classMask;

		for(int n = 0; n < N; n+= 1){
			if((classIndicator & deviceIdMasks[n]) == 0){
				return (int)(toUnsignedLong(deviceId) >> deviceIdShift[n + 1]);
			}
		}
		return 0;
	}

	public static int[] deviceId(int deviceId){
		final int classIndicator = deviceId & classMask;
		int[] devId = new int[N];
		long dId = toUnsignedLong(deviceId);

		for(int n = 0; n < N; n += 1){
			devId[n] = (int)((dId & deviceIdMasks[n]) >> deviceIdShift[n + 1]);
			if(n > 0 && (classIndicator & deviceIdMasks[n - 1]) == 0){
				devId[n] = (int)(dId & ((1 << deviceIdShift[n + 1]) - 1));
				return copyOf(devId, n + 1);
			}
		}
		return devId;
	}

	public static int deviceId(int[] devId){
		var deviceId = 0;

		if(devId.length == N){
			for(var i = 0; i < N; i += 1){
				deviceId = (deviceId << (4 * (1 + (i > 1 ? 1 : 0)))) +  devId[i];
			}
		}else{
			log.error("Invalid device ID: {}", devId);
		}
		return deviceId;
	}

	public static int deviceId(String... devId){
		var deviceId = 0;

		if(devId.length == 1){
			devId = devId[0].split("\\.");
		}
		if(devId.length == N){
			for(var i = 0; i < N; i += 1){
				deviceId = (deviceId << (4 * (1 + (i > 1 ? 1 : 0)))) +  parseInt(devId[i]);
			}
		}else{
			log.error("Invalid device ID: {}", devId);
		}
		return deviceId;
	}

	public boolean isLegacy()
	{
		return deviceId >> 32 == 0;
	}
}
