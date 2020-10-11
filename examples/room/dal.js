importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.examples.room);
// importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);

const anyRoomUpdate = id => bp.EventSet("", function (e) {
    return (e.name.startsWith("RoomIs") || e.name.equals("MotionDetected")) && e.data==id
})

function isRoom(type) {
    return type.equals('Room') || type.equals('Office')
}

let queries = {}

queries.Minute = {}
queries.Minute.query = ctx => ctx.type.equals("Time")
queries.Minute.name = 'Time.All'
ContextService.GetInstance().registerQuery(queries.Minute.name, queries.Minute.query)

queries.NoMovement3 = {}
queries.NoMovement3.query = ctx => isRoom(ctx.type) && !ctx.data.isEmpty && ContextService.GetInstance().getQueryResults(queries.Minute.name).get(0).data.value - ctx.data.lastMovement >= 3
queries.NoMovement3.name = 'Room.NoMovement3'
ContextService.GetInstance().registerQuery(queries.NoMovement3.name, queries.NoMovement3.query)

queries.Room = {}
queries.Room.query = ctx => isRoom(ctx.type)
queries.Room.name = 'Room.All'
ContextService.GetInstance().registerQuery(queries.Room.name, queries.Room.query)

queries.EmptyRoom = {}
queries.EmptyRoom.query = ctx => isRoom(ctx.type) && ctx.data.isEmpty
queries.EmptyRoom.name = 'Room.Empty'
ContextService.GetInstance().registerQuery(queries.EmptyRoom.name, queries.EmptyRoom.query)

queries.NonEmptyRoom = {}
queries.NonEmptyRoom.query = ctx => isRoom(ctx.type) && !ctx.data.isEmpty
queries.NonEmptyRoom.name = 'Room.Nonempty'
ContextService.GetInstance().registerQuery(queries.NonEmptyRoom.name, queries.NonEmptyRoom.query)

queries.EmptyOffice = {}
queries.EmptyOffice.query = ctx => ctx.type.equals("Office") && ctx.data.isEmpty
queries.EmptyOffice.name = 'Office.Empty'
ContextService.GetInstance().registerQuery(queries.EmptyOffice.name, queries.EmptyOffice.query)

queries.NonEmptyOffice = {}
queries.NonEmptyOffice.query = ctx => ctx.type.equals("Office") && !ctx.data.isEmpty
queries.NonEmptyOffice.name = 'Office.Nonempty'
ContextService.GetInstance().registerQuery(queries.NonEmptyOffice.name, queries.NonEmptyOffice.query)

/*dal.SpecificRoom = {}
dal.SpecificRoom.query = id => (ctx => isRoom(ctx.type) && ctx.id.equals(id))
dal.SpecificRoom.name = 'SpecificRoom'*/

let effects = {}

effects.AddTime = {}
effects.AddTime.name = "AddTime"
effects.AddTime.effect = (bp, e) => {
    if(e.name.equals("AddTime")) {
        ContextService.GetInstance().insertEntity(e.data)
    }
}
ContextService.GetInstance().addEffectFunction(effects.AddTime.effect)

effects.Minute = {}
effects.Minute.name = "Minute"
effects.Minute.effect = (bp, e) => {
    if(e.name.equals("Minute")) {
        let time = ContextService.GetInstance().getEntity("Time")
        time.data.value++
        ContextService.GetInstance().updateEntity(time)
    }
}
ContextService.GetInstance().addEffectFunction(effects.Minute.effect)

effects.AddRoom = {}
effects.AddRoom.name = "AddRoom"
effects.AddRoom.effect = (bp, e) => {
    if(e.name.equals("AddRoom")) {
        ContextService.GetInstance().insertEntity(e.data)
    }
}
ContextService.GetInstance().addEffectFunction(effects.AddRoom.effect)

effects.RoomIsNonEmpty = {}
effects.RoomIsNonEmpty.name = "RoomIsNonEmpty"
effects.RoomIsNonEmpty.effect = (bp, e) => {
    if(e.name.equals("RoomIsNonEmpty")) {
        let room = ContextService.GetInstance().getEntity(e.data)
        room.data.isEmpty = false
        ContextService.GetInstance().updateEntity(room)
    }
}
ContextService.GetInstance().addEffectFunction(effects.RoomIsNonEmpty.effect)

effects.RoomIsEmpty = {}
effects.RoomIsEmpty.name = "RoomIsEmpty"
effects.RoomIsEmpty.effect = (bp, e) => {
    if(e.name.equals("RoomIsEmpty")) {
        let room = ContextService.GetInstance().getEntity(e.data)
        room.data.isEmpty = false
        ContextService.GetInstance().updateEntity(room)
    }
}
ContextService.GetInstance().addEffectFunction(effects.RoomIsEmpty.effect)

effects.MotionDetected = {}
effects.MotionDetected.name = "MotionDetected"
effects.MotionDetected.effect = (bp, e) => {
    if(e.name.equals("MotionDetected")) {
        let room = ContextService.GetInstance().getEntity(e.data)
        room.data.lastMovement = ContextService.GetInstance().getEntity("Time").data.value
        ContextService.GetInstance().updateEntity(room)
    }
}
ContextService.GetInstance().addEffectFunction(effects.MotionDetected.effect)

bthread("populate data", function () {
    sync({request: bp.Event("AddTime", ContextEntity("Time","Time",{value:3}))}, 100)
    sync({request: bp.Event("AddRoom", ContextEntity("96/224","Room",{light:"96/224.light", isEmpty:true, lastMovement:0}))}, 100)
    sync({request: bp.Event("AddRoom", ContextEntity("96/225","Room",{light:"96/225.light", isEmpty:true, lastMovement:0}))}, 100)
})
