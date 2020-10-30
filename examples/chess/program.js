// -------------------–  determine if we are in a context ------------------------

cbt("MarkNonEmptyUponMotion", "Room.Empty", function (room) {
    sync({ waitFor: bp.Event("MotionDetected", room.id) });
    sync({request: bp.Event("RoomIsNonEmpty", room.id)});

});
