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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * The DBChecker is a toolset to verify the integrity of a database. The
 * available checks will be extended in further releases.
 *
 * @author Clayn <clayn_osmato@gmx.de>
 * @since 0.2.0
 */
public final class DBChecker
{

    private DBChecker()
    {
        throw new UnsupportedOperationException(
                "Creating a DBChecker is forbidden");
    }

    /**
     * Checks wether or not there exists a table with the given name in the
     * given database. This method checks the connections metadata to go through
     * the available tables. The connection will be closed afterwards.
     *
     * @param con the database where to search for the table
     * @param tableName the table to search for
     * @return {@code true} if and only if the table could be found in the
     * metadata, {@code false} otherwise or when an exception occures.
     */
    public static boolean tableExists(Connection con, String tableName)
    {
        try (Connection c = con)
        {
            DatabaseMetaData data = c.getMetaData();
            try (ResultSet set = data.getTables(null, null, "%", null))
            {
                boolean found = false;
                while (set.next())
                {
                    found = set.getString(3).equals(tableName);
                    if (found)
                    {
                        break;
                    }
                }
                return found;
            }

        } catch (SQLException ex)
        {
            return false;
        }
    }
}
