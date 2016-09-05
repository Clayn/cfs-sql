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

/**
 *
 * @author Clayn
 */
public class ScriptList
{

    private final List<Script> scripts;

    public ScriptList(List<Script> scripts)
    {
        this.scripts = scripts;
    }

    public List<Script> getScripts()
    {
        return scripts;
    }

    public List<Script> getSorted()
    {
        List<Script> tmp = new ArrayList<>(scripts);
        Collections.sort(tmp, Comparator.comparingInt(Script::getId));
        return Collections.unmodifiableList(tmp);
    }

    public Script getScript(String name)
    {
        return scripts.stream().parallel().filter((sc) -> sc.getName().equals(
                name)).findFirst().get();
    }
}
