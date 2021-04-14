ALTER SEQUENCE iam_origin_seq RESTART WITH 2;

INSERT INTO iam_origin (sid, name, description) VALUES (1, 'internal', 'internal');
