importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);

var CTX = ContextService;
var CTX_instance = CTX.getInstance();

function subscribe(subscribeId, ctxName, func) { //TODO: Add  parameter "boolean applyToCurrentInstances" ?
    bp.registerBThread(subscribeId + "ListenerBT", function() {
        while (true) {
            // Wrapping body with a function to avoid "Referencing mutable variable from closure"
            (function () {
                var ctx = bp.sync({ waitFor:CTX.AnyNewContextEvent(ctxName), interrupt:CTX.UnsubscribeEvent(subscribeId) }).ctx;
                bp.registerBThread("handler '" + subscribeId + "' for a new context of type '" + ctxName + "', and value '"+ ctx +"'", function() {
                    func(ctx);
                });
            })();
        }
    });
    return subscribeId;
}

function subscribeWithParameters(subscribeId, ctxName, ctxUniqueName, parameters, func) { //TODO: Add  parameter "boolean applyToCurrentInstances" ?
    CTX_instance.registerParameterizedContextQuery(ctxName, ctxUniqueName, parameters);
    return subscribe(subscribeId, ctxUniqueName, func);
}

CTX.subscribe = subscribe;
CTX.subscribeWithParameters = subscribeWithParameters;

// Highest priority
bp.registerBThread("ContextReporterBT", function() {
    while (true) {
        // Trigger new context events
        var events = CTX_instance.getContextEvents();
        for (var i = 0, len = events.length; i < len; i++) {
            bp.sync({ request: events[i] });
        }

        // Wait for next update
        bp.sync({ waitFor:CTX.AnyUpdateContextDBEvent() });
    }
});


