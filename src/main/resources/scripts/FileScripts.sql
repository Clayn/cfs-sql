/* 
 * Copyright (C) 2016 Clayn <clayn_osmato@gmx.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Author:  Clayn <clayn_osmato@gmx.de>
 * Created: 04.10.2016
 */
/*[Create]*/
INSERT INTO cfs_attribute(name,parent) VALUES (?,?);
/*[Get Attributes]*/
SELECT * FROM cfs_attribute att WHERE att.name=? AND att.parent=?;
/*[Set modtime]*/
UPDATE cfs_attribute SET lastMod=? WHERE name=? AND parent=?;
/*[Set createtime]*/
UPDATE cfs_attribute SET created=? WHERE name=? AND parent=?;
/*[Set usetime]*/
UPDATE cfs_attribute SET lastUsed=? WHERE name=? AND parent=?;