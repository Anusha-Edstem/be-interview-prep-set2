CREATE TABLE expenses (
    id UUID PRIMARY KEY,
    amount NUMERIC(12, 2) NOT NULL,
    category VARCHAR(20) NOT NULL,
    spent_on DATE NOT NULL,
    note VARCHAR(500),
    created_date TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT ck_expenses_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_expenses_spent_on ON expenses (spent_on);
CREATE INDEX idx_expenses_category_spent_on ON expenses (category, spent_on);
