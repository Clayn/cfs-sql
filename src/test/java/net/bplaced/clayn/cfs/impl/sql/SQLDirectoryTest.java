package net.bplaced.clayn.cfs.impl.sql;

import java.util.Arrays;
import net.bplaced.clayn.cfs.CFileSystem;
import net.bplaced.clayn.cfs.test.DirectoryTest;
import net.bplaced.clayn.cfs.test.base.sql.BaseSQLTest;

/**
 *
 * @author Clayn <clayn_osmato@gmx.de>
 */
public class SQLDirectoryTest extends DirectoryTest implements BaseSQLTest
{

    public SQLDirectoryTest()
    {
        runningTests.addAll(Arrays.asList(TEST_ALL));
    }

    
    @Override
    public CFileSystem getFileSystem() throws Exception
    {
        return getSQLFileSystem();
    }
    
}
