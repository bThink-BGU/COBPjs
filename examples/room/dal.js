
const anyRoomUpdate = id => bp.EventSet("", function (e) {
    return (e.name.startsWith("RoomIs") || e.name.equals("MotionDetected")) && e.data==id
})

function isRoom(type) {
    return type.equals('Room') || type.equals('Office')
}

CTX.registerQuery("Time.All", ctx => ctx.type.equals("Time"))
CTX.registerQuery('Room.NoMovement3', ctx => isRoom(ctx.type) && !ctx.data.isEmpty && CTX.getEntityById("Time").data.value - ctx.data.lastMovement >= 3)
CTX.registerQuery('Room.All', ctx => isRoom(ctx.type))
CTX.registerQuery('Room.Empty', ctx => isRoom(ctx.type) && ctx.data.isEmpty)
CTX.registerQuery('Room.Nonempty', ctx => isRoom(ctx.type) && !ctx.data.isEmpty)
CTX.registerQuery('Office.Empty', ctx => ctx.type.equals("Office") && ctx.data.isEmpty)
CTX.registerQuery('Office.Nonempty', ctx => ctx.type.equals("Office") && !ctx.data.isEmpty)

CTX.registerEffect("AddTime", (bp, e) => {CTX.insertEntity(e.data) })
CTX.registerEffect("Minute", (bp, e) => {
    let time = CTX.getEntityById("Time")
    time.data.value++
    CTX.updateEntity(time)
})
CTX.registerEffect("AddRoom", (bp, e) => {
    CTX.insertEntity(e.data)
})
CTX.registerEffect("RoomIsNonEmpty", (bp, e) => {
    let room = CTX.getEntityById(e.data)
    room.data.isEmpty = false
    CTX.updateEntity(room)
})
CTX.registerEffect("RoomIsEmpty", (bp, e) => {
    let room = CTX.getEntityById(e.data)
    room.data.isEmpty = false
    CTX.updateEntity(room)
})
CTX.registerEffect("MotionDetected", (bp, e) => {
    let room = CTX.getEntityById(e.data)
    room.data.lastMovement = CTX.getEntityById("Time").data.value
    CTX.updateEntity(room)
})

bthread("populate data", function () {
    sync({request: bp.Event("AddTime", CTX.createEntity("Time","Time",{value:3}))}, 100)
    sync({request: bp.Event("AddRoom", CTX.createEntity("96/224","Room",{light:"96/224.light", isEmpty:true, lastMovement:0}))}, 100)
    sync({request: bp.Event("AddRoom", CTX.createEntity("96/225","Room",{light:"96/225.light", isEmpty:true, lastMovement:0}))}, 100)
})
