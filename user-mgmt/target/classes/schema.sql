DROP TABLE IF EXISTS USER_TABLE, TOKEN_TABLE;
CREATE TABLE USER_TABLE(
  id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  username VARCHAR(100) NOT NULL,
  password VARCHAR(100) NOT NULL,
  roles VARCHAR NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE TOKEN_TABLE(
  id INT NOT NULL AUTO_INCREMENT,
  user_id INT NOT NULL,
  token LONGVARCHAR NOT NULL,
  is_logged_out BOOLEAN DEFAULT FALSE
);

INSERT INTO USER_TABLE (name, username, roles, password) VALUES
    ('Alice', 'alice@mail.co','ROLE_ADMIN', '$2a$10$Qldj64zlh4eDNm7oGfwmWOkoevSo.Uv1LjXao/Rwsc2jGwjIXKRii'),
    ('Bob', 'bob@mail.co','ROLE_USER', '$2a$10$Qldj64zlh4eDNm7oGfwmWOkoevSo.Uv1LjXao/Rwsc2jGwjIXKRii'),
    ('John', 'John.Doe@mail.co','ROLE_USER', '$2a$10$Qldj64zlh4eDNm7oGfwmWOkoevSo.Uv1LjXao/Rwsc2jGwjIXKRii');
