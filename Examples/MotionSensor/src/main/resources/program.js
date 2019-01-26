importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.events);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.rooms);

CTX.subscribe("DetectMotionStartInRooms","Room",function (room) {
    bp.sync({ waitFor:MotionDetectedEvent(room.getMotionDetector()) });
    bp.sync({ request:CTX.UpdateEvent("MarkRoomAsNonEmpty", {room: room}) });
});
CTX.subscribe("DetectMotionStopInRooms","Room",function (room) {
    bp.sync({ waitFor:MotionStoppedEvent(room.getMotionDetector()) });
    bp.sync({ request:CTX.UpdateEvent("MarkRoomAsEmpty", {room: room}) });
});

CTX.subscribe("DelayRoomAsEmpty","Room",function (room) {
    // var numberOfTicks = 60 * 3;
    var numberOfTicks = 5;
    var motionDetectedEvent = MotionDetectedEvent(room.getMotionDetector());
    var i = 0;
    while(true) {
        bp.sync({waitFor: MotionStoppedEvent(room.getMotionDetector())});
        bp.registerBThread("DelayRoomAsEmptyHelper_"+(i++), function() {
            var e = bp.sync({
                waitFor: CTX.AnyTickEvent(),
                block: CTX.UpdateEvent("MarkRoomAsEmpty", {room: room}),
                interrupt: motionDetectedEvent,
            });
            bp.sync({
                waitFor: CTX.TickEvent(e.tick + numberOfTicks),
                block: CTX.UpdateEvent("MarkRoomAsEmpty", {room: room}),
                interrupt: motionDetectedEvent,
            });
        });
    }
});

CTX.subscribe("TurnLightsOnInNonemptyRooms","NonEmptyRoom",function (room) {
    bp.sync({
        request:TurnLightOnEvent(room.getSmartLight()),
        interrupt: CTX.ContextEndedEvent("NonEmptyRoom", room)
    });
});
CTX.subscribe("TurnLightsOffInEmptyRooms","Room",function (room) {
    bp.sync({ request:TurnLightOffEvent(room.getSmartLight()) });
});

CTX.subscribe("OfficeBehaviors","Office",function (office) {
    CTX.subscribe("TurnACOnInNonemptyOffices","NonEmptyRoom",function (room) {
        if(office === room) {
            bp.sync({ request:TurnACOnEvent(room.getAirConditioner()) });
        }
    });
    CTX.subscribe("TurnACOffInEmptyOffices","Room",function (room) {
        if(office === room) {
            bp.sync({ request:TurnACOffEvent(room.getAirConditioner()) });
        }
    });
});

CTX.subscribe("DisableLightsOffDuringAnEmergency","Emergency",function (emergency) {
    bp.sync({block: AnyTurnLightOffEvent(),
        interrupt: CTX.ContextEndedEvent("Emergency", emergency)});
});