package net.bplaced.clayn.cfs.impl.sql.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Clayn
 * @version $Revision: 333 $
 * @since 0.1
 */
public class JDBCExecutor
{

    private final Connection con;
    private int index = 1;
    private Map<Integer, Object> parameters;
    private String sql;

    public JDBCExecutor(Connection con)
    {
        this(con, null);
    }

    private JDBCExecutor(Connection con, String sql)
    {
        this.parameters = new HashMap<>();
        this.con = con;
        this.sql = sql;
    }

    /**
     * Adds the given string to the current sql satement. This method also adds
     * an {@code " "} (whitespace) to the end so you can use this method like
     * {@code exec.script("INSERT").script("INTO")}.
     *
     * @param add the string to add to the current sql statement
     * @return the same instance
     * @since 0.1
     */
    public JDBCExecutor script(String add)
    {
        if (sql == null)
        {
            sql = add.concat(" ");
            return this;
        }
        sql = sql.concat(add).concat(" ");
        return this;
    }

    /**
     * Sets the given object as parameter for the index returned by
     * {@link #currentIndex()}. The parameters will be tried to set using
     * {@link PreparedStatement#setObject(int, java.lang.Object)} so keep that
     * in mind since that might fail depending on the connection.
     *
     * @param para the object to set as parameter
     * @return the same instance
     * @since 0.1
     */
    public JDBCExecutor put(Object para)
    {
        parameters.put(index++, para);
        return this;
    }

    @SuppressFBWarnings
    private PreparedStatement prepare(String sql) throws SQLException
    {
        PreparedStatement stat = null;
        try
        {
            stat = con.prepareStatement(sql);
            for (int i : parameters.keySet())
            {
                stat.setObject(i, parameters.get(i));
            }
            return stat;
        } catch (SQLException ex)
        {
            if (stat != null)
            {
                stat.close();
            }
            throw ex;
        }
    }

    /**
     * Returns the current index used for setting parameters. The index returned
     * by this method, will be the one the argument for
     * {@link #put(java.lang.Object)} will set to the executed statement.
     *
     * @return the index used for the next parameter.
     * @since 0.2.0
     */
    public int currentIndex()
    {
        return index;
    }

    public ResultSet query(String sql) throws SQLException
    {
        return prepare(sql).executeQuery();
    }

    /**
     * Uses the given sql to execute an update on the connected database. Using
     * this method overwrites all sql previous set by
     * {@link #script(java.lang.String)}.
     *
     * @param sql the sql to use for the update
     * @return the result of the update
     * @throws SQLException SQLException if a database access error occurs or
     * this method is called on a closed connection
     * @since 0.1
     * @see #update()
     * @see #query()
     * @see #query(java.lang.String)
     */
    public int update(String sql) throws SQLException
    {
        return prepare(sql).executeUpdate();
    }

    /**
     * Uses the stored sql statement to query the connected database and returns
     * the result.
     *
     * @return the result of the query using the stored sql statement.
     * @throws SQLException if a database access error occurs; this method is
     * called on a closed PreparedStatement or the SQL statement does not return
     * a ResultSet object
     * @since 0.1
     * @see #query(java.lang.String)
     * @see #update()
     * @see #update(java.lang.String)
     */
    public ResultSet query() throws SQLException
    {
        if (sql == null)
        {
            throw new NullPointerException();
        }
        return query(sql);
    }

    /**
     * Executes an update with the stored sql on the connected database.
     *
     * @return the result of the update
     * @throws SQLException if a database access error occurs or this method is
     * called on a closed connection
     * @see #update(java.lang.String)
     * @see #query()
     * @see #query(java.lang.String)
     * @since 0.1
     */
    public int update() throws SQLException
    {
        if (sql == null)
        {
            throw new NullPointerException();
        }
        return update(sql);
    }

    /**
     * Connects to the given databaseconnection. Simply uses
     * {@link JDBCExecutor#JDBCExecutor(java.sql.Connection)}.
     *
     * @param con the databaseconnection
     * @return a new executor for the given database
     * @since 0.1
     * @see #JDBCExecutor(java.sql.Connection, java.lang.String)
     * @see #JDBCExecutor(java.sql.Connection)
     */
    public static JDBCExecutor connect(Connection con)
    {
        return new JDBCExecutor(con);
    }
}
