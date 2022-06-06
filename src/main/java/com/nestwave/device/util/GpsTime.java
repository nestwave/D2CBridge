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
package com.nestwave.device.util;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;

public class GpsTime {

  private static final long NANOS_IN_WEEK = TimeUnit.DAYS.toNanos(7);
  // GPS epoch is 1980/01/06
  private static final long GPS_DAYS_SINCE_JAVA_EPOCH = 3657;
  private static final long GPS_UTC_EPOCH_OFFSET_SECONDS = TimeUnit.DAYS.toSeconds(GPS_DAYS_SINCE_JAVA_EPOCH);

	private static final ZonedDateTime GPS_EPOCH = ZonedDateTime.of(1980, 1, 6, 0, 0, 0, 0, ZoneOffset.UTC);;
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

	public static long getGpsTime(){ return getGpsTime(ZonedDateTime.now(ZoneOffset.UTC)); }
	public static long getGpsTime(ZonedDateTime date){ return GPS_EPOCH.until(date, ChronoUnit.SECONDS); }

	public static LocalDateTime getGpsAssistanceTime(String gpsClockTime){
		long offset = Long.parseLong(gpsClockTime);
		return getUtcAssistanceTime(offset).toLocalDateTime();
	}

	public static ZonedDateTime getUtcAssistanceTime(long gpsClockTime){
		return GPS_EPOCH.plus(gpsClockTime, ChronoUnit.SECONDS);
	}

  /**
   * @return week count since GPS epoch, and second count since the beginning of
   *         that week.
   */
  public static Map<Integer, Integer> getGpsWeekSecond(DateTime dateTime) {
    // JAVA/UNIX epoch: January 1, 1970 in msec
    // GPS epoch: January 6, 1980 in second
    Map<Integer, Integer> map = new HashMap<>();
    long gpsNanos = TimeUnit.MILLISECONDS.toNanos(dateTime.getMillis())
        + TimeUnit.SECONDS.toNanos(/*getLeapSecond(dateTime)*/ - GPS_UTC_EPOCH_OFFSET_SECONDS);
    int week = (int) (gpsNanos / NANOS_IN_WEEK);
    int second = (int) TimeUnit.NANOSECONDS.toSeconds(gpsNanos % NANOS_IN_WEEK);
    map.put(week, second);
    return map;
  }

}
