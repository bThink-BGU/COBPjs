package il.ac.bgu.cs.bp.bpjs.context.hotcold;

import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListenerAdapter;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

public class HotColdActuator extends BProgramRunnerListenerAdapter {
  @Override
  public void eventSelected(BProgram bp, BEvent e) {
    if(e.name.equals("hot") || e.name.equals("cold")) {
      System.out.printf("Pouring " + e.name + " water.");
    }
  }
}
