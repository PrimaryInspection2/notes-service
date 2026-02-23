CREATE TABLE notes (
                       note_id VARCHAR(255) PRIMARY KEY,
                       user_id VARCHAR(255) NOT NULL ,
                       title VARCHAR(255),
                       content TEXT,
                       source VARCHAR(255),
                       status VARCHAR(50),
                       priority VARCHAR(50) NOT NULL,
                        created_at TIMESTAMP NOT NULL,
                       updated_at TIMESTAMP NOT NULL
);

CREATE TABLE tags (
                      tag_id VARCHAR(255) PRIMARY KEY,
                      user_id VARCHAR(255) NOT NULL,
                      name VARCHAR(100) NOT NULL,
                      color VARCHAR(7),
                      description VARCHAR(255),
);

CREATE TABLE note_tags (
                           note_id VARCHAR(255) NOT NULL,
                           tag_id VARCHAR(255) NOT NULL,
                           PRIMARY KEY (note_id, tag_id),
                           FOREIGN KEY (note_id) REFERENCES notes(note_id) ON DELETE CASCADE,
                           FOREIGN KEY (tag_id) REFERENCES tags(tag_id) ON DELETE CASCADE
);