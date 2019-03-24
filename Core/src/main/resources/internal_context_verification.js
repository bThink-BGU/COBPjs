bp.registerBThread("ExecuteCtxEvents", function() {
    while (true) {
        var e = bp.sync({ waitFor:CTX.AnyUpdateContextDBEvent() });
        e.execute();
    }
});