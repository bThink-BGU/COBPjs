cbt("MarkNonEmptyUponMotion", dal.EmptyRoom, function (room) {
    sync({ waitFor: bp.Event("MotionDetected", room.id) });
    sync({request: bp.Event("RoomIsNonEmpty", room.id)});
});
cbt("MarkEmptyIfNoMovement", dal.NoMovement_3, function (room) {
    sync({
        request: bp.Event("RoomIsEmpty", room.id),
        waitFor: bp.Event("MotionDetected", room.id)
    });
});

//Critically Raises the number of states because room is updated all the time
/*cbt("MarkNonEmptyUponMotion", dal.EmptyRoom, function (room) {
    bp.sync({ waitFor: AnyInContext("MotionDetected", room) });
    bp.sync({request: bp.Event("RoomIsNonEmpty", room)});
});
cbt("MarkEmptyIfNoMovement", dal.NoMovement_3, function (room) {
    bp.sync({
        request: bp.Event("RoomIsEmpty", room),
        waitFor: AnyInContext("MotionDetected", room)
    });
});*/

cbt("Light: On", dal.NonEmptyRoom, function (room) {
    sync({
        request: bp.Event("On", room.light),
        // interrupt: CTX.AnyContextEndedEvent("NonEmptyRoom", room)
    });
});
cbt("Light: Off", dal.EmptyRoom, function (room) {
    sync({request: bp.Event("Off", room.light)});
});

cbt("Air-conditioner: On", dal.NonEmptyOffice, function (office) {
    sync({request: bp.Event("On", office.airConditioner)});
});

cbt("Air-conditioner: Off", dal.EmptyOffice, function (office) {
    sync({request: bp.Event("Off", office.AirConditioner)});
});