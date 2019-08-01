importPackage(Packages.il.ac.bgu.cs.bp.bpjs.context);

var CTX = ContextService;
var CTX_instance = ContextService.getInstance(); // DO NOT REMOVE: Here for verification

function subscribe(subscribeId, ctxName, func) { //TODO: Add  parameter "boolean applyToCurrentInstances" ?
    bp.registerBThread(subscribeId + "_ListenerBT", function() {
        while (true) {
            // Wrapping body with a function to avoid "Referencing mutable variable from closure"
            (function () {
                var e = bp.sync({ waitFor:CTX.AnyNewContextEvent(ctxName), interrupt:CTX.UnsubscribeEvent(subscribeId) });
                var ctxInstances = e.newContexts(ctxName);
                for (var index = 0; index < ctxInstances.length; index++) {
                    let element = ctxInstances[index];
                    bp.registerBThread("handler '" + subscribeId + "_/" + index + "' for a new context of type '" + ctxName + "', and value '"+ element.ctx +"'", function() {
                        func(element.ctx);
                    });
                }             
            })();
        }
    });
    return subscribeId;
}

function subscribeWithParameters(subscribeId, ctxName, ctxUniqueName, parameters, func) { //TODO: Add  parameter "boolean applyToCurrentInstances" ?
    CTX.registerParameterizedContextQuery(ctxName, ctxUniqueName, parameters);
    return subscribe(subscribeId, ctxUniqueName, func);
}

CTX.subscribe = subscribe;
CTX.subscribeWithParameters = subscribeWithParameters;

// Highest priority
bp.registerBThread("ContextReporterBT", function() {
    while (true) {
        // Wait for next update
        bp.sync({ waitFor:CTX.AnyContextCommandEvent() });

        // Trigger new context events
        var events = CTX.getContextEvents();
        if(events.length > 0)
            bp.sync({ request: events });
    }
});


