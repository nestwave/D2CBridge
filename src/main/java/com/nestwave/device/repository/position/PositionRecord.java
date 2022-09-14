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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.ZonedDateTime;

import static java.lang.Math.sqrt;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "positions")
public class PositionRecord{
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

	@NotNull
	@Column(name = "\"Vx\"")
	private float Vx;

	@NotNull
	@Column(name = "\"Vy\"")
	private float Vy;

	@NotNull
	@Column(name = "\"Vz\"")
	private float Vz;

	public PositionRecord(long id, ZonedDateTime utcTime, float confidence, float lon, float lat, float alt, float Vx, float Vy, float Vz)
	{
		CompositeKey key = new CompositeKey(id, utcTime);
		this.key = key;
		this.lon = lon;
		this.lat = lat;
		this.alt = alt;
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

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Embeddable
class CompositeKey implements Serializable{
	@NotNull
	@Column(name = "\"ID\"")
	private long id;

	@NotNull
	@Column(name = "\"utcTime\"")
	private ZonedDateTime utcTime;
}
