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

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;

@Repository
public class PositionRepository{
	private final PositionJpaRepository positionJpaRepository;

	public PositionRepository(PositionJpaRepository positionJpaRepository)
	{
		this.positionJpaRepository = positionJpaRepository;
	}

	public PositionRecord insertNavigationRecord(PositionRecord positionRecord)
	{
		Optional<PositionRecord> oldPositionRecord = positionJpaRepository.findByKey(positionRecord.getKey());
		if(oldPositionRecord.isPresent()){
			positionJpaRepository.delete(oldPositionRecord.get());
		}
		positionJpaRepository.save(positionRecord);
		return positionRecord;
	}

	public List<PositionRecord> findAllPositionRecordsById(long id)
	{
		return positionJpaRepository.findAllByKeyIdOrderByKeyUtcTimeAsc(id);
	}

	public void dropAllPositionRecordsWithId(long id){
		positionJpaRepository.deleteByKeyId(id);
	}

	public String getAllPositionRecordsWithId(long id){
		StringBuilder csv = new StringBuilder();
		List<PositionRecord> positions = findAllPositionRecordsById(id);

		csv.append("Longitude[°],Latitude[°],Altitude[m],Speed[m/s],Confidence[m],Date & Time\n");
		for(PositionRecord position : positions){
			csv.append(format("%f,%f,%f,%f,%f,%s\n", position.getLon(), position.getLat(), position.getAlt(),
					position.getSpeed(), position.getConfidence(),
					position.getKey().getUtcTime().format(ISO_ZONED_DATE_TIME)
			));
		}
		return csv.toString();
	}
}
