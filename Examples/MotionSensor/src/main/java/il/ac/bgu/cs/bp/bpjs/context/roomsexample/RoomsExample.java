package il.ac.bgu.cs.bp.bpjs.context.roomsexample;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.rooms.Room;

import il.ac.bgu.cs.bp.bpjs.model.BProgram;

public class RoomsExample {
	void run(String dbPopulationScript, String persistenceUnit) throws InterruptedException {
		System.out.println(">>>>>>>>>>>>>>>>>> Rooms example <<<<<<<<<<<<<<<<<<<");

		ContextService contextService = ContextService.getInstance();
		contextService.init(persistenceUnit);
		BProgram bprog = contextService.run(dbPopulationScript, "program.js");

		// Simulation of external events
		Thread.sleep(1000);
		Map<String, Room> rooms = contextService.<Room>getContextsOfType("Room")
				.stream().collect(Collectors.toMap(Room::getId, Function.identity()));

		bprog.enqueueExternalEvent((rooms.get("37/123").getMotionDetector()).startedEvent());
		Thread.sleep(1000);
		bprog.enqueueExternalEvent(rooms.get("37/123").getMotionDetector().stoppedEvent());
		Thread.sleep(1000);
		bprog.enqueueExternalEvent(rooms.get("37/123").getMotionDetector().startedEvent());
		//TODO: REMOVED
		//ContextService.close();
	}


	public static void main(String[] args) throws InterruptedException {
		new RoomsExample().run("db_population.js", "ContextDB");
	}
}
