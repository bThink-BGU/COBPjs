// -------------------–  determine if we are in a context ------------------------

cbt("MarkNonEmptyUponMotion", "Room.Empty", function (room) {
    sync({ waitFor: bp.Event("MotionDetected", room.id) });
    sync({request: bp.Event("RoomIsNonEmpty", room.id)});
});
cbt("MarkEmptyIfNoMovement", 'Room.NoMovement3', function (room) {
    sync({
        request: bp.Event("RoomIsEmpty", room.id),
        waitFor: bp.Event("MotionDetected", room.id)
    });
});


// -------------------–  what to do in a context ------------------------

cbt("Light: On", "Room.Nonempty", function (room) {
    sync({
        request: bp.Event("On", room.data.light),
    });
});
cbt("Light: Off", "Room.Empty", function (room) {
    sync({request: bp.Event("Off", room.data.light)});
});

cbt("Air-conditioner: On", "Office.Nonempty", function (office) {
    sync({request: bp.Event("On", office.airConditioner)});
});

cbt("Air-conditioner: Off", "Office.Empty", function (office) {
    sync({request: bp.Event("Off", office.airConditioner)});
});
