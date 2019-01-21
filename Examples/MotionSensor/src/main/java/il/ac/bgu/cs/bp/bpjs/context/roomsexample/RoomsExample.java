package il.ac.bgu.cs.bp.bpjs.context.roomsexample;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import il.ac.bgu.cs.bp.bpjs.context.roomsexample.events.MotionDetectedEvent;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.events.MotionStoppedEvent;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.Emergency;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.rooms.Room;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import org.junit.jupiter.api.Test;

import il.ac.bgu.cs.bp.bpjs.context.CTX;
import il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.rooms.Office;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

public class RoomsExample {

	public static void main(String[] args) throws InterruptedException {
		System.out.println(">>>>>>>>>>>>>>>>>> Rooms example <<<<<<<<<<<<<<<<<<<");

		CTX.init();

        /*CTX.registerContextQuery("Room", Room.class);
		CTX.registerContextQuery("Nonempty Room", Room.class);
		CTX.registerContextQuery("Office", Office.class);
		CTX.registerContextQuery("Emergency", Emergency.class);*/

		BProgram bprog = CTX.run("db_population.js", "program.js");

		// Simulation of external events
		Thread.sleep(1000);
		Room[] rooms = CTX.getContextsOfType("Room.findAll", Room.class);

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
