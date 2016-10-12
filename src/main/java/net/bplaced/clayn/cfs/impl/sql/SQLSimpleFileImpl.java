package net.bplaced.clayn.cfs.impl.sql;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import net.bplaced.clayn.cfs.Directory;
import net.bplaced.clayn.cfs.FileAttributes;
import net.bplaced.clayn.cfs.SimpleFile;
import net.bplaced.clayn.cfs.impl.sql.util.JDBCExecutor;
import net.bplaced.clayn.cfs.impl.sql.util.SQLUtils;
import net.bplaced.clayn.cfs.impl.sql.util.ScriptLoader;

/**
 *
 * @author Clayn
 * @version $Revision: 333 $
 * @since 0.1
 */
public class SQLSimpleFileImpl implements SimpleFile
{

    private static final Logger LOG = Logger.getLogger(
            SQLSimpleFileImpl.class.getName());

    private final String name;
    private final SQLDirectoryImpl parent;
    private final Supplier<Connection> dbAccess;
    private boolean cachedExist = false;
    private boolean cached = false;
    private final SQLFileAttributes attributes;

    static
    {
        ScriptLoader.loadScripts(SQLDirectoryImpl.SCRIPT_KEY,
                () -> SQLCFileSystem.class.getResourceAsStream(
                        "/scripts/Directories.sql"));
    }

    public SQLSimpleFileImpl(String name, SQLDirectoryImpl parent,
            Supplier<Connection> dbAccess) throws IOException
    {
        this.name = name;
        this.parent = parent;
        this.dbAccess = dbAccess;
        this.attributes = new SQLFileAttributes(this);
    }

    Supplier<Connection> getDbAccess()
    {
        return dbAccess;
    }

    @Override
    public boolean exists()
    {
        if (!parent.exists())
        {
            return false;
        }
        String sql = "SELECT * FROM " + SQLCFileSystem.FILE_TABLE + " WHERE parent=? AND name=?";
        try (Connection con = dbAccess.get())
        {
            try (PreparedStatement stat = con.prepareStatement(sql))
            {
                stat.setLong(1, parent.getId());
                stat.setString(2, name);
                try (ResultSet res = stat.executeQuery())
                {
                    cachedExist = res.next();
                    cached = true;
                }
            }
            return cachedExist;
        } catch (SQLException ex)
        {
            throw new RuntimeException(
                    "Could not determinate if file exists or not", ex);
        }
    }

    @Override
    public void create() throws IOException
    {
        if (exists())
        {
            return;
        }
        if (!parent.exists())
        {
            throw new IOException(
                    "Can't create file. Parent " + parent + " does not exist");
        }
        if ("CreateFS.sql".equals(name))
        {
            int a = 1;
        }

        String sql = "INSERT INTO " + SQLCFileSystem.FILE_TABLE + " (parent,name,bytes) VALUES (?,?,?)";
        long time = -1;
        try (Connection con = dbAccess.get())
        {
            JDBCExecutor.connect(con)
                    .put(parent.getId())
                    .put(name)
                    .put(0)
                    .update(sql);
            time = System.currentTimeMillis();
            SQLUtils.commit(con);
            JDBCExecutor.connect(con)
                    .put(parent.getId())
                    .put(name)
                    .put("create")
                    .update("INSERT INTO cfs_modification (parent,name,modType) VALUES (?,?,(SELECT id FROM cfs_modtype mt WHERE mt.typeName=?))");
            SQLUtils.commit(con);
            cached = true;
            cachedExist = true;
        } catch (SQLException ex)
        {
            Logger.getLogger(SQLSimpleFileImpl.class.getName()).log(Level.SEVERE,
                    null, ex);
            throw new IOException(ex);
        }
        if (time != -1)
        {
            attributes.setCreated(time);
        }
    }

    @Override
    public void delete() throws IOException
    {
        String sql2 = "DELETE FROM cfs_modification WHERE name=? AND parent=?";
        String sql = "DELETE FROM " + SQLCFileSystem.FILE_TABLE + " WHERE parent=? AND name=?";
        try (Connection con = dbAccess.get())
        {
            try (PreparedStatement stat = con.prepareStatement(sql2))
            {
                stat.setString(1, name);
                stat.setLong(2, parent.getId());
                stat.executeUpdate();
            }
            try (PreparedStatement stat = con.prepareStatement(sql))
            {
                stat.setLong(1, parent.getId());
                stat.setString(2, name);
                stat.executeUpdate();
            }
            SQLUtils.commit(con);
        } catch (SQLException ex)
        {
            Logger.getLogger(SQLSimpleFileImpl.class.getName()).log(Level.SEVERE,
                    null, ex);
            throw new IOException(ex);
        }
    }

    @Override
    public InputStream openRead() throws IOException
    {
        if (!exists())
        {
            throw new IOException("File " + this + " does not exist");
        }
        InputStream in = null;
        try (Connection con = dbAccess.get())
        {
            boolean old = con.getAutoCommit();
            con.setAutoCommit(false);
            String sql = "SELECT file.data as data FROM " + SQLCFileSystem.FILE_TABLE + " file WHERE file.name=? AND file.parent=?";
            try (PreparedStatement stat = con.prepareStatement(sql))
            {
                stat.setString(1, name);
                stat.setLong(2, parent.getId());
                ResultSet set = stat.executeQuery();
                if (set.next())
                {
                    in = set.getBinaryStream("data");
                }
            }
        } catch (SQLException ex)
        {
            Logger.getLogger(SQLSimpleFileImpl.class.getName()).log(Level.SEVERE,
                    null, ex);
            throw new IOException(ex);
        }
        attributes.setUsed(System.currentTimeMillis());
        return in == null ? new ByteArrayInputStream(new byte[0]) : in;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Directory getParent()
    {
        return parent;
    }

    /**
     * Returns an Outputstream to write content to the file.
     *
     * @return
     * @throws IOException
     */
    @Override
    public OutputStream openWrite() throws IOException
    {
        if (!exists())
        {
            throw new IOException("File " + this + " does not exist");
        }
//        ByteArrayOutputStream bout = new ByteArrayOutputStream() {
//            @Override
//            public void close() throws IOException {
//                try {
//                    super.close();
//                    try (Connection con = dbAccess.get()) {
//                        con.setAutoCommit(false);
//                        String sql = "UPDATE cfs_file SET data=?,bytes=? WHERE parent=? AND name=?";
//                        PreparedStatement stat = con.prepareStatement(sql);
//                        byte[] data = toByteArray();
//                        ByteArrayInputStream bin = new ByteArrayInputStream(data);
//                        stat.setBlob(1, bin);
//                        stat.setLong(2, data.length);
//                        stat.setLong(3,parent.getId());
//                        stat.setString(4, name);
//                        stat.executeUpdate();
//                        con.commit();
//                    }
//                } catch (SQLException ex) {
//                    Logger.getLogger(SQLSimpleFileImpl.class.getName()).log(Level.SEVERE, null, ex);
//                    throw new IOException(ex);
//                }
//
//            }
//
//        };
//        return bout;
        return openW();
    }

    protected OutputStream openW() throws IOException
    {
        ObjectProperty<Thread> reader = new SimpleObjectProperty<>();
        LongProperty prop = new SimpleLongProperty(0);
        PipedOutputStream pout = new PipedOutputStream()
        {
            @Override
            public void close() throws IOException
            {
                try
                {
                    super.close();
                    LOG.info("Wait for the reader thread to finish");
                    reader.get().join();
                    attributes.setLastModified(System.currentTimeMillis());
                    
                } catch (InterruptedException ex)
                {
                    Logger.getLogger(SQLSimpleFileImpl.class.getName()).log(
                            Level.SEVERE, null, ex);
                    throw new IOException(ex);
                }
            }

            @Override
            public void write(int b) throws IOException
            {
                super.write(b);
                prop.set(prop.get() + 1);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException
            {
                super.write(b, off, len);
                prop.set(prop.get() + len);
            }
        };
        PipedInputStream pin = new PipedInputStream(pout);
        Thread t = new Thread(()
                -> 
                {
                    long time = -1;
                    try (Connection con = dbAccess.get())
                    {
                        String sql = "UPDATE cfs_file SET data=?,bytes=? WHERE parent=? AND name=?";
                        try (PreparedStatement stat = con.prepareStatement(sql))
                        {
                            stat.setBlob(1, pin);
                            stat.setLong(2, prop.get() == 0 ? -1 : prop.get());
                            stat.setLong(3, parent.getId());
                            stat.setString(4, name);
                            stat.executeUpdate();
                            time = System.currentTimeMillis();
                            SQLUtils.commit(con);
                        }
                    } catch (SQLException ex)
                    {
                        Logger.getLogger(SQLSimpleFileImpl.class.getName()).log(
                                Level.SEVERE, null, ex);
                        throw new RuntimeException(ex);
                    }
                    

        });
        reader.set(t);
        t.start();
        return pout;
    }

    @Override
    public long getSize() throws IOException
    {
        if (!exists())
        {
            throw new FileNotFoundException(
                    "File " + toString() + " does not exist");
        }
        String sel = "SELECT bytes as size FROM " + SQLCFileSystem.FILE_TABLE + " WHERE parent=? AND name=?";
        try (Connection con = dbAccess.get(); PreparedStatement stat = con.prepareStatement(
                sel))
        {
            stat.setLong(1, parent.getId());
            stat.setString(2, name);
            try (ResultSet set = stat.executeQuery())
            {
                return set.next() ? set.getLong("size") : -1;
            }
        } catch (SQLException ex)
        {
            Logger.getLogger(SQLSimpleFileImpl.class.getName()).log(Level.SEVERE,
                    null, ex);
            throw new IOException(ex);
        }
    }

    @Override
    public String toString()
    {
        return parent.toString() + name;
    }

    @Override
    public FileAttributes getFileAttributes()
    {
        return attributes;
    }

}
