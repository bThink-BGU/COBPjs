package il.ac.bgu.cs.bp.bpjs.context.examples.ttt;

import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.context.examples.ttt.schema.Cell;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
	void run(String dbPopulationScript, String persistenceUnit) throws InterruptedException {
		System.out.println(">>>>>>>>>>>>>>>>>> TTT Blockly example <<<<<<<<<<<<<<<<<<<");

		ContextService contextService = ContextService.getInstance();
		contextService.init(persistenceUnit, dbPopulationScript, "program.js");
		BProgram bprog = contextService.run();

		// Simulation of external events
		Thread.sleep(1000);
		contextService.<Cell>getContextInstances("Cell")
				.stream().collect(Collectors.toMap(Cell::getId, Function.identity()))
		.values().forEach(cell -> bprog.enqueueExternalEvent(new BEvent("Click",cell)));

		/*Thread.sleep(5000);
		contextService.close();
		contextService.init(persistenceUnit, dbPopulationScript, "program.js");
		contextService.run();*/
	}


	public static void main(String[] args) throws InterruptedException {
		new Main().run("db_population.js", "ContextDB");
	}
}
