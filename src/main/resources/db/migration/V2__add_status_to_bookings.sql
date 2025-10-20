-- Add with a default so existing rows get a value
ALTER TABLE bookings
  ADD COLUMN status varchar(255) DEFAULT 'CONFIRMED';

-- Make it required
ALTER TABLE bookings
  ALTER COLUMN status SET NOT NULL;

-- Optional: drop default so the app must set it explicitly
ALTER TABLE bookings
  ALTER COLUMN status DROP DEFAULT;

-- Enforce allowed values
ALTER TABLE bookings
  ADD CONSTRAINT bookings_status_check
  CHECK (status IN ('CONFIRMED','WAITLISTED','CANCELLED'));
