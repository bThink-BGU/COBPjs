package il.ac.bgu.cs.bp.bpjs.context.examples.ttt;

import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.context.examples.ttt.schema.Cell;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

import java.util.function.Function;
import java.util.stream.Collectors;

public class Main {
	void run(String dbPopulationScript, String persistenceUnit) throws InterruptedException {
		System.out.println(">>>>>>>>>>>>>>>>>> TTT Blockly example <<<<<<<<<<<<<<<<<<<");

		ContextService contextService = ContextService.getInstance();
		contextService.initFromResources(persistenceUnit, dbPopulationScript, "program.js", "assertions.js");
		contextService.run();
		BProgram bprog = contextService.getBProgram();

		// Simulation of external events
		Thread.sleep(2000);
		contextService.<Cell>getContextInstances("Cell")
				.stream().collect(Collectors.toMap(Cell::getId, Function.identity()))
		.values().forEach(cell -> bprog.enqueueExternalEvent(new BEvent("Click",cell)));

		Thread.sleep(6000);
		contextService.close();
	}


	public static void main(String[] args) throws InterruptedException {
		new Main().run("db_population.js", "ContextDB");
	}
}
