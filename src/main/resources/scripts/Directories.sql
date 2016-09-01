/**
 * Author:  Clayn
 * Created: 11.07.2016
 */

SELECT files.name as name FROM cfs_file files WHERE files.parent = ?;

INSERT INTO cfs_directory (name,root,parent) VALUES (?,?,?);

SELECT * FROM cfs_modification mod WHERE mod.parent=? AND mod.consumed=false ORDER BY mod.modTime DESC;

SELECT * FROM cfs_directory WHERE name=? AND parent=?;

SELECT * FROM cfs_directory dir WHERE dir.parent=?;
