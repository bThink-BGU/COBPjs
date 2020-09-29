package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import org.mozilla.javascript.Scriptable;

import java.io.*;

/**
 * Convenience class for BPrograms that consist of JavaScript file
 */
public class ContextBProgram extends BProgram {
    private String dir;

    public ContextBProgram(String dir) {
        super(dir);
        this.dir = dir;
        verifyDirectoryExists(dir);
    }

    @Override
    protected void setupProgramScope(Scriptable scope) {
        InputStream testoryJS = Thread.currentThread().getContextClassLoader().getResourceAsStream("context.js");
        evaluate(testoryJS, "context.js");

        setWaitForExternalEvents(false);

        File folder = new File(dir);
        FilenameFilter filter = (dir, name) -> name.endsWith(".js");
        for (File f : folder.listFiles(filter)) {
            try {
                evaluate(new FileInputStream(f), f.getName());
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Error reading resource: '" + f.getPath() + "': " + e.getMessage(), e);
            }
        }
    }

    private void verifyDirectoryExists(String name) {
        File file = new File(name);
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("Cannot find directory'" + name + "'");
        }
    }

}