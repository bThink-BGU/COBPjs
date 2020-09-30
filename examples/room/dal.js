importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.room);
// importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);

const anyRoomUpdate = id => bp.EventSet("", function (e) {
    return (e.name.startsWith("RoomIs") || e.name.equals("MotionDetected")) && e.data==id
})
Room("a")
Office("a")

bthread("MinuteHandler", function () {
    let time = bp.sync({waitFor: Any("AddTime")}).data;
    sync({request: CtxInsertEntity(time)}, 200)
    while (true) {
        sync({waitFor: Any('Minute')})
        time.incTime()
        sync({request: CtxUpdateEntity(time)}, 200)
    }
})

bthread('RoomCtx handler', function () {
    while (true) {
        let room = sync({waitFor: Any("AddRoom")}).data;
        sync({request: CtxInsertEntity(room)}, 200)
        bthread("RoomCtx handler: " + room.id, function () {
            while (true) {
                let e = sync({waitFor: anyRoomUpdate(room.id)});
                // bp.log.info("event in room " + room.id)
                if (e.name == "RoomIsNonEmpty") {
                    room.isEmpty = false;
                } else if (e.name == "RoomIsEmpty") {
                    room.isEmpty = true;
                } else if (e.name == "MotionDetected") {
                    let time = getQueryResults("Time.All").get(0).time
                    // bp.log.info("time is "+time)
                    room.lastMovement = time
                }
                sync({request: CtxUpdateEntity(room)}, 200)
            }
        })
    }
})

bthread("populate data", function () {
    sync({request: CtxRegisterQuery("Time.All")}, 100)
    sync({request: CtxRegisterQuery("Room.All")}, 100)
    sync({request: CtxRegisterQuery("Room.NoMovement3")}, 100)
    sync({request: CtxRegisterQuery("Room.Empty")}, 100)
    sync({request: CtxRegisterQuery("Room.Nonempty")}, 100)
    sync({request: CtxRegisterQuery("Office.Empty")}, 100)
    sync({request: CtxRegisterQuery("Office.Nonempty")}, 100)
    sync({request: bp.Event("AddTime", Time())}, 100)
    sync({request: bp.Event("AddRoom", Room("96/224"))}, 100)
    sync({request: bp.Event("AddRoom", Office("96/225"))}, 100)
    // bp.sync({request: bp.Event("AddRoom", Office("37/101"))}, 100)
})
