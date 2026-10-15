CREATE TABLE users(
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE projects(
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR(150) NOT NULL,
    owner_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE tasks(
    id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    status      VARCHAR(20) NOT NULL DEFAULT 'TODO'
                CHECK ( status IN ('TODO', 'DOING', 'DONE')),
    due_date    DATE,
    project_id  BIGINT NOT NULL REFERENCES projects(id) ON DELETE CASCADE
);

CREATE INDEX idx_projects_owner ON projects (owner_id);
CREATE INDEX idx_tasks_project_status ON tasks (project_id, status);