package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.StorageModificationStrategy;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionStrategy;

import java.util.*;

import static java.util.stream.Collectors.joining;

public class ContextBProgram extends ResourceBProgram {
  public ContextBProgram(String aResourceName) {
    this(Collections.singletonList(aResourceName));
  }

  public ContextBProgram(String... resourceNames) {
    this(List.of(resourceNames));
  }

  public ContextBProgram(Collection<String> someResourceNames) {
    this(someResourceNames, someResourceNames.stream().collect(joining("+")));
  }

  public ContextBProgram(Collection<String> someResourceNames, String aName) {
    super(append(someResourceNames), aName, new CtxEventSelectionStrategy());
    super.setStorageModificationStrategy(new ContextStorageModificationStrategy());
    putInGlobalScope("ctx_proxy", ContextProxy.Create(this));
  }

  private static Collection<String> append(Collection<String> resourceNames) {
    return new ArrayList<>(resourceNames.size() + 1) {{
      add("context.js");
      addAll(resourceNames);
    }};
  }

  @Override
  public <T extends EventSelectionStrategy> T setEventSelectionStrategy(T anEventSelectionStrategy) {
    throw new UnsupportedOperationException("Cannot change the EventSelectionStrategy in ContextBProgram");
  }

  @Override
  public void setStorageModificationStrategy(StorageModificationStrategy storageModificationStrategy) {
    throw new UnsupportedOperationException("Cannot change the StorageModificationStrategy in ContextBProgram");
  }
}
