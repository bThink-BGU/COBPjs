package il.ac.bgu.cs.bp.bpjs.context.roomsexample;

import javax.persistence.EntityManager;

import il.ac.bgu.cs.bp.bpjs.context.CTX;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.events.MotionDetectedEvent;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.events.MotionStoppedEvent;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.Office;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.Room;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

public class RoomsExample {
	private static void setIntitialContext(EntityManager em) {
		Room r1 = new Room("96/224");
		Room r2 = new Room("96/225");
		Room r3 = new Office("37/123");
		Office o1 = new Office("96/226");
		r1.setHasPerson(true);

		em.getTransaction().begin();
		em.persist(r1);
		em.persist(r2);
		em.persist(r3);
		em.persist(o1);
		em.getTransaction().commit();
	}

	public static void main(String[] args) throws InterruptedException {
		System.out.println(">>>>>>>>>>>>>>>>>> Rooms example <<<<<<<<<<<<<<<<<<<");
		
		CTX.init(RoomsExample::setIntitialContext);

		CTX.registerContextQuery("NonEmptyRooms", Room.class);
		CTX.registerContextQuery("Rooms", Room.class);

		BProgram bprog = CTX.run("program.js");

		// Simulation of external events
		Thread.sleep(1000);

		Office r37_123 = new Office("37/123");
		Office r96_225 = new Office("96/225");
		
		bprog.enqueueExternalEvent(new MotionDetectedEvent(r37_123.getMotionSensor()));
		bprog.enqueueExternalEvent(new MotionDetectedEvent(r96_225.getMotionSensor()));

		Thread.sleep(1000);

		bprog.enqueueExternalEvent(new MotionStoppedEvent(r37_123.getMotionSensor()));

		Thread.sleep(1000);

		CTX.close();

	}
}
