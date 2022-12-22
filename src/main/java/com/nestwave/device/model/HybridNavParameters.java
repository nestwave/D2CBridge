package com.nestwave.device.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import static java.lang.Byte.toUnsignedInt;

@Data
public class HybridNavParameters{
	@Schema(description = "Cellular network type")
	public String radioType;

	@Schema(description = "4G cell information data as sent by the Iot device")
	public SingleCellInfo[] cellTowers;

	@Schema(description = "Wi-Fi cell information data as sent by the Iot device")
	public SingleWifiInfo[] wifiAccessPoints;

	@Schema(description = "Bluetooth cell information data as sent by the Iot device")
	public SingleBluetoothInfo[] bluetoothBeacons;

	public HybridNavParameters(HybridNavPayload hybridNavigationPayload){
		for(HybridNavMessage message : hybridNavigationPayload.messages){
			if(message instanceof CellInfo){
				this.radioType = "LTE";
				this.cellTowers = ((CellInfo) message).cellInfo;
			}else if(message instanceof WifiInfo){
				this.wifiAccessPoints = ((WifiInfo) message).wifiInfo;
			}else if(message instanceof BluetoothInfo){
				this.bluetoothBeacons = ((BluetoothInfo) message).bluetoothInfo;
			}
		}
	}
}

@Data
class SingleCellInfo{
	static int size = 12; /* C struct size */
	int cellId; /* uint32 ==> 4B */
	int mcc; /* uint16 ==> 2B */
	int mnc; /* uint16 ==> 2B */
	int lac; /* uint16 ==> 2B */
	int rsrp; /* int16 ==> 2B */

	SingleCellInfo(byte[] payload){
		assert payload.length == size;
		cellId = toUnsignedInt(payload[0]) + (toUnsignedInt(payload[1]) << 8) + (toUnsignedInt(payload[2]) << 16) + (toUnsignedInt(payload[3]) << 24);
		mcc = toUnsignedInt(payload[4]) + (toUnsignedInt(payload[5]) << 8);
		mnc = toUnsignedInt(payload[6]) + (toUnsignedInt(payload[7]) << 8);
		lac = toUnsignedInt(payload[8]) + (toUnsignedInt(payload[9]) << 8);
		rsrp = toUnsignedInt(payload[10]) + (toUnsignedInt(payload[11]) << 8) - (1 << 16);
	}
}

@Data
class MacAddress48{
	static int size = 6; /* C struct size */
	int[] v; /* uint8[6] ==> 6B */

	MacAddress48(byte[] payload){
		v = new int[size];
		for(int n = 0; n < size; n += 1){
			v[n] = toUnsignedInt(payload[n]);
		}
	}
}

@Data
class SingleWifiInfo{
	static int size = MacAddress48.size + 2; /* C struct size */
	MacAddress48 mac;
	int rssi; /* int16 ==> 2B */

	SingleWifiInfo(byte[] payload){
		assert payload.length == size;
		mac = new MacAddress48(payload);
		rssi = toUnsignedInt(payload[MacAddress48.size]) + (toUnsignedInt(payload[MacAddress48.size + 1]) << 8) - (1 << 16);
	}
}

@Data
class SingleBluetoothInfo{
	static int size = MacAddress48.size + 2; /* C struct size */
	MacAddress48 mac;
	int rssi; /* int16 ==> 2B */

	SingleBluetoothInfo(byte[] payload){
		assert payload.length == size;
		mac = new MacAddress48(payload);
		rssi = toUnsignedInt(payload[MacAddress48.size]) + (toUnsignedInt(payload[MacAddress48.size + 1]) << 8) - (1 << 16);
	}
}
