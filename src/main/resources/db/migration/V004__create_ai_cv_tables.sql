CREATE TABLE cv_documents (
                              id BIGSERIAL PRIMARY KEY,
                              user_id UUID NOT NULL UNIQUE,
                              personal_info TEXT,
                              summary TEXT,
                              experience TEXT,
                              education TEXT,
                              skills TEXT,
                              projects TEXT,
                              interests TEXT,
                              profile_photo TEXT,
                              last_modified TIMESTAMP
);

CREATE TABLE cv_document_section_order (
                                           cv_document_id BIGINT NOT NULL,
                                           section_name VARCHAR(255),
                                           CONSTRAINT fk_cv_document_order
                                               FOREIGN KEY (cv_document_id)
                                                   REFERENCES cv_documents (id)
                                                   ON DELETE CASCADE
);

CREATE INDEX idx_cv_documents_user_id ON cv_documents(user_id);