CREATE TABLE tickets (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         subject VARCHAR(200) NOT NULL,
                         message TEXT NOT NULL,
                         category VARCHAR(20) NOT NULL,
                         priority VARCHAR(20) NOT NULL,
                         status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
                         user_id UUID NOT NULL,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT fk_tickets_user
                             FOREIGN KEY (user_id)
                                 REFERENCES users(id)
                                 ON DELETE CASCADE,

                         CONSTRAINT chk_tickets_category
                             CHECK (category IN ('TECHNICAL', 'BILLING', 'ACCOUNT', 'FEATURE', 'OTHER')),

                         CONSTRAINT chk_tickets_priority
                             CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')),

                         CONSTRAINT chk_tickets_status
                             CHECK (status IN ('OPEN', 'IN_PROGRESS', 'WAITING', 'RESOLVED', 'CLOSED'))
);
CREATE TABLE ticket_responses (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  message TEXT NOT NULL,
                                  ticket_id UUID NOT NULL,
                                  author_id UUID NOT NULL,
                                  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_responses_ticket
                                      FOREIGN KEY (ticket_id)
                                          REFERENCES tickets(id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_responses_author
                                      FOREIGN KEY (author_id)
                                          REFERENCES users(id)
                                          ON DELETE CASCADE
);

CREATE INDEX idx_tickets_user_id ON tickets(user_id);
CREATE INDEX idx_tickets_status ON tickets(status);
CREATE INDEX idx_tickets_created_at ON tickets(created_at);
CREATE INDEX idx_ticket_responses_ticket_id ON ticket_responses(ticket_id);