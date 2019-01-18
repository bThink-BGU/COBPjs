importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.events);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.devices);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.roomsexample.schema.rooms);

CTX.subscribe("DetectMotionStartInRooms","Room",function (r) {
	bp.sync({ waitFor:MotionDetectedEvent(r.MotionDetector()) });
	bp.sync({ request:CTX.update("Room_markRoomAsNonEmpty", {room: r}) });
});
CTX.subscribe("DetectMotionStopInRooms","Room",function (r) {
	bp.sync({ waitFor:MotionStoppedEvent(r.MotionDetector()) });
	bp.sync({ request:CTX.update("Room_markRoomAsEmpty", {room: r}) });
});

CTX.subscribe("TurnLightsOnInNonemptyRooms","Nonempty Room",function (r) {
	bp.sync({ request:TurnLightOnEvent(r.getSmartLight()) });
});
CTX.subscribe("TurnLightsOffInEmptyRooms","Room",function (r) {
	bp.sync({ request:TurnLightOffEvent(r.getSmartLight()) });
});

CTX.subscribe("TurnACOnInNonemptyOffices","Nonempty Office",function (o) {
	bp.sync({ request:TurnACOnEvent(o.getAirConditioner()) });
});
CTX.subscribe("TurnACOnInEmptyOffices","Office",function (o) {
	bp.sync({ request:TurnACOffEvent(o.getAirConditioner()) });
});

//TODO: Not Working
CTX.subscribe("DisableLightsOffDuringAnEmergency","Emergency",function (e) {
	CTX.subscribe("DisableLightsOffDuringAnEmergency.Room","Room",function (r) {
		bp.sync({block: TurnLightOffEvent(o.getSmartLight())});
	});
});

//region DB Population
bp.registerBThread("PopulateDB", function() {
	var achiya = new Worker(000000000, "Achiya Elyasaf");
	var gera = new Worker(111111111, "Gera Weiss");
	var arnon = new Worker(222222222, "Arnon Sturm");
	var office96_224 = new Office("96/224", achiya);
	var office96_225 = new Office("96/224", arnon);
	var office37_123 = new Office("37/123", gera);
	CTX.populateDB([achiya,gera,arnon,office37_123,office96_224,office96_225]);
});
//endregion