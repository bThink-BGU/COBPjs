if(typeof this['usingWholeDBQuery'] === 'undefined'  || this['usingWholeDBQuery'] === null) {
    this['usingWholeDBQuery'] = false;
}
function query(name, func) {

    bp.log.setLevel("Fine");
    if (usingWholeDBQuery) {
        // bp.log.fine("whole db query");
        ctx.registerWholeDbQuery(name,
            function (entities) {
                return entities.filter(e => func(e))
            })
    } else {
        // bp.log.fine("regular query");
        ctx.registerQuery(name, func)
    }
}