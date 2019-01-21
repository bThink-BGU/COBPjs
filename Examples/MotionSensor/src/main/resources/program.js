importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.events);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.rooms);

CTX.subscribe("DetectMotionStartInRooms","Room.findAll",function (room) {
    bp.sync({ waitFor:MotionDetectedEvent(room.getMotionDetector()) });
    bp.sync({ request:CTX.UpdateEvent("Room.markAsNonEmpty", {room: room}) });
});
CTX.subscribe("DetectMotionStopInRooms","Room.findAll",function (room) {
    bp.sync({ waitFor:MotionStoppedEvent(room.getMotionDetector()) });
    bp.sync({ request:CTX.UpdateEvent("Room.markAsEmpty", {room: room}) });
});

CTX.subscribe("DelayRoomAsEmpty","Room.findAll",function (room) {
    // var numberOfTicks = 60 * 3;
    var numberOfTicks = 5;
    var motionDetectedEvent = MotionDetectedEvent(room.getMotionDetector());
    var i = 0;
    while(true) {
        bp.sync({waitFor: MotionStoppedEvent(room.getMotionDetector())});
        bp.registerBThread("DelayRoomAsEmptyHelper_"+(i++), function() {
            var e = bp.sync({
                waitFor: CTX.AnyTickEvent(),
                block: CTX.UpdateEvent("Room.markAsEmpty", {room: room}),
                interrupt: motionDetectedEvent,
            });
            bp.sync({
                waitFor: CTX.TickEvent(e.tick + numberOfTicks),
                block: CTX.UpdateEvent("Room.markAsEmpty", {room: room}),
                interrupt: motionDetectedEvent,
            });
        });
    }
});

CTX.subscribe("TurnLightsOnInNonemptyRooms","Room.findAllNonEmpty",function (room) {
    bp.sync({
        request:TurnLightOnEvent(room.getSmartLight()),
        interrupt: CTX.ContextEndedEvent("Room.findAllNonEmpty", room)
    });
});
CTX.subscribe("TurnLightsOffInEmptyRooms","Room.findAll",function (room) {
    bp.sync({ request:TurnLightOffEvent(room.getSmartLight()) });
});

CTX.subscribe("OfficeBehaviors","Office.findAll",function (office) {
    CTX.subscribe("TurnACOnInNonemptyOffices","Room.findAllNonEmpty",function (room) {
        if(office === room) {
            bp.sync({ request:TurnACOnEvent(room.getAirConditioner()) });
        }
    });
    CTX.subscribe("TurnACOffInEmptyOffices","Room.findAll",function (room) {
        if(office === room) {
            bp.sync({ request:TurnACOffEvent(room.getAirConditioner()) });
        }
    });
});

CTX.subscribe("DisableLightsOffDuringAnEmergency","Emergency.findAll",function (emergency) {
    bp.sync({block: AnyTurnLightOffEvent(),
        interrupt: CTX.ContextEndedEvent("Emergency.findAll", emergency)});
});