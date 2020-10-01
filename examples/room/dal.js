importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.room);
// importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);

const anyRoomUpdate = id => bp.EventSet("", function (e) {
    return (e.name.startsWith("RoomIs") || e.name.equals("MotionDetected")) && e.data==id
})

function isRoom(type) {
    return type.equals('Room') || type.equals('Office')
}

let dal = {}

dal.Minute = {}
dal.Minute.query = ctx => ctx.type.equals("Minute")
dal.Minute.name = 'Time.All'
ContextService.GetInstance().registerQuery(dal.Minute.name, dal.Minute.query)

dal.NoMovement3 = {}
dal.NoMovement3.query = ctx => isRoom(ctx.type) && !ctx.data.isEmpty && getQueryResults(dal.Minute.query).get(0).data.value - ctx.data.lastMovement >= 3
dal.NoMovement3.name = 'Room.NoMovement3'
ContextService.GetInstance().registerQuery(dal.NoMovement3.name, dal.NoMovement3.query)

dal.Room = {}
dal.Room.query = ctx => isRoom(ctx.type)
dal.Room.name = 'Room.All'
ContextService.GetInstance().registerQuery(dal.Room.name, dal.Room.query)

dal.EmptyRoom = {}
dal.EmptyRoom.query = ctx => isRoom(ctx.type) && ctx.data.isEmpty
dal.EmptyRoom.name = 'Room.Empty'
ContextService.GetInstance().registerQuery(dal.EmptyRoom.name, dal.EmptyRoom.query)

dal.NonEmptyRoom = {}
dal.NonEmptyRoom.query = ctx => isRoom(ctx.type) && !ctx.data.isEmpty
dal.NonEmptyRoom.name = 'Room.Nonempty'
ContextService.GetInstance().registerQuery(dal.NonEmptyRoom.name, dal.NonEmptyRoom.query)

dal.EmptyOffice = {}
dal.EmptyOffice.query = ctx => ctx.type.equals("Office") && ctx.data.isEmpty
dal.EmptyOffice.name = 'Office.Empty'
ContextService.GetInstance().registerQuery(dal.EmptyOffice.name, dal.EmptyOffice.query)

dal.NonEmptyOffice = {}
dal.NonEmptyOffice.query = ctx => ctx.type.equals("Office") && !ctx.data.isEmpty
dal.NonEmptyOffice.name = 'Office.Nonempty'
ContextService.GetInstance().registerQuery(dal.NonEmptyOffice.name, dal.NonEmptyOffice.query)

/*dal.SpecificRoom = {}
dal.SpecificRoom.query = id => (ctx => isRoom(ctx.type) && ctx.id.equals(id))
dal.SpecificRoom.name = 'SpecificRoom'*/



bthread("MinuteHandler", function () {
    let time = sync({waitFor: Any("AddTime")}).data;
    sync({request: CtxInsertEntity(time)}, 200)
    while (true) {
        sync({waitFor: Any('Minute')})
        time.data.value++
        sync({request: CtxUpdateEntity(time)}, 200)
    }
})

bthread('RoomCtx handler', function () {
    while (true) {
        let room = sync({waitFor: Any("AddRoom")}).data
        sync({request: CtxInsertEntity(room)}, 200)
        bthread("RoomCtx handler: " + room.id, function () {
            while (true) {
                let e = sync({waitFor: anyRoomUpdate(room.id)})
                // bp.log.info("event in room " + room.id)
                if (e.name == "RoomIsNonEmpty") {
                    room.data.isEmpty = false;
                } else if (e.name == "RoomIsEmpty") {
                    room.data.isEmpty = true;
                } else if (e.name == "MotionDetected") {
                    let time = getQueryResults("Time.All").get(0).time
                    // bp.log.info("time is "+time)
                    room.data.lastMovement = time
                }
                sync({request: CtxUpdateEntity(room)}, 200)
            }
        })
    }
})

bthread("populate data", function () {
    /*sync({request: CtxRegisterQuery("Time.All")}, 100)
    sync({request: CtxRegisterQuery("Room.All")}, 100)
    sync({request: CtxRegisterQuery("Room.NoMovement3")}, 100)
    sync({request: CtxRegisterQuery("Room.Empty")}, 100)
    sync({request: CtxRegisterQuery("Room.Nonempty")}, 100)
    // sync({request: CtxRegisterQuery("Office.Empty")}, 100)
    // sync({request: CtxRegisterQuery("Office.Nonempty")}, 100)*/
    sync({request: bp.Event("AddTime", ContextEntity("Time","Time",{value:3}))}, 100)
    sync({request: bp.Event("AddRoom", ContextEntity("Room","96/224",{light:"96/224.light", isEmpty:true, lastMovement:0}))}, 100)
    // bp.sync({request: bp.Event("AddRoom", Office("37/101"))}, 100)
})
