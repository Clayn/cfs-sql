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
package net.bplaced.clayn.cfs.impl.sql.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Clayn
 */
public class ScriptLoader2Test
{

    private static final String[] SCRIPTS = new String[]
    {
        "SELECT * FROM table WHERE cond=?",
        "INSERT INTO table (c1,c2,c3) VALUES(?,?,?)"
    };

    public ScriptLoader2Test()
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
    public void setUp()
    {
    }

    @After
    public void tearDown()
    {
    }

    /**
     * Test of loadScripts method, of class ScriptLoader2.
     *
     * @throws java.io.IOException
     */
    @Test
    public void testLoadScripts() throws IOException
    {
        System.out.println("loadScripts");
        List<Script> scripts;
        try (InputStream in = getClass().getResourceAsStream(
                "/sql/scripts/TestScript.sql"))
        {
            scripts = ScriptLoader2.loadScripts(in).getScripts();
        }
        assertNotNull(scripts);
        assertEquals(SCRIPTS.length, scripts.size());
        scripts.stream().map((sc)
                -> 
                {
                    assertFalse(sc.getSql().endsWith(";"));
                    return sc;
        }).forEach((sc)
                -> 
                {
                    assertEquals(sc.getSql(), sc.getSql().trim());
        });
        List<Script> tmp = new ArrayList<>(scripts);
        Collections.sort(tmp, Comparator.comparingInt(Script::getId));
        for (int i = 0; i < SCRIPTS.length; ++i)
        {
            assertTrue(containsName("Script_" + i, tmp));
            assertEquals(SCRIPTS[i], tmp.get(i).getSql());
        }
    }

    private boolean containsName(String name, List<Script> scripts)
    {
        return scripts.stream().parallel().map(Script::getName).filter(
                name::equals).findAny().isPresent();
    }

    @Test
    public void testNamesMissing() throws Exception
    {
        System.out.println("nameMissing");
        List<Script> scripts;
        try (InputStream in = getClass().getResourceAsStream(
                "/sql/scripts/MissingName.sql"))
        {
            scripts = ScriptLoader2.loadScripts(in).getScripts();
        }
        assertNotNull(scripts);
        assertEquals(1, scripts.size());
        assertEquals("Script_0", scripts.get(0).getName());
    }
}
