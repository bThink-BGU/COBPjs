importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.events);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);

var subscription_id = 0;

function subscribe(ctxName, func) {
	var id = "SID_" + (subscription_id++);
	return subscribe(id, ctxName, func);
}

function subscribe(id, ctxName, func) {
	bp.registerBThread(id + "ListenerBT", function() {
		while (true) {
			ctx = bp.sync({ waitFor:AnyNewContextEvent(ctxName), interrupt:UnsubscribeEvent(id) }).ctx;
			bp.registerBThread("an handler for a new context of type " + id, function() {
				func(ctx);
			});
		}
	});
	return id;
}


// Highest priority
bp.registerBThread("ContextReporterBT", function() {
	while (true) {
		// Trigger new context events
		for each (var event in CTX.getContextEvents()) {
			bp.sync({ request: event });
		}
		
		
		bp.sync({ waitFor:AnyUpdateContextDBEvent() });
	}
});


