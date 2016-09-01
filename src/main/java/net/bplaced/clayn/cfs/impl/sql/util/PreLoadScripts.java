package net.bplaced.clayn.cfs.impl.sql.util;

import java.io.InputStream;
import java.util.function.Supplier;

/**
 *
 * @author Clayn
 * @since 0.1
 * @version $Revision: 332 $
 */
public class PreLoadScripts
{

    private final String name;
    private final Supplier<InputStream> source;

    public PreLoadScripts(String name, Supplier<InputStream> source)
    {
        this.name = name;
        this.source = source;
    }

    public String getName()
    {
        return name;
    }

    public Supplier<InputStream> getSource()
    {
        return source;
    }

}
