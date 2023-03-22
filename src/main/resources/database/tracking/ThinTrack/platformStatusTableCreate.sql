-- Create table for storing ThinTrack platofrm status information.
CREATE TABLE "thintrackPlatformStatus" (
    "ID" BigInt NOT NULL,
    "utcTime" timestamp with time zone NOT NULL,
    "batteryTemperature" Integer,
    "ambientTemperature" Integer,
    "batteryChargeLevel" Integer,
    "shocksCount" Integer,
	PRIMARY KEY ("ID", "utcTime")
);

-- Add description of table columns.
COMMENT ON COLUMN "thintrackPlatformStatus"."ID" IS 'Device unique identifier. Mandatory';
COMMENT ON COLUMN "thintrackPlatformStatus"."utcTime" IS 'Fix UTC time. Mandatory';
COMMENT ON COLUMN "thintrackPlatformStatus"."batteryTemperature" IS 'Battery temperature in [-128..127].';
COMMENT ON COLUMN "thintrackPlatformStatus"."ambientTemperature" IS 'Ambiant temperature in [-128..127].';
COMMENT ON COLUMN "thintrackPlatformStatus"."batteryChargeLevel" IS 'Battery change level in [0..100].';
COMMENT ON COLUMN "thintrackPlatformStatus"."shocksCount" IS 'Number of chocks in [0..65535].';
