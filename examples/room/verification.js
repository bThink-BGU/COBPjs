bthread("SimulateMinutes",function () {
    for (let i = 4; i <= 10; i++) {
        sync({request: bp.Event("Minute")},-50)
    }
})

cbt("SimulateMovements", dal.Room, function (room) {
    sync({waitFor: Any("Minute")})
    sync({ request: bp.Event("MotionDetected", room.id) }, -50);
    // sync({ request: bp.Event("MotionDetected", room.id) }, -50);
});