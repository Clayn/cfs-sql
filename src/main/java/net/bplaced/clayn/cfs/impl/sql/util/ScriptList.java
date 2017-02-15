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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This class collects Scripts loaded from the ScriptLoader2 to provide easy
 * access.
 *
 * @author Clayn
 * @since 0.3.0
 */
public class ScriptList
{

    private final List<Script> scripts;

    public ScriptList(List<Script> scripts)
    {
        this.scripts = scripts;
    }

    /**
     * Returns a unmodifiable list containing all scripts of this scriptlist.
     *
     * @return a list of all script
     * @since 0.3.0
     * @see #getSorted()
     */
    public List<Script> getScripts()
    {
        return Collections.unmodifiableList(scripts);
    }

    /**
     * Returns a unmodifiable list of the scripts sorted by their ID in ascending 
     * order. Though {@link #getScripts()} should also have this order, using this 
     * method will definitely ensure this order.
     * @return a sorted list of all scripts
     * @since 0.3.0
     * @see #getScripts() 
     */
    public List<Script> getSorted()
    {
        List<Script> tmp = new ArrayList<>(scripts);
        Collections.sort(tmp, Comparator.comparingInt(Script::getId));
        return Collections.unmodifiableList(tmp);
    }

    /**
     * Returns the script that with the given name.
     *
     * @param name the name of the script
     * @return the script loaded with the given name, if present.
     * @throws NoSuchElementException if no script with the name was found
     * @since 0.3.0
     */
    public Script getScript(String name) throws NoSuchElementException
    {
        return scripts.stream().parallel().filter((sc) -> sc.getName().equals(
                name)).findFirst().get();
    }
}
