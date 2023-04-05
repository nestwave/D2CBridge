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

import com.nestwave.device.repository.position.PositionRecord;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.nestwave.device.repository.position.PositionRecord.positionDisplayColumns;
import static com.nestwave.device.repository.thintrack.ThinTrackPlatformStatusRecord.platformStatusDisplayColumns;
import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;

@Repository
public class ThinTrackPlatformStatusRepository{
	private final ThinTrackPlatformStatusJpaRepository thintrackPlatformStatusJpaRepository;

	public ThinTrackPlatformStatusRepository(ThinTrackPlatformStatusJpaRepository thintrackPlatformStatusJpaRepository)
	{
		this.thintrackPlatformStatusJpaRepository = thintrackPlatformStatusJpaRepository;
	}

	public ThinTrackPlatformStatusRecord insertNewRecord(ThinTrackPlatformStatusRecord thintrackPlatformStatusRecord)
	{
		Optional<ThinTrackPlatformStatusRecord> oldPositionRecord = thintrackPlatformStatusJpaRepository.findByKey(thintrackPlatformStatusRecord.getKey());
		if(oldPositionRecord.isPresent()){
			thintrackPlatformStatusJpaRepository.delete(oldPositionRecord.get());
		}
		thintrackPlatformStatusJpaRepository.save(thintrackPlatformStatusRecord);
		return thintrackPlatformStatusRecord;
	}

	public List<ThinTrackPlatformStatusRecord> findAllRecordsById(long id)
	{
		return thintrackPlatformStatusJpaRepository.findAllByKeyIdOrderByKeyUtcTimeAsc(id);
	}

	public void dropAllRecordsWithId(long id){
		thintrackPlatformStatusJpaRepository.deleteByKeyId(id);
	}

	public String getAllRecordsWithId(long id, List<PositionRecord> positionRecords){
		StringBuilder csv = new StringBuilder();
		List<ThinTrackPlatformStatusRecord> statusRecords = findAllRecordsById(id);

		if(statusRecords.isEmpty()){
			csv.append(positionDisplayColumns + "\n");
		}else{
			csv.append(positionDisplayColumns + "," + platformStatusDisplayColumns + "\n");
		}
		for(PositionRecord position : positionRecords){
			csv.append(format("%f,%f,%f,%f,%f,%s", position.getLon(), position.getLat(), position.getAlt(),
					position.getSpeed(), position.getConfidence(),
					position.getKey().getUtcTime().format(ISO_ZONED_DATE_TIME)
			));
			for(ThinTrackPlatformStatusRecord statusRecord : statusRecords){
				if(position.getKey().equals(statusRecord.getKey())){
					csv.append(format(",%d,%d,%d,%d", statusRecord.ambientTemperature, statusRecord.batteryTemperature,
							statusRecord.batteryChargeLevel, statusRecord.shocksCount
					));
				}
			}
			csv.append('\n');
		}
		return csv.toString();
	}
}
