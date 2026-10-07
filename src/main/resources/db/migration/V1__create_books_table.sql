CREATE TABLE books (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(32) NOT NULL,
    published_year INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    borrowed_by VARCHAR(100),
    borrowed_at TIMESTAMP WITH TIME ZONE,
    created_date TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_books_isbn UNIQUE (isbn)
);

CREATE INDEX idx_books_status ON books (status);
CREATE INDEX idx_books_title ON books (title);
CREATE INDEX idx_books_author ON books (author);
