package il.ac.bgu.cs.bp.bpjs.context.examples.ttt;

import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;

import java.util.Map;

public class Main {
	void run(String dbPopulationScript, String persistenceUnit) throws InterruptedException {
		System.out.println(">>>>>>>>>>>>>>>>>> TTT example <<<<<<<<<<<<<<<<<<<");

		ContextService contextService = ContextService.getInstance();
		contextService.initFromResources(persistenceUnit, dbPopulationScript, "program.js", "assertions.js");
		contextService.addContextUpdateListener(new ContextService.UpdateEffect(
				(EventSet) bEvent -> bEvent.name.equals("X") || bEvent.name.equals("O"),
				new String[]{"UpdateCell"},
				e -> Map.of("cell", e.getData(), "val", e.name)));
		contextService.run();
		BProgram bprog = contextService.getBProgram();

		// Simulation of external events
		Thread.sleep(3000);
		ContextService.getContextInstances("Cell")
				.forEach(cell -> bprog.enqueueExternalEvent(new BEvent("Click", cell)));

		Thread.sleep(6000);
		contextService.close();
	}


	public static void main(String[] args) throws InterruptedException {
		new Main().run("db_population.js", "ContextDB");
	}
}
