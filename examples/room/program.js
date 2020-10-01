cbt("MarkNonEmptyUponMotion", "Room.Empty", function (room) {
    sync({ waitFor: bp.Event("MotionDetected", room.id) });
    sync({request: bp.Event("RoomIsNonEmpty", room.id)});
});
cbt("MarkEmptyIfNoMovement", "Room.NoMovement3", function (room) {
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

cbt("Light: On", "Room.Nonempty", function (room) {
    sync({
        request: bp.Event("On", room.light),
        // interrupt: CTX.AnyContextEndedEvent("NonEmptyRoom", room)
    });
});
cbt("Light: Off", "Room.Empty", function (room) {
    sync({request: bp.Event("Off", room.light)});
});

cbt("Air-conditioner: On", "Office.Nonempty", function (office) {
    sync({request: bp.Event("On", office.airConditioner)});
});

cbt("Air-conditioner: Off", "Office.Empty", function (office) {
    sync({request: bp.Event("Off", office.airConditioner)});
});