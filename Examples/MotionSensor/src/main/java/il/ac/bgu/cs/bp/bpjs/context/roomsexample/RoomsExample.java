package il.ac.bgu.cs.bp.bpjs.context.roomsexample;

import java.security.cert.CollectionCertStoreParameters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import il.ac.bgu.cs.bp.bpjs.context.roomsexample.events.MotionDetectedEvent;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.events.MotionStoppedEvent;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.rooms.Room;

import il.ac.bgu.cs.bp.bpjs.context.CTX;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

public class RoomsExample {
	public void run(String dbPopulationScript, String persistenceUnit) throws InterruptedException {
		System.out.println(">>>>>>>>>>>>>>>>>> Rooms example <<<<<<<<<<<<<<<<<<<");

		CTX.init(persistenceUnit);

		BProgram bprog = CTX.run(dbPopulationScript, "program.js");

		// Simulation of external events
		Thread.sleep(1000);
		Map<String, Room> rooms = CTX.getContextsOfType("Room.findAll", Room.class)
				.stream().collect(Collectors.toMap(Room::getId, Function.identity()));

		bprog.enqueueExternalEvent(new MotionDetectedEvent((rooms.get("37/123").getMotionDetector())));
		Thread.sleep(1000);
		bprog.enqueueExternalEvent(new MotionStoppedEvent(rooms.get("37/123").getMotionDetector()));
		Thread.sleep(1000);
		bprog.enqueueExternalEvent(new MotionDetectedEvent((rooms.get("37/123").getMotionDetector())));
		//TODO: REMOVED
		//CTX.close();
	}


	public static void main(String[] args) throws InterruptedException {
		new RoomsExample().run("db_population.js", "ContextDB");
	}
}
