cbt("MarkNonEmptyUponMotion", queries.EmptyRoom.name, function (room) {
    sync({ waitFor: bp.Event("MotionDetected", room.id) });
    sync({request: bp.Event("RoomIsNonEmpty", room.id)});
});
cbt("MarkEmptyIfNoMovement", queries.NoMovement3.name, function (room) {
    sync({
        request: bp.Event("RoomIsEmpty", room.id),
        waitFor: bp.Event("MotionDetected", room.id)
    });
});

cbt("Light: On", queries.NonEmptyRoom.name, function (room) {
    sync({
        request: bp.Event("On", room.data.light),
    });
});
cbt("Light: Off", queries.EmptyRoom.name, function (room) {
    sync({request: bp.Event("Off", room.data.light)});
});

cbt("Air-conditioner: On", "Office.Nonempty", function (office) {
    sync({request: bp.Event("On", office.airConditioner)});
});

cbt("Air-conditioner: Off", "Office.Empty", function (office) {
    sync({request: bp.Event("Off", office.airConditioner)});
});
