importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.room.schema);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.devices);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.room.schema.rooms);

CTX.subscribe("MarkRoomAsNonEmpty", "EmptyRoom", function (room) {
    bp.sync({waitFor: room.getMotionDetector().startedEvent()});
    bp.sync({request: bp.Event("MarkRoomAsNonEmpty", room)});
});
CTX.subscribe("MarkRoomAsEmpty", "NoMovement_3", function (room) {
    bp.sync({
        request: bp.Event("MarkRoomAsEmpty", room),
        waitFor: room.getMotionDetector().startedEvent()
    });
});

CTX.subscribe("Light: On", "NonEmptyRoom", function (room) {
    bp.sync({
        request: room.getSmartLight().onEvent(),
        // interrupt: CTX.AnyContextEndedEvent("NonEmptyRoom", room)
    });
});
CTX.subscribe("Light: Off", "EmptyRoom", function (room) {
    bp.sync({request: room.getSmartLight().offEvent()});
});

CTX.subscribe("Air-conditioner: On", "NonemptyOffice", function (office) {
    bp.sync({request: office.getAirConditioner().onEvent()});
});

CTX.subscribe("Air-conditioner: Off", "EmptyOffice", function (office) {
    bp.sync({request: office.getAirConditioner().offEvent()});
});

CTX.subscribe("Emergency: Lights", "Room", function (room) {
    // while (true) {
        CTX.subscribe("Emergency: Light " + room.id, "Emergency", function (e) {
            bp.sync({block: room.getSmartLight().offEvent(), waitFor: CTX.Ended("Emergency", e)});
        });
    // }
});
