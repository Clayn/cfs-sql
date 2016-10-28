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
package net.bplaced.clayn.cfs.impl.sql.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.function.Supplier;
import net.bplaced.clayn.cfs.impl.sql.SQLCFileSystem;
import net.bplaced.clayn.cfs.impl.sql.err.DBAccessException;

/**
 * This class wraps connection informations for a database and can be used 
 * for creating a {@link SQLCFileSystem}.
 * @author Clayn <clayn_osmato@gmx.de>
 * @since 0.3.0
 */
public class DBSupplier implements Supplier<Connection>
{
    private final String url;
    private final String user;
    private final String password;

    /**
     * Creates a new DBSupplier that uses the given url, user and password 
     * to open jdbc connections. 
     * @param url the url for the database
     * @param user the user that should connect
     * @param password the password for that user
     * @since 0.3.0
     */
    public DBSupplier(String url, String user, String password)
    {
        this.url = url;
        this.user = user;
        this.password = password;
    }
    
    
    /**
     * Returns a new connection using the stored connection informations.
     * @return a new jdbc connection
     * @since 0.3.0
     * @throws DBAccessException if the connection could not be established.
     */
    @Override
    public Connection get()
    {
        try
        {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException ex)
        {
            throw new DBAccessException(ex);
        }
    }
    
}
