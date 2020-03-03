bp.registerBThread("ContextReporterBT", function() {
    while (true) {
        var e = bp.sync({ waitFor: bp.EventSet("", function(e) { return true;})});
        CTX.updateContextForVerification(e);
        // Trigger new context events
        var events = CTX.getContextEvents();
        if(events.events.size() > 0)
            bp.sync({ request: events });
    }
});