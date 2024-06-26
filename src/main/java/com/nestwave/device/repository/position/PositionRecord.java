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
package com.nestwave.device.repository.position;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.nestwave.device.repository.CompositeKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

import static java.lang.Math.sqrt;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "positions")
public class PositionRecord{
	public static String positionDisplayColumns = "Longitude[°],Latitude[°],Altitude[m],Speed[m/s],Confidence[m],Date & Time";
	public static String positionDisplayColumns19 = "Longitude[°],Latitude[°],Altitude[m],Speed[m/s],Confidence[m],Date & Time,Height Above Terrain[m]";
	@EmbeddedId
	CompositeKey key;

	@NotNull
	private float confidence;

	@NotNull
	private float lon;

	@NotNull
	private float lat;

	@NotNull
	private float alt;

	private Float hat;

	@NotNull
	@Column(name = "\"Vx\"")
	private float Vx;

	@NotNull
	@Column(name = "\"Vy\"")
	private float Vy;

	@NotNull
	@Column(name = "\"Vz\"")
	private float Vz;

	public PositionRecord(long id, ZonedDateTime utcTime, float confidence, float lon, float lat, float alt, Float hat, float Vx, float Vy, float Vz)
	{
		CompositeKey key = new CompositeKey(id, utcTime);
		this.key = key;
		this.lon = lon;
		this.lat = lat;
		this.alt = alt;
		this.hat = hat;
		this.Vx = Vx;
		this.Vy = Vy;
		this.Vz = Vz;
		this.confidence = confidence;
	}

	public double getSpeed()
	{
		return sqrt(Vx * Vx + Vy * Vy + Vz * Vz);
	}
}
