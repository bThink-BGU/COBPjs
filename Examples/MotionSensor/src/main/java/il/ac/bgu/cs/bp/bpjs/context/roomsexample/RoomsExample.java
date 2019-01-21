package il.ac.bgu.cs.bp.bpjs.context.roomsexample;

import java.util.List;

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
		List<Room> rooms = CTX.getContextsOfType("Room.findAll", Room.class);

		for (Room room : rooms) {
			bprog.enqueueExternalEvent(new MotionDetectedEvent(((Room)room).getMotionDetector()));
		}

		Thread.sleep(1000);

		for (Room room : rooms) {
			bprog.enqueueExternalEvent(new MotionStoppedEvent(((Room)room).getMotionDetector()));
		}
		Thread.sleep(1000);

		//TODO: REMOVED
		//CTX.close();
	}


	public static void main(String[] args) throws InterruptedException {
		new RoomsExample().run("db_population.js", "ContextDB");
	}
}
