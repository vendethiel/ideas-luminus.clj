CREATE TABLE users (
  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  email TEXT,
  username TEXT,
  admin BOOLEAN NOT NULL,
  last_login TIME,
  is_active BOOLEAN NOT NULL,
  pass TEXT
);
--;;
CREATE TABLE categories (
  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  name TEXT NOT NULL
);
--;;
CREATE TABLE ideas (
  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  user_id INT NOT NULL REFERENCES users (id),
  name TEXT NOT NULL,
  description TEXT NOT NULL,
  tags TEXT[] NOT NULL
);
--;;
CREATE TABLE idea_category (
  category_id INT NOT NULL REFERENCES ideas (id),
  idea_id INT NOT NULL REFERENCES categories (id),
  PRIMARY KEY (category_id, idea_id)
);
--;;
CREATE TABLE implementations (
  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  idea_id integer NOT NULL REFERENCES ideas (id),
  user_id integer NOT NULL REFERENCES users (id),
  repo_url TEXT,
  demo_url TEXT,
  abstract TEXT,
  comment TEXT,
  tags TEXT[] NOT NULL
);
--;;
CREATE TABLE screenshots (
  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  implementation_id integer NOT NULL REFERENCES implementations (id),
  url TEXT NOT NULL
);
--;;
CREATE TABLE comments (
  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  parent_type varchar(50) NOT NULL,
  parent_id integer NOT NULL,
  user_id integer NOT NULL REFERENCES users (id),
  created_at timestamp NOT NULL DEFAULT CUqRENT_TIMESTAMP,
  content TEXT NOT NULL
);
