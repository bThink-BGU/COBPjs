package il.ac.bgu.cs.bp.bpjs.context.roomsexample;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import il.ac.bgu.cs.bp.bpjs.context.ContextService;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.events.MotionDetectedEvent;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.events.MotionStoppedEvent;
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

		bprog.enqueueExternalEvent(new MotionDetectedEvent((rooms.get("37/123").getMotionDetector())));
		Thread.sleep(1000);
		bprog.enqueueExternalEvent(new MotionStoppedEvent(rooms.get("37/123").getMotionDetector()));
		Thread.sleep(1000);
		bprog.enqueueExternalEvent(new MotionDetectedEvent((rooms.get("37/123").getMotionDetector())));
		//TODO: REMOVED
		//ContextService.close();
	}


	public static void main(String[] args) throws InterruptedException {
		new RoomsExample().run("db_population.js", "ContextDB");
	}
}
