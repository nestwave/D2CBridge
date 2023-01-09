package com.nestwave.device.model;

import com.nestwave.model.Payload;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static com.nestwave.model.Payload.appendFletcher32;
import static java.lang.Byte.toUnsignedInt;
import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOf;

@Slf4j
public class HybridNavPayload{
	public final HybridNavMessage[] messages;

	public HybridNavPayload(Payload payload) throws InvalidHybridNavPayloadException{
		int N = 32;
		int L = payload.content.length;
		int n = 0;
		int l = 0;
		HybridNavMessage[] messages = new HybridNavMessage[N];
		HybridNavMessage message;

		while(l + 1 < L && n < N){
			message = HybridNavMessage.of(payload.content, l);
			if(message == null){
				throw new InvalidHybridNavPayloadException("Invalid message in received payload with deviceId = %d, payload = %s",  payload.deviceId, Base64.encodeBase64String(payload.content));
			}
			messages[n++] = message;
			l += HybridNavMessage.length(payload, l);
		}
		this.messages = copyOf(messages, n);
	}

	public byte[] rawMeas(){
		byte[] rawMeas;
		int len = 0;
		int index = 0;

		for(HybridNavMessage message : messages){
			if(message instanceof GnssData){
				index = 0;
				len = message.hybridHeader.length;
			}else if(message instanceof GnssMoreData){
				if(((GnssMoreData) message).index != index){
					return null;
				}
				len += message.hybridHeader.length;
			}
			index += 1;
		}
		rawMeas = new byte[len];
		len = 0;
		for(HybridNavMessage message : messages){
			if(message instanceof GnssData){
				int chunkLen = ((GnssData) message).rawMeas.length;
				arraycopy(((GnssData) message).rawMeas, 0, rawMeas, len, chunkLen);
				len += chunkLen;
			}else if(message instanceof GnssMoreData){
				int chunkLen = ((GnssMoreData) message).rawMeas.length;
				arraycopy(((GnssMoreData) message).rawMeas, 0, rawMeas, len, chunkLen);
				len += chunkLen;
			}
		}
		return rawMeas;
	}

	public byte[] addTechno(String technology, byte[] payload){
		byte[] responsePayload = new byte[payload.length - 4 + 1]; /* Remove Fletcher 32 and add techno */

		arraycopy(payload, 0, responsePayload, 1, responsePayload.length - 1);
		for(HybridNavMessage message : messages){
			try{
				String techno = (String) message.getClass().getField("technology").get(null);
				if(techno.equals(technology)){
					responsePayload[0] = (byte) message.hybridHeader.techno;
				}
			}catch(NoSuchFieldException e){
				log.error("Bad definition of class {}! Missing technology field.", message.getClass());
			}catch (IllegalAccessException e){
				log.error("Caught exception: {}", e.getMessage());
			}
		}
		responsePayload = appendFletcher32(responsePayload);
		return responsePayload;
	}
}

enum Techno{
	NONE(0, null),
	LPGNSS(1, GnssData.class),
	CELLID(2, CellInfo.class),
	WIFI(3, WifiInfo.class),
	BLUETOOTH(4, BluetoothInfo.class),
	LPGNSS_MORE(128, GnssMoreData.class),
	USER(255, UserData.class);
	final int value;
	public final Class messageClass;

	Techno(int value, Class ThinMessageClass){
		this.value = value;
		this.messageClass = ThinMessageClass;
	}

	static Techno of(byte value){
		return of(toUnsignedInt(value));
	}

	static Techno of(int value){
		for(Techno v : values()){
			if(v.value == value){
				return v;
			}
		}
		return NONE;
	}
}

class HybridHeader{
	static int size = 2; /* C struct size */
	int techno; /* uint8 ==> 1B */
	int length; /* uint8 ==> 1B */

	public HybridHeader(byte[] payload, int offset){
		techno = toUnsignedInt(payload[offset + 0]);
		length = toUnsignedInt(payload[offset + 1]);
	}
}

class HybridNavMessage{
	final HybridHeader hybridHeader;

	HybridNavMessage(byte[] payload, int offset){
		hybridHeader = new HybridHeader(payload, offset);
	}

	static int length(byte[] payload, int offset){
		int L = payload.length;
		int len = HybridHeader.size;

		if(offset + 1 < L){
			len += toUnsignedInt(payload[offset + 1]);
		}
		return len;
	}

	static int length(Payload payload, int offset){
		return length(payload.content, offset);
	}

	static HybridNavMessage of(byte[] payload, int offset) throws InvalidHybridNavPayloadException{
		Techno techno = Techno.of(payload[offset]);
		if(techno.messageClass != null){
			try{
				final Constructor messageClass = techno.messageClass.getConstructor(byte[].class, int.class);
				return (HybridNavMessage) messageClass.newInstance(payload, offset);
			}catch(InvocationTargetException e){
				if(e.getCause() instanceof InvalidHybridNavPayloadException){
					throw (InvalidHybridNavPayloadException)e.getCause();
				}else{
					throw new RuntimeException(e);
				}
			}catch(NoSuchMethodException e){
				throw new RuntimeException(e);
			}catch(InstantiationException e){
				throw new RuntimeException(e);
			}catch(IllegalAccessException e){
				throw new RuntimeException(e);
			}
		}
		return null;
	}
}

class GnssData extends HybridNavMessage{
	public static String technology = "GNSS";
	public final byte[] rawMeas;

	public GnssData(byte[] payload, int offset){
		super(payload, offset);
		rawMeas = new byte[hybridHeader.length];
		arraycopy(payload, offset + 2, rawMeas, 0, rawMeas.length);
	}
}

class UserData extends HybridNavMessage{
	public static String technology = "USER";
	final byte[] data;

	public UserData(byte[] payload, int offset){
		super(payload, offset);
		data = new byte[hybridHeader.length];
		arraycopy(payload, offset + 2, data, 0, data.length);
	}
}

class GnssMoreData extends HybridNavMessage{
	public static String technology = "MORE";
	final byte index;
	final byte[] rawMeas;

	public GnssMoreData(byte[] payload, int offset){
		super(payload, offset);
		rawMeas = new byte[hybridHeader.length];
		index = payload[offset + 2];
		arraycopy(payload, offset + 3, rawMeas, 0, rawMeas.length - 1);
	}
}

class CellInfo extends HybridNavMessage{
	public static String technology = "Cellular";
	SingleCellInfo[] cellInfo;

	public CellInfo(byte[] payload, int offset) throws InvalidHybridNavPayloadException{
		super(payload, offset);
		int N = hybridHeader.length / SingleCellInfo.size;
		if(hybridHeader.length != N * SingleCellInfo.size){
			throw new InvalidHybridNavPayloadException("Invalid inner structure size (alignement issue on SingleCellInfo?): got %d expected %d", hybridHeader.length, N * SingleCellInfo.size);
		}
		cellInfo = new SingleCellInfo[N];
		byte[] data = new byte[SingleCellInfo.size];
		for(int n = 0; n < N; n += 1){
			arraycopy(payload, offset + 2 + n * data.length, data, 0, data.length);
			cellInfo[n] = new SingleCellInfo(data);
		}
	}
}

class WifiInfo extends HybridNavMessage{
	public static String technology = "Wi-Fi";
	SingleWifiInfo[] wifiInfo;

	public WifiInfo(byte[] payload, int offset) throws InvalidHybridNavPayloadException{
		super(payload, offset);
		int N = hybridHeader.length / SingleWifiInfo.size;
		if(hybridHeader.length != N * SingleWifiInfo.size){
			throw new InvalidHybridNavPayloadException("Invalid inner structure size (alignement issue on SingleWifiInfo?): got %d expected %d", hybridHeader.length, N * SingleWifiInfo.size);
		}
		wifiInfo = new SingleWifiInfo[N];
		byte[] data = new byte[SingleWifiInfo.size];
		for(int n = 0; n < N; n += 1){
			arraycopy(payload, offset + 2 + n * data.length, data, 0, data.length);
			wifiInfo[n] = new SingleWifiInfo(data);
		}
	}
}

class BluetoothInfo extends HybridNavMessage{
	public static String technology = "Bluetooth";
	SingleBluetoothInfo[] bluetoothInfo;

	public BluetoothInfo(byte[] payload, int offset) throws InvalidHybridNavPayloadException{
		super(payload, offset);
		int N = hybridHeader.length / SingleBluetoothInfo.size;
		if(hybridHeader.length != N * SingleBluetoothInfo.size){
			throw new InvalidHybridNavPayloadException("Invalid inner structure size (alignement issue on SingleCellInfo?): got %d expected %d", hybridHeader.length, N * SingleBluetoothInfo.size);
		}
		bluetoothInfo = new SingleBluetoothInfo[N];
		byte[] data = new byte[SingleBluetoothInfo.size];
		for(int n = 0; n < N; n += 1){
			arraycopy(payload, offset + 2 + n * data.length, data, 0, data.length);
			bluetoothInfo[n] = new SingleBluetoothInfo(data);
		}
	}
}

class PlatformStatus extends HybridNavMessage{
	public static String technology = "Status";
	byte battery;
	byte batteryTemperature;
	byte ambientTemperature;

	public PlatformStatus(byte[] payload, int offset){
		super(payload, offset);
		assert hybridHeader.length == 3;
		battery = payload[offset + 3];
		batteryTemperature = payload[offset + 4];
		ambientTemperature = payload[offset + 5];
	}
}
