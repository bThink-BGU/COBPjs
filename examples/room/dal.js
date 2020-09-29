function isRoom(type) {
    return type.equals('Room') || type.equals('Office')
}

let dal = {}

dal.Tick = {}
dal.Tick.query = ctx => ctx.type == 'Tick'
dal.Tick.name = 'Tick'

dal.NoMovement_3 = {}
dal.NoMovement_3.query = ctx => isRoom(ctx.type) && !ctx.isEmpty && getQueryResults(dal.Tick.query)[0].value - ctx.lastMovement >= 3
dal.NoMovement_3.name = 'NoMovement_3'

dal.Room = {}
dal.Room.query = ctx => isRoom(ctx.type)
dal.Room.name = 'Room'

dal.EmptyRoom = {}
dal.EmptyRoom.query = ctx => isRoom(ctx.type) && ctx.isEmpty
dal.EmptyRoom.name = 'EmptyRoom'

dal.NonEmptyRoom = {}
dal.NonEmptyRoom.query = ctx => isRoom(ctx.type) && !ctx.isEmpty
dal.NonEmptyRoom.name = 'NonEmptyRoom'

dal.EmptyOffice = {}
dal.EmptyOffice.query = ctx => ctx.type == 'Office' && ctx.isEmpty
dal.EmptyOffice.name = 'EmptyOffice'

dal.NonEmptyOffice = {}
dal.NonEmptyOffice.query = ctx => ctx.type == 'Office' && !ctx.isEmpty
dal.NonEmptyOffice.name = 'NonEmptyOffice'

dal.SpecificRoom = {}
dal.SpecificRoom.query = id => (ctx => isRoom(ctx.type) && ctx.id == id)
dal.SpecificRoom.name = 'SpecificRoom'

const anyRoomUpdate = bp.EventSet("", function (e) {
    return e.name.startsWith("RoomIs") || e.name.equals("MotionDetected") || e.name.equals("AddRoom")
})

bthread("tickHandler", function () {
    let ctx = {type: 'Tick', value: 0}
    while (true) {
        let e = bp.sync({waitFor: Any('Minute')})
        if (e.data) {
            ctx.value = e.data
            startContext(ctx)
        } else {
            ctx.value++
            updateContext(ctx)
        }
    }
})

bthread('RoomCtx handler', function () {
    while (true) {
        let ctx = bp.sync({waitFor: Any("AddRoom")}).data;
        startContext(ctx);
        bthread("RoomCtx handler: " + ctx.name, function () {
            while(true) {
                let e = bp.sync({waitFor: anyRoomUpdate});
                if (e.name == "RoomIsNonEmpty") {
                    ctx.isEmpty = false;
                } else if (e.name == "RoomIsEmpty") {
                    ctx.isEmpty = true;
                } else if (e.name == "MotionDetected") {
                    ctx.lastMovement = getQueryResults(dal.Tick.query)[0].value
                }
                updateContext(ctx)
            }
        })
    }
})

bthread("populate data", function () {
    bp.sync({request: bp.Event("Minute", 3)}, 100)
    bp.sync({
        request: bp.Event("AddRoom", {
            type: 'Room', name: "96/224",
            isEmpty: true, lastMovement: 0,
            light: "96/224.light", airConditioner: "96/224.airConditioner"
        })
    }, 100)
    /*bp.sync({
        request: bp.Event("AddRoom", {
            type: 'Room', name: "96/225",
            isEmpty: true, lastMovement: 0,
            light: "96/225.light", airConditioner: "96/225.airConditioner"
        })
    }, 100)
    bp.sync({
        request: bp.Event("AddRoom", {
            type: 'Office', name: "37/101",
            isEmpty: true, lastMovement: 0,
            light: "37/101.light", airConditioner: "37/101.airConditioner"
        })
    }, 100)*/
})
