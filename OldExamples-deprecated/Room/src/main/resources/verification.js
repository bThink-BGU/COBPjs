
CTX.subscribe("Simulate motions","Room",function (room) {
    for (let i = 0; i < 3; i++) {
        bp.sync({request: room.getMotionDetector().startedEvent()}, -10);
        bp.sync({request: room.getMotionDetector().stoppedEvent()}, -10);
    }
});

CTX.subscribe("Simulate minutes", "System", function(system) {
    for (let i = 0; i < 10; i++) {
        bp.sync({ request: bp.Event("tick") }, -10);
    }
});
