importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.events);
importPackage(Packages.java.util);

// ---------------------------------------------
bp.registerBThread("BT1", function() {

	// Wait for any event to start
	// bp.sync({ waitFor:bp.all });
	
	// Subscribe to turn on the light when a room becomes empty
	subscribe("NonEmptyRoomListener", "NonEmptyRooms", function(room) {
		bp.sync({ request:bp.Event("Turn on light in room: " + room ) });
	});

});
// ---------------------------------------------

// ---------------------------------------------
bp.registerBThread("BT2", function() {
	bp.sync({ waitFor:bp.Event("Stop") });
	bp.sync({ request:Unsubscribe("NonEmptyRoomListener") });
});
// ---------------------------------------------


// ---------------------------------------------
bp.registerBThread("Room BTs", function() {
	subscribe("Rooms", function(room) {
		while(true) {
			bp.sync({ waitFor:MotionStoppedEvent(room.getMotionSensor()) });
			bp.sync({ request:UpdateContexDBEvent("Room.markRoomAsEmpty", { room: room }) });
		}
	});
	
	subscribe("Rooms", function(room) {
		bp.log.info("ROOM IS " + room);
		bp.log.info("sensor IS " + room.getMotionSensor());
		while(true) {
			bp.sync({ waitFor:MotionDetectedEvent(room.getMotionSensor()) });
			bp.sync({ request:UpdateContexDBEvent("Room.markRoomAsNonEmpty", { room: room }) });
			
			for each (var r in CTX.getContextsOfType("NonEmptyRooms")) {
				bp.log.info(r + " is not empty");
			}
		}
	});
});
// ---------------------------------------------
