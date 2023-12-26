
CREATE TABLE "thintrackPlatformBarometerStatus" (
    "ID" BigInt NOT NULL,
    "utcTime" timestamp with time zone NOT NULL,
    "barometerMeasurementsCount" INTEGER,
    "barometerMeasurementsAverage" float,
    "barometerMeasurementsVariance" float,
    "barometerMeasurementsMin" float,
    "barometerMeasurementsMax" float,
    "barometerTemperature" float,
    PRIMARY KEY ("ID", "utcTime")
);