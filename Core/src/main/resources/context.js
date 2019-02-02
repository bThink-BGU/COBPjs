importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);

var CTX = ContextService;
var CTX_instance = CTX.getInstance();

function subscribe(id, ctxName, func) { //TODO: Add  parameter "boolean applyToCurrentInstances" ?
    bp.registerBThread(id + "ListenerBT", function() {
        while (true) {
            // Wrapping body with a function to avoid "Referencing mutable variable from closure"
            (function () {
                var ctx = bp.sync({ waitFor:CTX.AnyNewContextEvent(ctxName), interrupt:CTX.UnsubscribeEvent(id) }).ctx;
                bp.registerBThread("handler '" + id + "' for a new context of type '" + ctxName + "'", function() {
                    func(ctx);
                });
            })();
        }
    });
    return id;
}

CTX.subscribe = subscribe;

// Highest priority
bp.registerBThread("ContextReporterBT", function() {
    while (true) {
        // Trigger new context events
        var events =CTX_instance.getContextEvents();
        for (var i = 0, len = events.length; i < len; i++) {
            bp.sync({ request: events[i] });
        }

        // Wait for next update
        bp.sync({ waitFor:CTX.AnyUpdateContextDBEvent() });
    }
});


