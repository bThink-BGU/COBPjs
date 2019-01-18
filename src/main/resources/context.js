importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);

function subscribe(id, ctxName, func) { //TODO: Add  parameter "boolean applyToCurrentInstances" ?
	bp.registerBThread(id + "ListenerBT", function() {
		while (true) {
			ctx = bp.sync({ waitFor:CTX.AnyNewContextEvent(ctxName), interrupt:CTX.UnsubscribeEvent(id) }).ctx;
			bp.registerBThread("handler for a new context of type " + id, function() {
				func(ctx);
			});
		}
	});
	return id;
}

CTX.subscribe = subscribe;

// Highest priority
bp.registerBThread("ContextReporterBT", function() {
	while (true) {
		// Trigger new context events
		for (var event in CTX.getContextEvents()) {
			bp.sync({ request: event });
		}
		
		// Wait for next update
		bp.sync({ waitFor:CTX.AnyUpdateContextDBEvent() });
	}
});


