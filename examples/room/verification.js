bthread("SimulateMinutes",function () {
    for (let i = 4; i <= 20; i++) {
        sync({request: bp.Event("Minute", i)},-50)
    }
})

cbt("SimulateMovements", queries.Room.name, function (room) {
    sync({waitFor: Any("Minute")})
    sync({ request: bp.Event("MotionDetected", room.id) }, -50);
    //sync({ request: bp.Event("MotionDetected", room.id) }, -50);
});