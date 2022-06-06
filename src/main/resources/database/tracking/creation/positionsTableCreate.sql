-- Create table for storing devices position.
CREATE TABLE positions (
    "ID" integer NOT NULL,
    "utcTime" timestamp with time zone NOT NULL,
    confidence real NOT NULL,
    lon real NOT NULL,
    lat real NOT NULL,
    alt real NOT NULL,
    "Vx" real,
    "Vy" real,
    "Vz" real,
	PRIMARY KEY ("ID", "utcTime")
);

-- Add description of table columns.
COMMENT ON COLUMN positions."ID" IS 'Device unique identifier. Mandatory';
COMMENT ON COLUMN positions."utcTime" IS 'Fix UTC time. Mandatory';
COMMENT ON COLUMN positions.confidence IS 'Confidence on the fix result.';
COMMENT ON COLUMN positions.lon IS 'Fix result longitude. Mandatory';
COMMENT ON COLUMN positions.lat IS 'Fix result latitude. Mandatory';
COMMENT ON COLUMN positions.alt IS 'Fix result altitude. Mandatory';
COMMENT ON COLUMN positions."Vx" IS 'Velocity in m/s along West to East direction.';
COMMENT ON COLUMN positions."Vy" IS 'Velocity in m/s along South to North direction.';
COMMENT ON COLUMN positions."Vz" IS 'Velocity in m/s along bottom to up direction.';
