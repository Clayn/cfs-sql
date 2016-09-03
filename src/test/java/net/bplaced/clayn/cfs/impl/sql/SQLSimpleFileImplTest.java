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
package net.bplaced.clayn.cfs.impl.sql;

import java.util.Arrays;
import net.bplaced.clayn.cfs.CFileSystem;
import net.bplaced.clayn.cfs.test.SimpleFileTest;
import net.bplaced.clayn.cfs.test.base.sql.BaseSQLTest;

/**
 *
 * @author Clayn
 */
public class SQLSimpleFileImplTest extends SimpleFileTest implements BaseSQLTest
{
    
    public SQLSimpleFileImplTest()
    {
        runningTests.addAll(Arrays.asList(TEST_ALL));
    }
    

    @Override
    public CFileSystem getFileSystem() throws Exception
    {
        return getSQLFileSystem();
    }  
}
