package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.StorageModificationStrategy;

import java.util.*;

public class ContextBProgram extends ResourceBProgram {
    public ContextBProgram(String aResourceName) {
        this(Collections.singletonList(aResourceName));
    }

    public ContextBProgram(String... resourceNames) {
        this(List.of(resourceNames));
    }

    public ContextBProgram(Collection<String> someResourceNames) {
        this(someResourceNames, String.join("+", someResourceNames));
    }

    public ContextBProgram(Collection<String> someResourceNames, String aName) {
        super(append(someResourceNames), aName, null);
        initBProgram(this);
    }

    public static void initBProgram(BProgram bprog) {
        var proxy = new ContextProxy();
        bprog.setStorageModificationStrategy(new CtxStorageModificationStrategy(proxy));
        bprog.putInGlobalScope("ctx_proxy", proxy);
    }

    private static Collection<String> append(Collection<String> resourceNames) {
        return new ArrayList<>(resourceNames.size() + 2) {{
            add("base.js");
            add("context.js");
            addAll(resourceNames);
        }};
    }

    @Override
    public void setStorageModificationStrategy(StorageModificationStrategy storageModificationStrategy) {
        // COBP makes its own usage and rules regarding storage. Do not mix different usages.
        if (!(storageModificationStrategy instanceof CtxStorageModificationStrategy))
            throw new UnsupportedOperationException("Cannot change the StorageModificationStrategy in ContextBProgram");
        super.setStorageModificationStrategy(storageModificationStrategy);
    }
}
