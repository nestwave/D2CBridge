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
import static com.nestwave.device.repository.position.PositionRecord.positionDisplayColumns19;
import static com.nestwave.device.repository.thintrack.ThinTrackPlatformBarometerStatusRecord.platformStatusDisplayColumns;
import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;

@Repository
public class ThinTrackPlatformStatusRepository{
	private final ThinTrackPlatformStatusJpaRepository thintrackPlatformStatusJpaRepository;
	private final ThinTrackPlatformBarometerStatusJpaRepository thintrackPlatformBarometerStatusJpaRepository;

	public ThinTrackPlatformStatusRepository(ThinTrackPlatformStatusJpaRepository thintrackPlatformStatusJpaRepository, ThinTrackPlatformBarometerStatusJpaRepository thintrackPlatformBarometerStatusJpaRepository)
	{
		this.thintrackPlatformStatusJpaRepository = thintrackPlatformStatusJpaRepository;
		this.thintrackPlatformBarometerStatusJpaRepository = thintrackPlatformBarometerStatusJpaRepository;
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

	public ThinTrackPlatformBarometerStatusRecord insertNewRecord(ThinTrackPlatformBarometerStatusRecord thinTrackPlatformBarometerStatusRecord)
	{
		Optional<ThinTrackPlatformBarometerStatusRecord> oldPositionRecord = thintrackPlatformBarometerStatusJpaRepository.findByKey(thinTrackPlatformBarometerStatusRecord.getKey());
		if(oldPositionRecord.isPresent()){
			thintrackPlatformBarometerStatusJpaRepository.delete(oldPositionRecord.get());
		}
		thintrackPlatformBarometerStatusJpaRepository.save(thinTrackPlatformBarometerStatusRecord);
		return thinTrackPlatformBarometerStatusRecord;
	}

	public List<ThinTrackPlatformStatusRecord> findAllRecordsById(long id)
	{
		return thintrackPlatformStatusJpaRepository.findAllByKeyIdOrderByKeyUtcTimeAsc(id);
	}

	public List<ThinTrackPlatformBarometerStatusRecord> findAllBaroRecordsById(long id)
	{

		return thintrackPlatformBarometerStatusJpaRepository.findAllByKeyIdOrderByKeyUtcTimeAsc(id);
	}

	public void dropAllRecordsWithId(long id){
		thintrackPlatformStatusJpaRepository.deleteByKeyId(id);
	}

	public String getAllRecordsWithId(long id, List<PositionRecord> positionRecords, String apiVer){
		StringBuilder csv = new StringBuilder();
		List<ThinTrackPlatformStatusRecord> statusRecords = findAllRecordsById(id);
		List<ThinTrackPlatformBarometerStatusRecord> baroRecords = findAllBaroRecordsById(id);
		String columnNames;

		if(apiVer.compareTo("v1.91") >= 0){
		 	columnNames = positionDisplayColumns19;
		} else {
			columnNames = positionDisplayColumns;
		}
		if(!statusRecords.isEmpty()){
			csv.append(columnNames+"," + platformStatusDisplayColumns + "\n");
		}else{
			csv.append(columnNames+"\n");
		}
		for(PositionRecord position : positionRecords){
			csv.append(format("%f,%f,%f,%f,%f,%s", position.getLon(), position.getLat(), position.getAlt(),
					position.getSpeed(), position.getConfidence(),
					position.getKey().getUtcTime().format(ISO_ZONED_DATE_TIME)
			));
			if(apiVer.compareTo("v1.91") >= 0){
				csv.append(format(",%f", position.getHat()));
			}
			for(ThinTrackPlatformStatusRecord statusRecord : statusRecords){
				if(position.getKey().equals(statusRecord.getKey())){
					csv.append(format(",%d,%d,%d,%d", statusRecord.ambientTemperature, statusRecord.batteryTemperature,
							statusRecord.batteryChargeLevel, statusRecord.shocksCount
					));
				}
			}
			for(ThinTrackPlatformBarometerStatusRecord baroRecord:baroRecords){
				if(position.getKey().equals(baroRecord.getKey())){
					csv.append(format(",%d,%f,%f,%f,%f,%f", baroRecord.barometerMeasurementsCount, baroRecord.barometerMeasurementsAverage,
							baroRecord.barometerMeasurementsVariance, baroRecord.barometerMeasurementsMin, baroRecord.barometerMeasurementsMax, baroRecord.barometerMeasurementsTemperature
					));
				}
			}
			csv.append('\n');
		}
		return csv.toString();
	}
}
