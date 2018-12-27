importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context.events);
importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);

function subscribe(id, ctxName, func) {
	bp.registerBThread(id + "ListenerBT", function() {
		while (true) {
			ctx = bp.sync({ waitFor:AnyNewContextEvent(ctxName), interrupt:UnsubscribeEvent(id) }).ctx;
			bp.registerBThread("handler for a new context of type " + id, function() {
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
		
		// Wait for next update
		bp.sync({ waitFor:AnyUpdateContextDBEvent() });
	}
});


