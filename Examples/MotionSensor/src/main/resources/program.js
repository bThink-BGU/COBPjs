importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.events);
importPackage(Packages.java.util);

// ---------------------------------------------
bp.registerBThread("BT1", function() {
	CTX.subscribe("NonEmptyRoomListener", "NonEmptyRooms", function(room) {
		bp.sync({ request:bp.Event("Turn on light in room: " + room ) });
	});

});
// ---------------------------------------------

// ---------------------------------------------
bp.registerBThread("BT2", function() {
	bp.sync({ waitFor:bp.Event("Stop") });
	bp.sync({ request:UnsubscribeEvent("NonEmptyRoomListener") });
});
// ---------------------------------------------


// ---------------------------------------------
bp.registerBThread("Room BTs", function() {

	CTX.subscribe("MotionDetector","Rooms", function(room) {
		while(true) {
			bp.sync({ waitFor:MotionDetectedEvent(room.getMotionSensor()) });
			bp.sync({ request:UpdateContexDBEvent("Room.markRoomAsNonEmpty", { room: room }) });
			
			// Print the list of nonempty rooms
			for each (var r in CTX.getContextsOfType("NonEmptyRooms")) {
				bp.log.info(r + " is not empty");
			}
		}
	});


	CTX.subscribe("MotionStopDetector", "Rooms", function(room) {
		while(true) {
			bp.sync({ waitFor:MotionStoppedEvent(room.getMotionSensor()) });
			bp.sync({ request:UpdateContexDBEvent("Room.markRoomAsEmpty", { room: room }) });
		}
	});
	
});
// ---------------------------------------------
