importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.room.schema);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.devices);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.rooms);

CTX.subscribe("DetectMotionStartInRooms","Room",function (room) {
    bp.sync({ waitFor: room.getMotionDetector().startedEvent() });
    bp.sync({ request:bp.Event("MarkRoomAsNonEmpty", {room: room}) });
});
CTX.subscribe("DetectMotionStopInRooms","Room",function (room) {
    bp.sync({ waitFor: room.getMotionDetector().stoppedEvent() });
    bp.sync({ request:bp.Event("MarkRoomAsEmpty", {room: room}) });
});

CTX.subscribe("DelayRoomAsEmpty","Room",function (room) {
    // var numberOfTicks = 60 * 3;
    var numberOfTicks = 5;
    var motionDetectedEvent = room.getMotionDetector().startedEvent();
    var i = 0;
    while(true) {
        bp.sync({waitFor: room.getMotionDetector().stoppedEvent()});
        bp.registerBThread("DelayRoomAsEmptyHelper_"+(i++), function() {
            var e = bp.sync({
                waitFor: CTX.AnyTickEvent(),
                block: bp.Event("MarkRoomAsEmpty", {room: room}),
                interrupt: motionDetectedEvent,
            });
            bp.sync({
                waitFor: CTX.TickEvent(e.tick + numberOfTicks),
                block: bp.Event("MarkRoomAsEmpty", {room: room}),
                interrupt: motionDetectedEvent,
            });
        });
    }
});

CTX.subscribe("TurnLightsOnInNonemptyRooms","NonEmptyRoom",function (room) {
    bp.sync({
        request: room.getSmartLight().onEvent(),
        interrupt: CTX.AnyContextEndedEvent("NonEmptyRoom", room)
    });
});
CTX.subscribe("TurnLightsOffInEmptyRooms","Room",function (room) {
    bp.sync({ request: room.getSmartLight().offEvent() });
});

CTX.subscribe("OfficeBehaviors","Office",function (office) {
    CTX.subscribe("TurnACOnInNonemptyOffices","NonEmptyRoom",function (room) {
        if(office === room) {
            bp.sync({ request: room.getAirConditioner().onEvent() });
        }
    });
    CTX.subscribe("TurnACOffInEmptyOffices","Room",function (room) {
        if(office === room) {
            bp.sync({ request: room.getAirConditioner().offEvent() });
        }
    });
});

CTX.subscribe("DisableLightsOffDuringAnEmergency1","Emergency",function (emergency) {
    bp.sync({block: SmartLight.AnyTurnLightOffEvent(),
        interrupt: CTX.AnyContextEndedEvent("Emergency", emergency)});
});

CTX.subscribe("DisableLightsOffDuringAnEmergency2","Emergency",function (emergency) {
    CTX.subscribe("RoomDuringEmergency","Room", function(room) {
        bp.sync({
            block: room.getSmartLight().offEvent(),
            interrupt: CTX.AnyContextEndedEvent("Emergency", emergency)
        });
    });
});