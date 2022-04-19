package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.StorageModificationStrategy;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;

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
    var proxy = new ContextProxy();
    super.setStorageModificationStrategy(new CtxStorageModificationStrategy(proxy));
    putInGlobalScope("ctx_proxy", proxy);
  }

  private static Collection<String> append(Collection<String> resourceNames) {
    return new ArrayList<>(resourceNames.size() + 1) {{
      add("base.js");
      add("context.js");
      addAll(resourceNames);
    }};
  }

/*  @Override
  public <T extends EventSelectionStrategy> T setEventSelectionStrategy(T anEventSelectionStrategy) {
    ((CtxEventSelectionStrategy)getEventSelectionStrategy()).setEventSelectionStrategy(anEventSelectionStrategy);
    return anEventSelectionStrategy;
  }*/

  @Override
  public void setStorageModificationStrategy(StorageModificationStrategy storageModificationStrategy) {
    throw new UnsupportedOperationException("Cannot change the StorageModificationStrategy in ContextBProgram");
    // COBP makes its own usage and rules regarding storage. Do not mix different usages.
  }
}
