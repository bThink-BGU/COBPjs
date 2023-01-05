/*
 * (c) Testory Technologies 2021
 */
package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspections;
import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;

import java.nio.file.FileSystems;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.fail;

public class TestUtils {

    public static ContextBProgram prepareBProgram(String ... resourceName) {
        return new ContextBProgram(resourceName);
    }


    public static String eventNamesString(List<BEvent> trace, String delimiter) {
        return trace.stream()
                .map(BEvent::getName)
                .collect(joining(delimiter));
    }

    public static String eventNamesString(List<BEvent> trace) {
        return eventNamesString(trace, "");
    }
    
    /**
     * Verify the bprogram with all standard BPjs-based inspections. Fail on any violation.
     * @param bprog The b-program to be verified.
     * @throws Exception 
     */
    public static VerificationResult verify(BProgram bprog) throws Exception{
        var vfr = new DfsBProgramVerifier(bprog);
        vfr.addInspection( ExecutionTraceInspections.FAILED_ASSERTIONS );
        vfr.addInspection( ExecutionTraceInspections.HOT_SYSTEM );
        vfr.addInspection( ExecutionTraceInspections.HOT_TERMINATIONS  );
        vfr.addInspection( ExecutionTraceInspections.HOT_BTHREADS  );
        
        var res = vfr.verify(bprog);
        
        if ( res.isViolationFound() ) {
            final Violation violation = res.getViolation().get();
            System.out.println("Violation found: " + violation.decsribe() );
            String trace = violation.getCounterExampleTrace().getNodes().stream()
                .map( n -> n.getEvent() ).filter( eo -> eo.isPresent() )
                .map( Optional::get ).map(e->e.toString()).collect( joining("\n"));
            fail("Violation found " + violation.decsribe() + "\n" + trace );
        }
        return res;
    }
    

    /**
     * Checks whether {@code sought} is equal to {@code list}, after the latter is
     * filtered to contain only members of former.
     * <p>
     * e.g.:
     * <code>
     * 1,2,3,4 is embedded in a,1,d,2,g,r,cv,3,g,4
     * a,b,x is not embedded in x,a,b
     * a,b,x is not embedded in a,a,b,x
     * </code>
     *
     * @param <T>
     * @param sought
     * @param list
     * @return
     */
    public static <T> boolean isEmbeddedSublist(List<T> sought, List<T> list) {
        Set<T> filter = new HashSet<>(sought);
        return list.stream()
                .filter(filter::contains)
                .collect(toList()).equals(sought);
    }

    public static String getPath(String pathPrefix, String pathSuffix) {
        String path = pathPrefix + pathSuffix;
        return FileSystems.getDefault().getPath(path).toAbsolutePath().toString();
    }
}
