package il.ac.bgu.cs.bp.bpjs.context.roomsexample;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import il.ac.bgu.cs.bp.bpjs.context.CTX;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.events.MotionDetectedEvent;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.events.MotionStoppedEvent;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.events.Stop;
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

		Room r96_226 = new Room("96/226");
		Office r37_123 = new Office("37/123");
		Office r96_225 = new Office("96/225");

		bprog.enqueueExternalEvent(new MotionDetectedEvent(r37_123.getMotionSensor()));
		bprog.enqueueExternalEvent(new MotionDetectedEvent(r96_225.getMotionSensor()));

		Thread.sleep(1000);

		bprog.enqueueExternalEvent(new MotionStoppedEvent(r37_123.getMotionSensor()));

		Thread.sleep(1000);

		bprog.enqueueExternalEvent(new Stop());
		bprog.enqueueExternalEvent(new MotionDetectedEvent(r96_226.getMotionSensor()));

		Thread.sleep(1000);

		CTX.close();

	}

	@Test
	void test() throws InterruptedException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(baos);
		PrintStream old = System.out;
		System.setOut(ps);
		main(null);
		System.out.flush();

		System.setOut(old);
		System.out.println(baos.toString());

		assert(baos.toString().startsWith(">>>>>>>>>>>>>>>>>> Rooms example <<<<<<<<<<<<<<<<<<<\r\n" +  
				"---:context.js+ Starting\r\n" + 
				"  -:context.js+ Added ContextReporterBT\r\n" + 
				"  -:context.js+ Added BT1\r\n" + 
				"  -:context.js+ Added BT2\r\n" + 
				"  -:context.js+ Added Room BTs\r\n" + 
				"---:context.js+ Started\r\n" + 
				"  -:context.js+ Added MotionDetectorListenerBT\r\n" + 
				"  -:context.js+ Added NonEmptyRoomListenerListenerBT\r\n" + 
				"  -:context.js+ Added MotionStopDetectorListenerBT\r\n" + 
				" --:context.js+ Event [BEvent name:NewContextEvent(Rooms,Room [37/123])]\r\n" + 
//				"  -:context.js+ Added handler for a new context of type MotionDetector\r\n" + 
//				"  -:context.js+ Added handler for a new context of type MotionStopDetector\r\n" + 
//				" --:context.js+ Event [BEvent name:NewContextEvent(Rooms,Room [96/225])]\r\n" + 
//				"  -:context.js+ Added handler for a new context of type MotionStopDetector\r\n" + 
//				"  -:context.js+ Added handler for a new context of type MotionDetector\r\n" + 
//				" --:context.js+ Event [BEvent name:NewContextEvent(Rooms,Room [96/226])]\r\n" + 
//				"  -:context.js+ Added handler for a new context of type MotionStopDetector\r\n" + 
//				"  -:context.js+ Added handler for a new context of type MotionDetector\r\n" + 
//				" --:context.js+ Event [BEvent name:NewContextEvent(Rooms,Room [96/224])]\r\n" + 
//				"  -:context.js+ Added handler for a new context of type MotionDetector\r\n" + 
//				"  -:context.js+ Added handler for a new context of type MotionStopDetector\r\n" + 
//				"---:context.js+ No Event Selected\r\n" + 
//				" --:context.js+ Event [BEvent name:MotionDetectedEvent(37/123.MotionSensor)]\r\n" + 
//				" --:context.js+ Event [BEvent name:UpdateContexDBEvent(Room.markRoomAsNonEmpty,[room=Room [37/123]])]\r\n" + 
//				"[BP][Info] Room [96/224] is not empty\r\n" + 
//				"[BP][Info] Room [37/123] is not empty\r\n" + 
//				" --:context.js+ Event [BEvent name:NewContextEvent(NonEmptyRooms,Room [37/123])]\r\n" + 
//				"  -:context.js+ Added handler for a new context of type NonEmptyRoomListener\r\n" + 
//				" --:context.js+ Event [BEvent name:Turn on light in room: Room [37/123]]\r\n" + 
//				"  -:context.js+ Done handler for a new context of type NonEmptyRoomListener\r\n" + 
//				" --:context.js+ Event [BEvent name:MotionDetectedEvent(96/225.MotionSensor)]\r\n" + 
//				" --:context.js+ Event [BEvent name:UpdateContexDBEvent(Room.markRoomAsNonEmpty,[room=Room [96/225]])]\r\n" + 
//				"[BP][Info] Room [96/224] is not empty\r\n" + 
//				"[BP][Info] Room [37/123] is not empty\r\n" + 
//				"[BP][Info] Room [96/225] is not empty\r\n" + 
//				" --:context.js+ Event [BEvent name:NewContextEvent(NonEmptyRooms,Room [96/225])]\r\n" + 
//				"  -:context.js+ Added handler for a new context of type NonEmptyRoomListener\r\n" + 
//				" --:context.js+ Event [BEvent name:Turn on light in room: Room [96/225]]\r\n" + 
//				"  -:context.js+ Done handler for a new context of type NonEmptyRoomListener\r\n" + 
//				"---:context.js+ No Event Selected\r\n" + 
//				" --:context.js+ Event [BEvent name:MotionStoppedEvent(37/123.MotionSensor)]\r\n" + 
//				" --:context.js+ Event [BEvent name:UpdateContexDBEvent(Room.markRoomAsEmpty,[room=Room [37/123]])]\r\n" + 
//				" --:context.js+ Event [BEvent name:ContextEndedEvent(NonEmptyRooms,Room [37/123])]\r\n" + 
//				"---:context.js+ No Event Selected\r\n" + 
//				" --:context.js+ Event [BEvent name:Stop]\r\n" + 
//				" --:context.js+ Event [BEvent name:UnsubscribeEvent(NonEmptyRoomListener)]\r\n" + 
//				"  -:context.js+ Removed NonEmptyRoomListenerListenerBT\r\n" + 
//				"  -:context.js+ Done BT2\r\n" + 
//				" --:context.js+ Event [BEvent name:MotionDetectedEvent(96/226.MotionSensor)]\r\n" + 
//				" --:context.js+ Event [BEvent name:UpdateContexDBEvent(Room.markRoomAsNonEmpty,[room=Room [96/226]])]\r\n" + 
//				"[BP][Info] Room [96/224] is not empty\r\n" + 
//				"[BP][Info] Room [96/225] is not empty\r\n" + 
//				"[BP][Info] Room [96/226] is not empty\r\n" + 
//				" --:context.js+ Event [BEvent name:NewContextEvent(NonEmptyRooms,Room [96/226])]\r\n" + 
//				"---:context.js+ No Event Selected"+
				""));
	}
}
