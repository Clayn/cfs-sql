package net.bplaced.clayn.cfs.impl.sql;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.function.Supplier;
import net.bplaced.clayn.cfs.CFileSystem;
import net.bplaced.clayn.cfs.Directory;
import net.bplaced.clayn.cfs.FileSettings;
import net.bplaced.clayn.cfs.SimpleFileSettings;
import net.bplaced.clayn.cfs.impl.sql.util.DBChecker;
import net.bplaced.clayn.cfs.impl.sql.util.DatabaseIntegrityException;
import net.bplaced.clayn.cfs.impl.sql.util.SQLUtils;
import net.bplaced.clayn.cfs.impl.sql.util.Script;
import net.bplaced.clayn.cfs.impl.sql.util.ScriptList;
import net.bplaced.clayn.cfs.impl.sql.util.ScriptLoader2;

/**
 *
 * @author Clayn
 * @version $Revision: 333 $
 * @since 0.1
 */
public class SQLCFileSystem implements CFileSystem
{

    static final FileSettings SETTINGS = new SimpleFileSettings();
    private ScriptList createScripts;
    private ScriptList workScripts;

    public static final String FILE_TABLE = "cfs_file";
    public static final String DIRECTORY_TABLE = "cfs_directory";
    private static final String CREATE_SCRIPT = "Create Root";
    private static final String ROOT_SCRIPT = "Root";

    private static final String TABLE_REPLACEMENT = "__TABLE__";
    /**
     * @deprecated Use {@code workScripts.getScript(ROOT_SCRIPT)} instead.
     */
    private static final String GET_ROOT_QUERY = "SELECT * FROM " + DIRECTORY_TABLE + " dir WHERE dir.root = true";
    /**
     * @deprecated Use {@code workScripts.getScript(CREATE_SCRIPT)} instead.
     */
    private static final String CREATE_ROOT = "INSERT INTO " + DIRECTORY_TABLE + " (name,root,parent) VALUES ('/',true,-1)";

    private final Supplier<Connection> dbAccess;

    public SQLCFileSystem(Supplier<Connection> dbAccess) throws SQLException
    {
        this.dbAccess = dbAccess;
        loadScripts();
        try
        {
            checkIntegrity();
        } catch (DatabaseIntegrityException ex)
        {
            createTables();
        }
    }

    private void loadScripts() throws SQLException
    {
        try (InputStream in = getClass().getResourceAsStream(
                "/scripts/CreateFS.sql"))
        {
            createScripts = ScriptLoader2.loadScripts(in);
        } catch (IOException ex)
        {
            throw new SQLException(ex);
        }
        try (InputStream in = getClass().getResourceAsStream(
                "/scripts/FileSystemScripts.sql"))
        {
            workScripts = ScriptLoader2.loadScripts(in);
        } catch (IOException ex)
        {
            throw new SQLException(ex);
        }
    }

    private void checkIntegrity()
    {
        if (!DBChecker.tableExists(dbAccess.get(), FILE_TABLE) || !DBChecker.tableExists(
                dbAccess.get(), DIRECTORY_TABLE))
        {
            throw new DatabaseIntegrityException(
                    "The integrity of the database can't be ensured anymore. "
                    + "Possible reason: either file or directory table can't be found");
        }
    }

    private void createTables() throws SQLException
    {
        try (Connection con = dbAccess.get())
        {
            for (Script script : createScripts.getScripts())
            {
                try (PreparedStatement stat = con.prepareStatement(
                        script.getSql()))
                {
                    stat.executeUpdate();
                }
            }
            SQLUtils.commit(con);
        }
    }

    @SuppressFBWarnings(justification = "Bugs should be fixed, however findbugs will report them if not annotated")
    @Override
    public Directory getRoot() throws IOException
    {
        PreparedStatement stat = null;
        ResultSet set = null;
        try (Connection con = dbAccess.get())
        {
            boolean old = con.getAutoCommit();
            con.setAutoCommit(false);
            stat = con.prepareStatement(workScripts.getScript(
                    ROOT_SCRIPT).replace(TABLE_REPLACEMENT, DIRECTORY_TABLE).getSql());
            set = stat.executeQuery();
            long id;
            if (!set.next())
            {
                stat = con.prepareStatement(
                        workScripts.getScript(CREATE_SCRIPT).replace(
                        TABLE_REPLACEMENT,
                        DIRECTORY_TABLE).getSql(),
                        Statement.RETURN_GENERATED_KEYS);
                int res = stat.executeUpdate();
                if (res != 1)
                {
                    throw new IOException("Could not create root directory");
                }
                set = stat.getGeneratedKeys();
                if (!set.next())
                {
                    throw new IOException(
                            "There was an error while creating the directory");
                }
                id = set.getLong(1);
            } else
            {
                id = set.getLong("id");
            }
            con.commit();
            con.setAutoCommit(old);

            return new SQLDirectoryImpl("/", null, dbAccess, id);
        } catch (SQLException ex)
        {
            try
            {
                if (stat != null)
                {
                    stat.close();
                }
                if (set != null)
                {
                    set.close();
                }
            } catch (SQLException ex1)
            {
                throw new IOException(ex1);
            }
            throw new IOException(ex);
        }

    }

    @Override
    public FileSettings getFileSettings()
    {
        return SETTINGS;
    }

    @Override
    public CFileSystem subFileSystem(String dir) throws IOException
    {
        SQLDirectoryImpl impl = (SQLDirectoryImpl) getDirectory(dir);
        impl.mkDirs();
        SQLDirectoryImpl nRoot = new SQLDirectoryImpl(impl.getName(), null,
                dbAccess, impl.getId());
        try
        {
            return new SQLCFileSystem(dbAccess)
            {
                @Override
                public Directory getRoot() throws IOException
                {
                    return nRoot;
                }

            };
        } catch (SQLException sQLException)
        {
            throw new IOException(sQLException);
        }
    }

}
