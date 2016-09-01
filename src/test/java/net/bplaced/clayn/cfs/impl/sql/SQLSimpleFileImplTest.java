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

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import net.bplaced.clayn.cfs.CFileSystem;
import net.bplaced.clayn.cfs.Directory;
import net.bplaced.clayn.cfs.SimpleFile;
import net.bplaced.clayn.cfs.test.base.sql.BaseSQLTest;
import net.bplaced.clayn.cfs.util.CFiles;
import org.junit.AfterClass;
import org.junit.Assert;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Clayn
 */
public class SQLSimpleFileImplTest extends BaseSQLTest
{
    
    public SQLSimpleFileImplTest()
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
    

    /**
     * Test of exists method, of class SQLSimpleFileImpl.
     */
    @Test
    public void testExists() throws SQLException, IOException
    {
        System.out.println("exists");
        CFileSystem fs=new SQLCFileSystem(this::getDB);
        SimpleFile file=fs.getFile("Test.txt");
        assertNotNull(file);
        assertFalse(file.exists());
        file.create();
        assertTrue(file.exists());
    }

    /**
     * Test of create method, of class SQLSimpleFileImpl.
     */
    @Test
    public void testCreate() throws Exception
    {
        System.out.println("create");
        CFileSystem fs=new SQLCFileSystem(this::getDB);
        SimpleFile file=fs.getFile("Test.txt");
        assertNotNull(file);
        assertFalse(file.exists());
        file.create();
        assertTrue(file.exists());
    }

    /**
     * Test of delete method, of class SQLSimpleFileImpl.
     */
    @Test
    public void testDelete() throws Exception
    {
        System.out.println("delete");
        CFileSystem fs=new SQLCFileSystem(this::getDB);
        SimpleFile file=fs.getFile("Test.txt");
        assertNotNull(file);
        assertFalse(file.exists());
        file.create();
        assertTrue(file.exists());
        file.delete();
        assertFalse(file.exists());
    }

    /**
     * Test of openRead method, of class SQLSimpleFileImpl.
     */
    @Test
    public void testOpenRead() throws Exception
    {
        System.out.println("openRead");
        String text="Hello World";
        byte[] data=text.getBytes();
        CFileSystem fs=new SQLCFileSystem(this::getDB);
        SimpleFile file=fs.getFile("Test.txt");
        file.create();
        try(OutputStream out=file.openWrite())
        {
            out.write(data);
        }
        assertEquals(data.length, file.getSize());
        byte[] read=CFiles.readAllBytes(file);
        assertEquals(data.length, read.length);
        assertArrayEquals(data, read);
        assertEquals(text, new String(read));
    }

    /**
     * Test of getName method, of class SQLSimpleFileImpl.
     */
    @Test
    public void testGetName() throws SQLException, IOException
    {
        System.out.println("getName");
        CFileSystem fs=new SQLCFileSystem(this::getDB);
        SimpleFile file=fs.getFile("Test.txt");
        SimpleFile file2=fs.getFile("Test2.txt");
        SimpleFile file3=fs.getFile("dir/Test.txt");
        assertNotNull(file);
        assertNotNull(file2);
        assertNotNull(file3);
        
        assertEquals("Test.txt", file.getName());
        assertEquals("Test2.txt", file2.getName());
        assertEquals("Test.txt", file3.getName());
        
        assertEquals(file.getName(), file3.getName());
        assertNotEquals(file2.getName(), file.getName());
        assertNotEquals(file2.getName(), file3.getName());
    }

    /**
     * Test of getParent method, of class SQLSimpleFileImpl.
     */
    @Test
    public void testGetParent() throws Exception
    {
        System.out.println("getParent");
        CFileSystem fs=new SQLCFileSystem(this::getDB);
        SimpleFile file=fs.getFile("Test.txt");
        SimpleFile file2=fs.getFile("dir/Test.txt");
        SimpleFile file3=fs.getFile("Test2.txt");
        List<SimpleFile> files=Arrays.asList(file,file2,file3);
        files.stream().parallel().forEach(Assert::assertNotNull);
        files.stream().parallel().map(SimpleFile::getParent).forEach(Assert::assertNotNull);
        
        Directory dir1=file.getParent();
        Directory dir2=file2.getParent();
        
        assertNotEquals(dir1.getName(), dir2.getName());
        assertNull(dir1.getParent());
        assertNotNull(dir2.getParent());
        
        dir2=file3.getParent();
        assertNull(dir2.getParent());
        assertEquals(dir1.getName(), dir2.getName());
        
    }

    /**
     * Test of openWrite method, of class SQLSimpleFileImpl.
     */
    @Test
    public void testOpenWrite() throws Exception
    {
        System.out.println("openWrite");
        byte[] data="Hello World".getBytes();
        CFileSystem fs=new SQLCFileSystem(this::getDB);
        SimpleFile file=fs.getFile("Test.txt");
        file.create();
        int size=data.length;
        try(OutputStream out=file.openWrite())
        {
            out.write(data);
            out.flush();
        }
        assertEquals(size, file.getSize());
    }

    /**
     * Test of getSize method, of class SQLSimpleFileImpl.
     */
    @Test
    public void testGetSize() throws Exception
    {
        System.out.println("getSize");
        byte[] data="Hello World".getBytes();
        CFileSystem fs=new SQLCFileSystem(this::getDB);
        SimpleFile file=fs.getFile("Test.txt");
        assertEquals(-1, file.getSize());
        file.create();
        assertEquals(-1, file.getSize());
        int size=data.length;
        try(OutputStream out=file.openWrite())
        {
            out.write(data);
            out.flush();
        }
        assertEquals(size, file.getSize());
    }

    /**
     * Test of toString method, of class SQLSimpleFileImpl.
     */
    @Test
    public void testToString() throws SQLException, IOException
    {
        System.out.println("toString");
        CFileSystem fs=new SQLCFileSystem(this::getDB);
        SimpleFile file=fs.getFile("Test.txt");
        SimpleFile file2=fs.getFile("Test2.txt");
        SimpleFile file3=fs.getFile("dir/Test.txt");
        List<SimpleFile> files=Arrays.asList(file,file2,file3);
        
        assertEquals(file.getParent().toString()+file.getName(), file.toString());
        assertEquals(file2.getParent().toString()+file2.getName(), file2.toString());
        assertEquals(file3.getParent().toString()+file3.getName(), file3.toString());
        
        assertEquals("/Test.txt", file.toString());
        assertEquals("/Test2.txt", file2.toString());
        assertEquals("/dir/Test.txt", file3.toString());
    }
    
}
