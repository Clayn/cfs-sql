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
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Clayn
 */
public class ScriptLoader2
{

    private static final String SINGLE = "(\\/{2}.*\\n?)";
    private static final String MULTI = "(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)";
    private static final Pattern NAME=Pattern.compile("\\[(.*)\\]");
    
    public static ScriptList loadScripts(InputStream in) throws IOException
    {
        List<Script> scripts=new ArrayList<>();
        int read;
        StringWriter writer=new StringWriter(1024);
        boolean inComment=false;
        boolean commentStart=false;
        boolean commentEnd=false;
        int counter=0;
        while((read=in.read())!=-1)
        {
            if(read=='/')
            {
                commentStart=!inComment;
                if(commentEnd)
                {
                    inComment=false;
                }
            }
            if(read=='*')
            {
                if(commentStart)
                {
                    inComment=true;        
                    commentStart=false;
                }
                else
                {
                    commentEnd=true;
                }
            }
            else
            {
                commentEnd=false;
            }
            if(!inComment&&read==';')
            {
                String str=writer.toString();
                scripts.add(new Script(getName(str,counter), counter++, removeComments(
                        str)));
                writer=new StringWriter(1024);
            }
            else
            {
                writer.write(read);
                writer.flush();
            }
        }
        writer.close();
        return new ScriptList(scripts);
    }
    
    private static void prepareScript(String str,Map<String,String> map)
    {
        String name=getName(str,-1);
        if(name==null)
            throw new RuntimeException("Scripts must have a name assigend");
        map.put(name, removeComments(str));
    }
    
    private static String getName(String str,int index)
    {
        Matcher m=NAME.matcher(str);
        if(!m.find())
            return String.format("Script_%d", index);
        return m.group(1);
    }
    
    private static String removeComments(String str)
    {
        return str.replaceAll(SINGLE, "").replaceAll(MULTI, "").replaceAll(NAME.pattern(),
                "").trim();
    }
}
