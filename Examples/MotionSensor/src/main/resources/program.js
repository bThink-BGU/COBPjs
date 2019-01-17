importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.events);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema);

// ---------------------------------------------
bp.registerBThread("BT2", function() {
	bp.sync({ waitFor:bp.Event("Stop") });
	bp.sync({ request:CTX.UnsubscribeEvent("NonEmptyRoomListener") });
});
// ---------------------------------------------

CTX.subscribe("MotionDetector","Rooms", function(room) {
	while(true) {
		bp.sync({ waitFor:MotionDetectedEvent(room.getMotionSensor()) });
		bp.sync({ request:CTX.UpdateEvent("Room.markRoomAsNonEmpty", { room: room }) });

		// Print the list of nonempty rooms
		for each (var r in CTX.getContextsOfType("NonEmptyRooms")) {
			bp.log.info(r + " is not empty");
		}
	}
});

CTX.subscribe("MotionStopDetector", "Rooms", function(room) {
	while(true) {
		bp.sync({ waitFor:MotionStoppedEvent(room.getMotionSensor()) });
		bp.sync({ request:CTX.UpdateEvent("Room.markRoomAsEmpty", { room: room }) });
	}
});

// ---------------------------------------------
bp.registerBThread("InitDB", function() {
	room1 = new Room("96/224");
	room2 = new Room("96/225");
	office1 = new Office("37/123");
	office2 = new Office("96/226");
	CTX.populateDB([room1, room2, office1, office2]);
});
// ---------------------------------------------