package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.StorageModificationStrategy;

import java.util.*;

public class DebugableContextBProgram extends ResourceBProgram {
    public DebugableContextBProgram(String aResourceName) {
        this(Collections.singletonList(aResourceName));
    }

    public DebugableContextBProgram(String... resourceNames) {
        this(List.of(resourceNames));
    }

    public DebugableContextBProgram(Collection<String> someResourceNames) {
        this(someResourceNames, String.join("+", someResourceNames));
    }

    public DebugableContextBProgram(Collection<String> someResourceNames, String aName) {
        super(someResourceNames, aName, null);
        initBProgram(this);
    }

    public static void initBProgram(BProgram bprog) {
        var proxy = new ContextProxy();
        bprog.setStorageModificationStrategy(new CtxStorageModificationStrategy(proxy));
        bprog.putInGlobalScope("ctx_proxy", proxy);
    }

    @Override
    public void setStorageModificationStrategy(StorageModificationStrategy storageModificationStrategy) {
        // COBP makes its own usage and rules regarding storage. Do not mix different usages.
        if (!(storageModificationStrategy instanceof CtxStorageModificationStrategy))
            throw new UnsupportedOperationException("Cannot change the StorageModificationStrategy in ContextBProgram");
        super.setStorageModificationStrategy(storageModificationStrategy);
    }
}
