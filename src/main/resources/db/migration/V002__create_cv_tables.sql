CREATE TABLE cvs (
                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                     user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                     template_type VARCHAR(50) NOT NULL,
                     first_name VARCHAR(100) NOT NULL,
                     last_name VARCHAR(100) NOT NULL,
                     email VARCHAR(255) NOT NULL,
                     phone VARCHAR(50) NOT NULL,
                     address VARCHAR(255),
                     profile_picture VARCHAR(500),
                     summary TEXT,
                     created_at DATE NOT NULL DEFAULT CURRENT_DATE,
                     updated_at DATE NOT NULL DEFAULT CURRENT_DATE
);

CREATE TABLE experiences (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             cv_id UUID NOT NULL REFERENCES cvs(id) ON DELETE CASCADE,
                             position VARCHAR(100) NOT NULL,
                             company VARCHAR(100) NOT NULL,
                             location VARCHAR(100),
                             start_date DATE NOT NULL,
                             end_date DATE,
                             description TEXT
);

CREATE TABLE educations (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            cv_id UUID NOT NULL REFERENCES cvs(id) ON DELETE CASCADE,
                            degree VARCHAR(150) NOT NULL,
                            institution VARCHAR(150) NOT NULL,
                            location VARCHAR(100),
                            start_date DATE NOT NULL,
                            end_date DATE,
                            description TEXT
);

CREATE TABLE languages (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           cv_id UUID NOT NULL REFERENCES cvs(id) ON DELETE CASCADE,
                           name VARCHAR(50) NOT NULL,
                           level VARCHAR(50) NOT NULL
);

CREATE TABLE cv_skills (
                           cv_id UUID NOT NULL REFERENCES cvs(id) ON DELETE CASCADE,
                           skill VARCHAR(100) NOT NULL
);

CREATE INDEX idx_cvs_user_id ON cvs(user_id);
CREATE INDEX idx_experiences_cv_id ON experiences(cv_id);
CREATE INDEX idx_educations_cv_id ON educations(cv_id);
CREATE INDEX idx_languages_cv_id ON languages(cv_id);
CREATE INDEX idx_cv_skills_cv_id ON cv_skills(cv_id);