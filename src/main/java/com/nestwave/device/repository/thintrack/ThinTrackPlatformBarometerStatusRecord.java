package com.nestwave.device.repository.thintrack;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.nio.ByteBuffer;
import java.time.ZonedDateTime;

import static java.lang.Byte.toUnsignedInt;
import static java.nio.ByteOrder.LITTLE_ENDIAN;
import static java.util.Arrays.copyOfRange;
import static org.springframework.beans.BeanUtils.copyProperties;

@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "\"thintrackPlatformBarometerStatus\"")
public class ThinTrackPlatformBarometerStatusRecord extends ThinTrackPlatformStatusRecord{
	static int size = ThinTrackPlatformStatusRecord.size + 21; /* C struct size */
	static int tag = 0xBEBF; /* uint16 ==> 2B */
	public static String platformStatusDisplayColumns = ThinTrackPlatformStatusRecord.platformStatusDisplayColumns +
			",Baro Meas Count, Baro Meas Average[Pa], Baro Meas Variance, Baro Meas Minimum[Pa], Baro Meas Maximum[Pa], Baro Temperature[°C]";

	@JsonProperty("count")
	@Column(name = "\"barometerMeasurementsCount\"")
	int barometerMeasurementsCount; /* uint8 ==> 1B */

	@JsonProperty("average")
	@Column(name = "\"barometerMeasurementsAverage\"")
	float barometerMeasurementsAverage; /* float ==> 4B */

	@JsonProperty("variance")
	@Column(name = "\"barometerMeasurementsVariance\"")
	float barometerMeasurementsVariance; /* float ==> 4B */

	@JsonProperty("min")
	@Column(name = "\"barometerMeasurementsMin\"")
	float barometerMeasurementsMin; /* float ==> 4B */

	@JsonProperty("max")
	@Column(name = "\"barometerMeasurementsMax\"")
	float barometerMeasurementsMax; /* float ==> 4B */

	@JsonProperty("temperature")
	@Column(name = "\"barometerTemperature\"")
	float barometerMeasurementsTemperature; /* float ==> 4B */

	public ThinTrackPlatformBarometerStatusRecord(long deviceId, ZonedDateTime utcTime, byte[] data){
		super(deviceId, utcTime, data);
		data = copyOfRange(data, ThinTrackPlatformStatusRecord.size, size);
		barometerMeasurementsCount = data[0];
		barometerMeasurementsAverage = ByteBuffer.wrap(data,1, 4).order(LITTLE_ENDIAN).getFloat();
		barometerMeasurementsVariance = ByteBuffer.wrap(data,5, 4).order(LITTLE_ENDIAN).getFloat();
		barometerMeasurementsMin = ByteBuffer.wrap(data,9, 4).order(LITTLE_ENDIAN).getFloat();
		barometerMeasurementsMax = ByteBuffer.wrap(data,13, 4).order(LITTLE_ENDIAN).getFloat();
		barometerMeasurementsTemperature = ByteBuffer.wrap(data,17, 4).order(LITTLE_ENDIAN).getFloat();
	}

	static boolean isValid(byte[] data){
		int dataTag = toUnsignedInt(data[0]) + (toUnsignedInt(data[1]) << 8);

		return (size == data.length) && (tag == dataTag);
	}

}
