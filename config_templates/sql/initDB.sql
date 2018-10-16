DROP TABLE IF EXISTS groups CASCADE;
DROP TABLE IF EXISTS city CASCADE;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS projects CASCADE;
DROP TABLE IF EXISTS project_group CASCADE;
DROP SEQUENCE IF EXISTS user_seq;
DROP TYPE IF EXISTS group_type;
DROP TYPE IF EXISTS user_flag;

CREATE TYPE user_flag AS ENUM ('active', 'deleted', 'superuser');
CREATE TYPE group_type AS ENUM ('FINISHED', 'CURRENT');

CREATE SEQUENCE user_seq
  START 100000;

CREATE TABLE city (
  code TEXT PRIMARY KEY,
  name TEXT NOT NULL
);

CREATE TABLE users (
  id        INTEGER PRIMARY KEY DEFAULT nextval('user_seq'),
  full_name TEXT      NOT NULL,
  email     TEXT      NOT NULL,
  flag      user_flag NOT NULL,
  code TEXT NOT NULL,
  FOREIGN KEY (code) REFERENCES city(code)
);

CREATE UNIQUE INDEX email_idx
  ON users (email);

CREATE TABLE groups(
  name TEXT primary key,
  type group_type NOT NULL
);

CREATE TABLE projects(
  name TEXT NOT NULL primary key,
  description TEXT NOT NULL
);

CREATE TABLE project_group(
  group_name TEXT,
  project_name TEXT,
  FOREIGN KEY (group_name) REFERENCES groups(name),
  FOREIGN KEY (project_name) REFERENCES projects(name)
);
