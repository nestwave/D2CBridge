/******************************************************************************
 * Copyright 2022 - NEXTNAV INC
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
package com.nestwave.device.repository.thintrack;

import com.nestwave.device.model.HybridNavPayload;

import static java.lang.Byte.toUnsignedInt;

public class ThinTrackPlatformStatusRecord{
	static int size = 7; /* C struct size */
	static int tag = 0xBEBE; /* uint16 ==> 2B */

	int batteryTemperature; /* int8 ==> 1B */
	int ambientTemperature; /* int8 ==> 1B */
	int batteryChargeLevel; /* int8 ==> 1B */
	int shocksCount; /* uint16 ==> 2B */

	public ThinTrackPlatformStatusRecord(byte[] data){
		batteryTemperature = data[2];
		ambientTemperature = data[3];
		batteryChargeLevel = data[4];
		shocksCount = toUnsignedInt(data[5]) + (toUnsignedInt(data[6]) << 8);
	}

	static boolean isValid(byte[] data){
		int dataTag = toUnsignedInt(data[0]) + (toUnsignedInt(data[1]) << 8);

		return (size == data.length) && (tag == dataTag);
	}

	public static ThinTrackPlatformStatusRecord[] of(HybridNavPayload hybridNavPayload){
		byte[][] dataList = hybridNavPayload.userData();
		int recordsCount = 0;
		ThinTrackPlatformStatusRecord[] records;

		for(byte[] data : dataList){
			if(isValid(data)){
				recordsCount += 1;
			}
		}
		records = new ThinTrackPlatformStatusRecord[recordsCount];
		recordsCount = 0;
		for(byte[] data : dataList){
			if(isValid(data)){
				records[recordsCount++] = new ThinTrackPlatformStatusRecord(data);
			}
		}
		return records;
	}

	public boolean save(){
		// TODO: Implement here save to DB operation
		return true;
	}
}
