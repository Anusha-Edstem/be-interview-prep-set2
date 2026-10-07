CREATE TABLE doctors (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    speciality VARCHAR(100) NOT NULL
);

CREATE TABLE appointments (
    id UUID PRIMARY KEY,
    doctor_id UUID NOT NULL,
    patient_id VARCHAR(100) NOT NULL,
    starts_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL,
    active_slot VARCHAR(120),
    held_until TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    confirmed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_appointments_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT uq_appointments_active_slot UNIQUE (active_slot)
);

CREATE INDEX idx_appointments_doctor_starts_at ON appointments (doctor_id, starts_at);
CREATE INDEX idx_appointments_held_until ON appointments (held_until);
