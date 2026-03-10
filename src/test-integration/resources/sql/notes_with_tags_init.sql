-- ╔══════════════════════════════════════════════════════════════════════╗
-- ║                            TAGS                                      ║
-- ╠══════════╦══════════╦══════════╦═════════╦══════════════════════════╣
-- ║  tag_id  ║  user_id ║   name   ║  color  ║       description        ║
-- ╠══════════╬══════════╬══════════╬═════════╬══════════════════════════╣
-- ║  tag-1   ║  user-1  ║  work    ║ #FF0000 ║        work tag          ║
-- ║  tag-2   ║  user-1  ║  home    ║ #00FF00 ║        home tag          ║
-- ║  tag-3   ║  user-2  ║  sport   ║ #0000FF ║        sport tag         ║
-- ╚══════════╩══════════╩══════════╩═════════╩══════════════════════════╝

-- ╔══════════════════════════════════════════════════════════════════════╗
-- ║                            NOTES                                     ║
-- ╠═════════╦══════════╦══════════════╦═════════════╦════════╦══════════╣
-- ║ note_id ║  user_id ║    title     ║   content   ║ status ║ priority ║
-- ╠═════════╬══════════╬══════════════╬═════════════╬════════╬══════════╣
-- ║ note-10 ║  user-1  ║ Tagged Note  ║  With tags  ║ ACTIVE ║   HIGH   ║
-- ║ note-11 ║  user-1  ║ Second Note  ║  Also tags  ║ DONE   ║   LOW    ║
-- ║ note-12 ║  user-2  ║ Other User   ║  Isolated   ║ ACTIVE ║  MEDIUM  ║
-- ╚═════════╩══════════╩══════════════╩═════════════╩════════╩══════════╝

-- ╔═══════════════════════════════════════════════╗
-- ║            NOTE_TAGS (relations)              ║
-- ╠═════════╦══════════╦══════════════════════════╣
-- ║ note_id ║  tag_id  ║                          ║
-- ╠═════════╬══════════╣                          ║
-- ║ note-10 ║  tag-1   ║  → user-1 / "work"       ║
-- ║ note-10 ║  tag-2   ║  → user-1 / "home"       ║
-- ║ note-11 ║  tag-1   ║  → user-1 / "work"       ║
-- ║ note-12 ║  tag-3   ║  → user-2 / "sport"      ║
-- ╚═════════╩══════════╩══════════════════════════╝

-- Summary:
--   user-1
--     ├── note-10 "Tagged Note"  →  tag-1 "work", tag-2 "home"
--     └── note-11 "Second Note"  →  tag-1 "work"
--   user-2
--     └── note-12 "Other User"   →  tag-3 "sport"

INSERT INTO tags (tag_id, user_id, name, color, description, created_at, updated_at)
VALUES ('tag-1', 'user-1', 'work',  '#FF0000', 'work tag',  now(), now()),
       ('tag-2', 'user-1', 'home',  '#00FF00', 'home tag',  now(), now()),
       ('tag-3', 'user-2', 'sport', '#0000FF', 'sport tag', now(), now());

INSERT INTO notes (note_id, user_id, title, content, source, status, priority, created_at, updated_at)
VALUES ('note-10', 'user-1', 'Tagged Note', 'With tags', 'REST_API', 'ACTIVE', 'HIGH',   now(), now()),
       ('note-11', 'user-1', 'Second Note', 'Also tags', 'REST_API', 'DONE',   'LOW',    now(), now()),
       ('note-12', 'user-2', 'Other User',  'Isolated',  'REST_API', 'ACTIVE', 'MEDIUM', now(), now());

INSERT INTO note_tags (note_id, tag_id)
VALUES ('note-10', 'tag-1'),
       ('note-10', 'tag-2'),
       ('note-11', 'tag-1'),
       ('note-12', 'tag-3');