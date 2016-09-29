/**
 * Author:  Clayn
 * Created: 06.07.2016
 */

DROP TABLE IF EXISTS cfs_file;
DROP TABLE IF EXISTS cfs_directory;
DROP TABLE IF EXISTS cfs_modtype;
DROP TABLE IF EXISTS cfs_modification;

CREATE TABLE IF NOT EXISTS cfs_directory(
  root BOOLEAN DEFAULT false,
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  parent BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL
  );
  
 ALTER TABLE cfs_directory ADD UNIQUE KEY(parent,name);

CREATE TABLE IF NOT EXISTS cfs_file(
  parent BIGINT NOT NULL,
  bytes BIGINT,
  data BLOB,
  name VARCHAR(255),

  
  FOREIGN KEY (parent) REFERENCES cfs_directory(id),
  PRIMARY KEY(parent,name)
);

CREATE TABLE IF NOT EXISTS cfs_modtype(
    id INT PRIMARY KEY,
    typeName VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS cfs_modification(

    name VARCHAR(255) NOT NULL,
    parent BIGINT NOT NULL,
    modType INT NOT NULL,
    modTime TIMESTAMP default CURRENT_TIMESTAMP,
    consumed BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (parent,name) REFERENCES cfs_file(parent,name),
    FOREIGN KEY (modType) REFERENCES cfs_modtype(id),
    CONSTRAINT cfs_mod_ok PRIMARY KEY(name,parent,modTime)
);

INSERT INTO cfs_modtype (id,typeName) VALUES (0,'create');
INSERT INTO cfs_modtype (id,typeName) VALUES (1,'delete');
INSERT INTO cfs_modtype (id,typeName) VALUES (2,'modify');
