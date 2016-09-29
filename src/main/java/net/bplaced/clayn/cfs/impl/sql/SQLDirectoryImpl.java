package net.bplaced.clayn.cfs.impl.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.bplaced.clayn.cfs.AbstractActiveDirectory;
import net.bplaced.clayn.cfs.ActiveDirectory;
import net.bplaced.clayn.cfs.Deletable;
import net.bplaced.clayn.cfs.Directory;
import net.bplaced.clayn.cfs.FileModification;
import net.bplaced.clayn.cfs.SimpleFile;
import net.bplaced.clayn.cfs.SimpleFileFilter;
import net.bplaced.clayn.cfs.impl.sql.util.JDBCExecutor;
import net.bplaced.clayn.cfs.impl.sql.util.SQLUtils;
import net.bplaced.clayn.cfs.impl.sql.util.ScriptLoader;
import net.bplaced.clayn.cfs.util.IOUtils;

/**
 *
 * @author Clayn
 * @version $Revision: 333 $
 * @since 0.1
 */
public class SQLDirectoryImpl extends AbstractActiveDirectory
{

    static final String SCRIPT_KEY = "directory_scripts";
    private static final int LIST_FILES_SCRIPT = 0;
    private static final int MK_DIR_SCRIPT = 1;
    private static final int GET_MODIFICATIONS_SCRIPT = 2;
    private static final int EXISTS_SCRIPT = 3;
    private static final int LIST_DIRECTORIES_SCRIPT = 4;
    private static final int DELETE_DIR_SCRIPT = 5;

    private static final String GET_DIRECTORY_WITH_NAME = "SELECT * FROM " + SQLCFileSystem.DIRECTORY_TABLE + " dir WHERE dir.name = ?";
    private static final String GET_FILES_WITH_PARENT = "SELECT files.name as name FROM " + SQLCFileSystem.FILE_TABLE + " files WHERE files.parent = ?";
    static final String MK_DIR = "INSERT INTO " + SQLCFileSystem.DIRECTORY_TABLE + " (name,root,parent) VALUES (?,?,?)";

    private static final String GET_LAST_MODIFICATION = "SELECT * FROM cfs_modification mod WHERE mod.parent=? AND mod.consumed=false ORDER BY mod.modTime DESC";

    private final String name;
    private final SQLDirectoryImpl parent;
    private final Supplier<Connection> dbAccess;
    private long id = -1;
//    private final Thread directoryMonitor;
    private boolean active = false;

    public SQLDirectoryImpl(String name, SQLDirectoryImpl parent,
            Supplier<Connection> dbAccess, long id)
    {
        this.name = name;
        this.parent = parent;
        this.dbAccess = dbAccess;
        this.id = id;
//        this.directoryMonitor = new Thread(getWatch());
    }

    @Override
    protected void dispatchModification(FileModification mod)
    {
        super.dispatchModification(mod);
    }

    private Runnable getWatch()
    {
        return new Runnable()
        {
            @Override
            public void run()
            {
                while (!Thread.interrupted())
                {
                    try
                    {
                        try (final Connection con = dbAccess.get())
                        {
                            List<FileModification> mods = new ArrayList<>();
                            PreparedStatement stat = con.prepareStatement(
                                    ScriptLoader.getScript(SCRIPT_KEY,
                                            MK_DIR_SCRIPT));
                            stat.setLong(1, id);
                            try (final ResultSet set = stat.executeQuery())
                            {
                                while (set.next())
                                {
                                    String file = set.getString("name");
                                    long pId = set.getLong("parent");
                                    int type = set.getInt("modType");
                                    long time = set.getTimestamp("modTime").getTime();
                                    SimpleFile sf = new SQLSimpleFileImpl(file,
                                            SQLDirectoryImpl.this, dbAccess);
                                    mods.add(new FileModification(sf,
                                            FileModification.Modification.values()[type],
                                            time));
                                }
                                mods.stream().sorted(Comparator.comparingLong(
                                        FileModification::getTimeStamp)).forEach(
                                                SQLDirectoryImpl.this::dispatchModification);
                            }
                            stat.close();
                        } catch (SQLException ex)
                        {
                            Logger.getLogger(
                                    SQLDirectoryImpl.class.getName()).log(
                                            Level.SEVERE,
                                            null, ex);
                            throw new RuntimeException(ex);
                        }
                        Thread.sleep(500);
                    } catch (InterruptedException ex)
                    {
                        Logger.getLogger(SQLDirectoryImpl.class.getName()).log(
                                Level.SEVERE,
                                null, ex);
                        throw new RuntimeException(ex);
                    }
                }
            }
        };
    }

    long getId()
    {
        return id;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean exists()
    {
        if (parent != null)
        {
            if (!parent.exists())
            {
                return false;
            }
            try (Connection con = dbAccess.get())
            {
                ResultSet set = JDBCExecutor.connect(con)
                        .put(name)
                        .put(parent.getId())
                        .query("SELECT * FROM " + SQLCFileSystem.DIRECTORY_TABLE + " WHERE name=? AND parent=?");
                if (set.next())
                {
                    id = set.getLong("id");
                    return true;
                }
                return false;
            } catch (SQLException ex)
            {
                Logger.getLogger(SQLDirectoryImpl.class.getName()).log(
                        Level.SEVERE, null, ex);
                return false;
            }
        } else
        {
            return true;
        }
//        try (Connection con = dbAccess.get()) {
//            boolean old=con.getAutoCommit();
//            con.setAutoCommit(false);
//            PreparedStatement stat=con.prepareStatement(GET_DIRECTORY_WITH_NAME);
//            stat.setString(1, name);
//            ResultSet set=stat.executeQuery();
//            con.setAutoCommit(true);
//            return set.next();
//        } catch (SQLException ex) {
//            Logger.getLogger(SQLDirectoryImpl.class.getName()).log(Level.SEVERE, null, ex);
//            return false;
//        }
    }

    @Override
    public ActiveDirectory changeDirectory(String path) throws IOException
    {
        boolean fromRoot = path.startsWith("/");
        path = IOUtils.cleanPath(path);
        String parts[] = path.split("/");
        ActiveDirectory end = this;
        if (fromRoot)
        {
            while (end.getParent() != null)
            {
                end = end.getParent();
            }
        }
        for (String part : parts)
        {
            if (!".".equals(part) && !"..".equals(part))
            {
                end = new SQLDirectoryImpl(part, (SQLDirectoryImpl) end,
                        dbAccess, -1);
            } else if (".".equals(part))
            {
                //end = end;
            } else
            {
                if (end.getParent() == null)
                {
                    throw new IOException(
                            "No parent directory available for 'root' directory");
                }
                end = end.getParent();
            }
        }

        return end;
    }

    @Override
    public SimpleFile getFile(String name) throws IOException
    {
        return new SQLSimpleFileImpl(name, this, dbAccess);
    }

    @Override //@TODO cache files that ma not be created...maybe
    public List<SimpleFile> listFiles(SimpleFileFilter sff) throws IOException
    {
        if (!exists())
        {
            throw new IOException("Can't list files in not existing directory");
        }
        List<SimpleFile> files = new ArrayList<>();
        try (Connection con = dbAccess.get())
        {
            try (PreparedStatement stat = con.prepareStatement(
                    GET_FILES_WITH_PARENT))
            {
                stat.setLong(1, id);
                try (ResultSet result = stat.executeQuery())
                {
                    while (result.next())
                    {
                        files.add(
                                new SQLSimpleFileImpl(result.getString("name"),
                                        this,
                                        dbAccess));
                    }
                }
            }
        } catch (SQLException ex)
        {
            Logger.getLogger(SQLDirectoryImpl.class.getName()).log(Level.SEVERE,
                    null, ex);
            throw new IOException(ex);
        }
        return files.stream().filter(sff).collect(Collectors.toList());
    }

    @Override
    public void mkDir() throws IOException
    {
        if (exists())
        {
            return;
        }
        boolean root = getParent() == null;
        if (root)
        {
            return;
        }
        if (!parent.exists())
        {
            throw new IOException(
                    "Can't create " + this + ". Parent directory " + parent + " does not exist");
        }
        try (Connection con = dbAccess.get())
        {
            boolean old = con.getAutoCommit();
            con.setAutoCommit(false);
            try (//            if(res!=0)
                    //            {
                    //                throw new IOException("Could not create directory "+name);
                    //            }
                    PreparedStatement stat = con.prepareStatement(MK_DIR,
                            Statement.RETURN_GENERATED_KEYS))
            {
                stat.setString(1, name);
                stat.setBoolean(2, root);
                stat.setLong(3, parent.id);
                int res = stat.executeUpdate();
                try (//            if(res!=0)
                        //            {
                        //                throw new IOException("Could not create directory "+name);
                        //            }
                        ResultSet set = stat.getGeneratedKeys())
                {
                    if (!set.next())
                    {
                        throw new IOException(
                                "There was some error while creating " + this);
                    }
                    id = set.getLong(1);

                }
            }
            SQLUtils.commit(con);
            con.setAutoCommit(old);
        } catch (SQLException ex)
        {
            Logger.getLogger(SQLDirectoryImpl.class.getName()).log(Level.SEVERE,
                    null, ex);
            throw new IOException(ex);
        }

    }

    @Override
    public String toString()
    {
        if (parent == null)
        {
            return "/";
        }
        return parent.toString() + name + "/";
    }

    @Override
    public ActiveDirectory getParent()
    {
        return parent;
    }

    @Override
    public List<Directory> listDirectories() throws IOException
    {
        if (!exists())
        {
            return new ArrayList<>();
        }
        try (Connection con = dbAccess.get())
        {
            List<Directory> dir;
            try (ResultSet base = JDBCExecutor.connect(con)
                    .script("SELECT")
                    .script("*")
                    .script("FROM")
                    .script(SQLCFileSystem.DIRECTORY_TABLE)
                    .script("dir")
                    .script("WHERE")
                    .script("dir.parent=?")
                    .put(id)
                    .query())
            {
                dir = new ArrayList<>();
                while (base.next())
                {
                    dir.add(fromResultSet(base));
                }
            }
            return dir;
        } catch (SQLException ex)
        {
            Logger.getLogger(SQLDirectoryImpl.class.getName()).log(Level.SEVERE,
                    null, ex);
            throw new IOException(ex);
        }
    }

    void invokeWatcher(FileModification... fileMod)
    {
        if (!active)
        {
            return;
        }
        Arrays.stream(fileMod).sorted(Comparator.comparingLong(
                FileModification::getTimeStamp)).forEach(
                        this::dispatchModification);
    }

    private boolean safeNext(ResultSet set)
    {
        try
        {
            return set.next();
        } catch (SQLException ex)
        {
            Logger.getLogger(SQLDirectoryImpl.class.getName()).log(Level.SEVERE,
                    null, ex);
            return false;
        }
    }

    private Directory fromResultSet(ResultSet set)
    {
        try
        {
            return new SQLDirectoryImpl(set.getString("name"), this, dbAccess,
                    set.getLong("id"));
        } catch (SQLException ex)
        {
            Logger.getLogger(SQLDirectoryImpl.class.getName()).log(Level.SEVERE,
                    null, ex);
            return null;
        }
    }

    @Override
    public void activate()
    {
        //directoryMonitor.start();
        active = true;
    }

    @Override
    public void deactivate()
    {
        //directoryMonitor.interrupt();
        active = false;
    }

    private void runtimeDelete(Deletable del)
    {
        try
        {
            del.delete();
        } catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void delete() throws IOException
    {
        //TODO Think a bulk deletion of files instead of single deletion. 
        Stream.concat(listFiles().stream(), listDirectories().stream()).forEach(
                this::runtimeDelete);
        try (Connection con = dbAccess.get())
        {
            String sql = "DELETE FROM cfs_directory WHERE id=?";
            try (PreparedStatement stat = con.prepareStatement(sql))
            {
                stat.setLong(1, id);
                stat.executeUpdate();
            }
        } catch (SQLException ex)
        {
            throw new IOException(ex);
        }
    }
}
