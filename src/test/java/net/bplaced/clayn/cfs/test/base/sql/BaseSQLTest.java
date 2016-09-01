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
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.bplaced.clayn.cfs.impl.sql.SQLCFileSystemTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Clayn
 */
public class BaseSQLTest
{
    private String url;
    private String user="sa";
    private String password="";
    public BaseSQLTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp() throws ClassNotFoundException, IOException
    {
        TemporaryFolder folder=new TemporaryFolder();
        folder.create();
        File db = folder.newFile("testdb");
        url="jdbc:h2:"+db.toURI().toURL();
        System.out.println("Using database: "+url);
        //url="jdbc:h2:/Users/Clayn/h2/cfs/sql";
        Class.forName("org.h2.Driver");
    }
    
    @After
    public void tearDown()
    {
    }
    
    protected Connection getDB()
    {
        try
        {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException ex)
        {
            Logger.getLogger(SQLCFileSystemTest.class.getName()).log(Level.SEVERE,
                    null, ex);
            throw new RuntimeException(ex);
        }
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
}
