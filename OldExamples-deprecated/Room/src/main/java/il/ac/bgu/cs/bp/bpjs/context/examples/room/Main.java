package il.ac.bgu.cs.bp.bpjs.context.examples.room;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.effectFunctions.MarkRoomEffect;
import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.effectFunctions.MotionStartedEffect;
import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.effectFunctions.TickEffect;
import il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.rooms.Room;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;

public class Main {
	void run(String dbPopulationScript, String persistenceUnit) throws InterruptedException {
		System.out.println(">>>>>>>>>>>>>>>>>> Rooms example <<<<<<<<<<<<<<<<<<<");

		ContextService contextService = ContextService.getInstance();
		contextService.initFromResources(persistenceUnit, dbPopulationScript, "program.js");
		contextService.addEffectFunction(new MotionStartedEffect());
		contextService.addEffectFunction(new MarkRoomEffect());
		contextService.addEffectFunction(new TickEffect());
		contextService.run();
		BProgram bprog = contextService.getBProgram();

		// Simulation of external events
		Thread.sleep(2000);
		@SuppressWarnings("unchecked")
		Map<String, Room> rooms = ((List<Room>) ContextService.getContextInstances("Room")).stream()
				.collect(Collectors.toMap(Room::getId,Function.identity()));

		bprog.enqueueExternalEvent((rooms.get("224").getMotionDetector()).startedEvent());
		bprog.enqueueExternalEvent(rooms.get("224").getMotionDetector().stoppedEvent());
		bprog.enqueueExternalEvent(rooms.get("224").getMotionDetector().startedEvent());
		bprog.enqueueExternalEvent(rooms.get("224").getMotionDetector().stoppedEvent());
		bprog.enqueueExternalEvent(BEvent.named("tick"));
		bprog.enqueueExternalEvent(BEvent.named("tick"));
		bprog.enqueueExternalEvent(BEvent.named("tick"));
		bprog.enqueueExternalEvent(BEvent.named("tick"));
		bprog.enqueueExternalEvent(rooms.get("224").getMotionDetector().startedEvent());
		Thread.sleep(2000);
		ContextService.getInstance().close();
	}


	public static void main(String[] args) throws InterruptedException {
		new Main().run("db_population.js", "ContextDB");
	}
}
