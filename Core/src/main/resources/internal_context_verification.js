bp.registerBThread("ExecuteCtxEvents", function() {
    var e;
    while (true) {
        e = bp.sync({ waitFor:CTX.AnyUpdateContextDBEvent() });
        e.execute();
    }
});