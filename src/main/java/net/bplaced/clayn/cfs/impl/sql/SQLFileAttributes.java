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
package net.bplaced.clayn.cfs.impl.sql;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.bplaced.clayn.cfs.FileAttributes;
import net.bplaced.clayn.cfs.err.CFSException;
import net.bplaced.clayn.cfs.impl.sql.util.SQLUtils;
import net.bplaced.clayn.cfs.impl.sql.util.Script;
import net.bplaced.clayn.cfs.impl.sql.util.ScriptList;
import net.bplaced.clayn.cfs.impl.sql.util.ScriptLoader2;

/**
 *
 * @author Clayn <clayn_osmato@gmx.de>
 */
public class SQLFileAttributes implements FileAttributes
{

    private final SQLSimpleFileImpl file;
    private final ScriptList scripts;

    public SQLFileAttributes(SQLSimpleFileImpl file) throws IOException
    {
        this.file = file;
        try(InputStream in=getClass().getResourceAsStream("/scripts/FileScripts.sql"))
        {
            scripts=ScriptLoader2.loadScripts(in);
        }
        if (!checkExistence())
        {
            create();
        }
    }
    
    private boolean checkExistence()
    {
        try(Connection con=file.getDbAccess().get())
        {
            PreparedStatement stat=con.prepareStatement(scripts.getScript("Get Attributes").getSql());
            stat.setString(1, file.getName());
            stat.setLong(2, ((SQLDirectoryImpl)file.getParent()).getId());
            ResultSet res=stat.executeQuery();
            return res.next();
        } catch (SQLException ex)
        {
            throw new CFSException(ex);
        }
    }
    
    private void create()
    {
        try(Connection con=file.getDbAccess().get())
        {
            PreparedStatement stat=con.prepareStatement(scripts.getScript("Create").getSql());
            stat.setString(1, file.getName());
            stat.setLong(2, ((SQLDirectoryImpl)file.getParent()).getId());
            stat.executeUpdate();
            SQLUtils.commit(con);
        } catch (SQLException ex)
        {
            throw new CFSException(ex);
        }
    }
    
    void setLastModified(long time)
    {
        if(time<0)
            return;
        setValue("Set modtime", time);
    }
    
    void setCreated(long time)
    {
        System.out.println("Set creation to: "+time);
        if(time<0)
            return;
        setValue("Set createtime", time);
        
    }
    
    void setUsed(long time)
    {
        if(time<0)
            return;
        setValue("Set usetime", time);
    }
    
    private void setValue(String script, long value)
    {
        try(Connection con=file.getDbAccess().get())
        {
            PreparedStatement stat=con.prepareStatement(scripts.getScript(script).getSql());
            stat.setLong(1, value);
            stat.setString(2, file.getName());
            stat.setLong(3, ((SQLDirectoryImpl)file.getParent()).getId());
            int res=stat.executeUpdate();
            SQLUtils.commit(con);
        } catch (SQLException ex)
        {
            throw new CFSException(ex);
        }
    }
    
    @Override
    public long lastModified()
    {
        return getValueFor("lastMod");
    }

    @Override
    public long creationTime()
    {
        return getValueFor("created");
    }
    private long getValueFor(String column)
    {
        try(Connection con=file.getDbAccess().get())
        {
            Script sc = scripts.getScript("Get Attributes");
            PreparedStatement stat=con.prepareStatement(sc.getSql());
            stat.setString(1, file.getName());
            stat.setLong(2, ((SQLDirectoryImpl)file.getParent()).getId());
            ResultSet res=stat.executeQuery();
            return res.next()?res.getLong(column):-1;
        } catch (SQLException ex)
        {
            throw new CFSException(ex);
        }
    }
    @Override
    public long lastUsed()
    {
        return getValueFor("lastUsed");
    }
    
}
