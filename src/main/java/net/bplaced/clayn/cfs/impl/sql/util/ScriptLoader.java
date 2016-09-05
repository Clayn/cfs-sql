package net.bplaced.clayn.cfs.impl.sql.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The ScriptLoader is a utility tool that can load scripts from an input
 * stream. You can read all scripts at once or each one by one. The ScriptLoader
 * keeps track of the scripts so they can be accessed using its index. The main
 * use of this class is to store predefined skript in a separated file and then
 * get the sql statements to prepare statements.
 *
 * @author Clayn
 * @since 0.1
 * @deprecated Use {@link ScriptLoader2} instead
 */
public class ScriptLoader
{

    private static final String SINGLE = "(\\/{2}.*\\n?)";
    private static final String MULTI = "(?:/\\*(?:[^*]|(?:\\*+[^*/]))*\\*+/)|(?://.*)";

    private static final Map<String, PreLoadScripts> definedScripts = new HashMap<>();

    public static void loadScripts(String key, Supplier<InputStream> src)
    {
        if (definedScripts.containsKey(key))
        {
            return;
        }
        definedScripts.put(key, new PreLoadScripts(key, src));
    }

    public static void removeScripts(String key)
    {
        definedScripts.remove(key);
    }

    public static String getScript(String key, int scriptNum)
    {
        return Optional.ofNullable(definedScripts.get(key)).map(
                (PreLoadScripts t)
                -> 
                {
                    try
                    {
                        return getScript(t.getSource().get(), scriptNum);
                    } catch (IOException ex)
                    {
                        Logger.getLogger(ScriptLoader.class.getName()).log(
                                Level.SEVERE,
                                null, ex);
                        throw new RuntimeException(ex);
                    }
        }).get();
    }

    private static String getCommand(InputStream src) throws IOException
    {
        String script;
        char ch;
        boolean finished = false;
        try (StringWriter sw = new StringWriter())
        {
            byte[] buffer = new byte[1024];
            int read = 0;
            while (!finished && (read = src.read(buffer)) >= 0)
            {
                for (int i = 0; i < read; i++)
                {
                    ch = (char) buffer[i];
                    if (ch == ';')
                    {
                        break;
                    } else
                    {
                        sw.append(ch);
                    }
                }
            }
            sw.flush();
            script = sw.toString();
        }
        return removeCommentsAndTrim(script);
    }

    @SuppressFBWarnings(justification = "The check is needed to know when all scripts were read")
    public static String[] getScripts(InputStream src) throws IOException
    {
        List<String> scripts = new ArrayList<>();
        String script;
        while ((script = getCommand(src)) != null)
        {
            scripts.add(script);
        }
        return scripts.toArray(new String[scripts.size()]);
    }

    public static String getScript(InputStream src, int scriptNum) throws IOException
    {
        String[] scripts = null;
        try (InputStream in = src)
        {
            scripts = getScripts(in);
        }

        if (scripts == null || scriptNum >= scripts.length)
        {
            throw new RuntimeException();
        }
        return scripts[scriptNum];
    }

    private static String removeCommentsAndTrim(String str)
    {
        String res = str.replaceAll(SINGLE, "").replaceAll(MULTI, "");
        return res.trim();
    }
}
