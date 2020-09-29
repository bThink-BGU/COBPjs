package il.ac.bgu.cs.bp.bpjs.context;

import com.google.devtools.common.options.Option;
import com.google.devtools.common.options.OptionsBase;

/**
 * Command-line options definition 
 */
public class BProgramOptions extends OptionsBase {
    @Option(name = "path", abbrev = 'p', help = "Path to the JavaScript test project.", defaultValue = "examples/room" )
    public String path;
}