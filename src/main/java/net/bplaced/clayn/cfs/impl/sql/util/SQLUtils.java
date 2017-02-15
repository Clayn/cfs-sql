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
import java.sql.SQLException;

/**
 * Class for utility functions that target databases and general sql.
 *
 * @author Clayn <clayn_osmato@gmx.de>
 * @since 0.3.0
 */
public final class SQLUtils
{

    private SQLUtils() throws IllegalAccessException
    {
        throw new IllegalAccessException("No SQLUtils for you!");
    }

    
    /**
     * Commits all transactions that were done with the given connection. If the
     * connection has autocommit enabled or is closed, this method does nothing.
     * Some JDBC Drivers will fail to call {@link Connection#commit() commit()}
     * when autocommit is enabled.
     *
     * @param con the connection which transactions should be commited
     * @throws SQLException if the commiting failed
     * @since 0.3.0
     */
    public static final void commit(Connection con) throws SQLException
    {
        if (!con.isClosed() && !con.getAutoCommit())
        {
            con.commit();
        }
    }
}
