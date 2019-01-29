package il.ac.bgu.cs.bp.bpjs.context.examples.ttt;

import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

public class Main {
	void run(String dbPopulationScript, String persistenceUnit) throws InterruptedException {
		System.out.println(">>>>>>>>>>>>>>>>>> TTT Blockly example <<<<<<<<<<<<<<<<<<<");

		ContextService contextService = ContextService.getInstance();
		contextService.init(persistenceUnit);
		BProgram bprog = contextService.run(dbPopulationScript, "program.js");

		// Simulation of external events
		Thread.sleep(1000);
		/*Map<String, Room> rooms = contextService.<Room>getContextsOfType("Room")
				.stream().collect(Collectors.toMap(Room::getId, Function.identity()));

		bprog.enqueueExternalEvent((rooms.get("37/123").getMotionDetector()).startedEvent());
		Thread.sleep(1000);
		bprog.enqueueExternalEvent(rooms.get("37/123").getMotionDetector().stoppedEvent());
		Thread.sleep(1000);
		bprog.enqueueExternalEvent(rooms.get("37/123").getMotionDetector().startedEvent());*/
		//TODO: REMOVED
		//ContextService.close();
	}


	public static void main(String[] args) throws InterruptedException {
		new Main().run("db_population.js", "ContextDB");
	}
}
