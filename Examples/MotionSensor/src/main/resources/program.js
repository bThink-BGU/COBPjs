importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.events);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.rooms);

CTX.subscribe("DetectMotionStartInRooms","Room",function (r) {
    bp.sync({ waitFor:MotionDetectedEvent(r.MotionDetector()) });
    bp.sync({ request:CTX.UpdateEvent("Room_markRoomAsNonEmpty", {room: r}) });
});
CTX.subscribe("DetectMotionStopInRooms","Room",function (r) {
    bp.sync({ waitFor:MotionStoppedEvent(r.MotionDetector()) });
    bp.sync({ request:CTX.UpdateEvent("Room_markRoomAsEmpty", {room: r}) });
});

CTX.subscribe("TurnLightsOnInNonemptyRooms","Nonempty Room",function (r) {
    bp.sync({ request:TurnLightOnEvent(r.getSmartLight()) });
});
CTX.subscribe("TurnLightsOffInEmptyRooms","Room",function (r) {
    bp.sync({ request:TurnLightOffEvent(r.getSmartLight()) });
});

CTX.subscribe("TurnACOnInNonemptyOffices","Nonempty Office",function (o) {
    bp.sync({ request:TurnACOnEvent(o.getAirConditioner()) });
});
CTX.subscribe("TurnACOnInEmptyOffices","Office",function (o) {
    bp.sync({ request:TurnACOffEvent(o.getAirConditioner()) });
});

//TODO: Not Working
CTX.subscribe("DisableLightsOffDuringAnEmergency","Emergency",function (e) {
    CTX.subscribe("DisableLightsOffDuringAnEmergency.Room","Room",function (r) {
        bp.sync({block: TurnLightOffEvent(o.getSmartLight())});
    });
});