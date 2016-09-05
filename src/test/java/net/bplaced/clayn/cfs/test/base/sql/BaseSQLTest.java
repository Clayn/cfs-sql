/*
 * Copyright (C) 2016 Clayn
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
package net.bplaced.clayn.cfs.test.base.sql;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import net.bplaced.clayn.cfs.CFileSystem;
import net.bplaced.clayn.cfs.impl.sql.SQLCFileSystem;
import net.bplaced.clayn.cfs.impl.sql.SQLCFileSystemTest;
import org.junit.BeforeClass;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Clayn
 */
public interface BaseSQLTest
{

    public static StringProperty url = new SimpleStringProperty();

    @BeforeClass
    public static void setUpClass() throws ClassNotFoundException
    {
        Class.forName("org.h2.Driver");
    }

    public default Connection getDB()
    {
        try
        {
            return DriverManager.getConnection(url.get(), "sa", "");
        } catch (SQLException ex)
        {
            Logger.getLogger(SQLCFileSystemTest.class.getName()).log(
                    Level.SEVERE,
                    null, ex);
            throw new RuntimeException(ex);
        }
    }

    public default CFileSystem getSQLFileSystem() throws Exception
    {
        TemporaryFolder folder = new TemporaryFolder();
        folder.create();
        File db = folder.newFile("testdb");
        url.set("jdbc:h2:" + db.toURI().toURL());
        System.out.println("Using database: " + url);
        //url="jdbc:h2:/Users/Clayn/h2/cfs/sql";

        return new SQLCFileSystem(this::getDB);
    }
}
