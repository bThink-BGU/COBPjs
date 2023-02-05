/* global bp, ctx_proxy, Packages, EventSets */ // <-- Turn off warnings
// importPackage(Packages.il.ac.bgu.cs.bp.bpjs.model.eventsets);

const ContextChanged = EventSet('CTX.ContextChanged', function (e) {
    return ctx_proxy.effectFunctions.containsKey(String('CTX.Effect: ' + e.name))
})

const __base_sync__ = __sync__
__sync__ = function (stmt, syncData, isHot) {
    let ret = null
    let changes = null
    let query = null
    let id = null
    if(bp.thread.data.seed!==undefined && bp.thread.data.seed.constructor==Object)//if seed is a dictionary, it means that the b-thread is a multi context b-thread
    {

        while (true) {
            bp.thread.data.waitFor.push(ContextChanged)
            ret = __base_sync__(stmt, syncData, isHot)
            // stmt.waitFor.unshift(ContextChanged)
            // ret = syncData ? bp.hot(isHot).sync(stmt, syncData) : bp.hot(isHot).sync(stmt)//todo check if needs to be changed to be same as in else(it changed)
            // stmt.waitFor.shift()
            if (ContextChanged.contains(ret)) {
                ctx_proxy.waitForEffect(bp.store, ret, ctx)
                changes = ctx_proxy.getChanges().toArray()
                query = bp.thread.data.query
                id = bp.thread.data.seed
                //multi query case
                let seed= bp.thread.data.seed
                let queries=Object.keys(seed)
                for (let i = 0; i < changes.length; i++) {
                    if (changes[i].type.equals('end') && queries.includes(changes[i].query) && changes[i].entityId.equals(seed[changes[i].query])) {
                        ctx_proxy.throwEndOfContext()
                    }
                }
                if (ctx_proxy.shouldWake(stmt, ret)) {
                    return ret
                }
            } else {
                bp.thread.data.waitFor.pop()//todo, this row is also new. check that it is okay.

                return ret
            }
            ret = null
            changes = null
            query = null
            id = null
        }
    }
    else {
        while (true) {
            bp.thread.data.waitFor.push(ContextChanged)
            //todo, things in comment were changed to be the uncommented line in the next row.
            //ret = syncData ? bp.hot(isHot).sync(stmt, syncData) : bp.hot(isHot).sync(stmt)
            //       stmt.waitFor.shift()
            ret = __base_sync__(stmt, syncData, isHot)
            if (ContextChanged.contains(ret)) {
                ctx_proxy.waitForEffect(bp.store, ret, ctx)
                bp.thread.data.waitFor.pop()
                changes = ctx_proxy.getChanges().toArray()
                query = bp.thread.data.query
                id = bp.thread.data.seed
                if (query) {
                    // change's structure - {type: 'end/new', query: 'query_name', entityId: 'id'}
                    for (let i = 0; i < changes.length; i++) {
                        if (changes[i].type.equals('end') && changes[i].query.equals(query) && changes[i].entityId.equals(id)) {
                            ctx_proxy.throwEndOfContext()
                        }
                    }
                }
                if (ctx_proxy.shouldWake(stmt, ret)) {
                    return ret
                }
            } else {
                bp.thread.data.waitFor.pop()//todo, this row is also new. check that it is okay.
                return ret
            }
            ret = null
            changes = null
            query = null
            id = null
        }
    }
}

const ctx = {
    __internal_fields__: {
        createLiveCopy: function (name, queries, entities, bt) {
            if(Array.isArray(queries)) {
                // bp.log.setLevel("Fine")
                // bp.log.fine('Multiple queries')
                if(queries.length !==entities.length){
                    throw new Error("queries and entities arrays must have the same length")//TODO: check if this is the right way to throw an error
                }
                let seed={}
                for (let i = 0; i < queries.length; i++) {
                    seed[queries[i]] = entities[i].id
                }
                bthread(name,
                    { seed: seed},
                    function () {
                        try {
                            //? ...entities is not working(not supported by the current version of node?)
                            bt.apply(this, entities)
                            //TODO in multiple queries, we will have instead: bt(entity[0], entity[1],...)
                        } catch (ex) {
                            if (!ctx.isEndOfContextException(ex)) {
                                ctx.rethrowException(ex)
                            }
                        }
                    })
            }
            //regular case
            else{
                bthread(name,
                    { query: queries, seed: entities.id },
                    function () {
                        try {
                            bt(entities)
                        } catch (ex) {
                            if (!ctx.isEndOfContextException(ex)) {
                                ctx.rethrowException(ex)
                            }
                        }
                    })
            }
        },
        assign: function (target, source) {
            for (let property in source) {
                let val = source[property]
                if (source[property] instanceof org.mozilla.javascript.ConsString) {
                    val = String(val)
                } else if (Array.isArray(source[property])) {
                    bp.log.warn('Property \'\'{0}\'\' is an array. If the order of the array\'\'s elements is not important, you should use java.util.HashSet. Entity is {1}.', property, source)
                }
                target[property] = val
            }
            return target
        },
        insertEntityUnsafe: function (entity) {
            const key = String('CTX.Entity: ' + entity.id)
            if (bp.store.has(key)) {
                throw new Error('Key ' + entity.id + ' already exists')
            }
            bp.store.put(key, entity)
            return entity
        },
        testIsEffect(caller, expected) {
            if (expected && isInBThread())
                throw new Error(String('The function ' + caller + ' must be called by an effect function'))
            if (!expected && !isInBThread())
                throw new Error(String('The function ' + caller + ' must no be called by an effect function'))
        }
    },
    Entity: function (id, type, data) {
        let entity = {id: String(id), type: String(type)}
        if (typeof data !== 'undefined' && data != null) {
            if (typeof data.id !== 'undefined' || typeof data.type !== 'undefined') {
                throw new Error(String('Entity\'s data must not include "id" or "type" fields.'))
            }
            this.__internal_fields__.assign(entity, data)
        }
        return entity
    },
    insertEntity: function (entity) {
        this.__internal_fields__.testIsEffect('insertEntity', true)

        return this.__internal_fields__.insertEntityUnsafe(entity)
    },
    removeEntity: function (entity_or_id) {
        this.__internal_fields__.testIsEffect('removeEntity', true)

        const key = String('CTX.Entity: ' + (entity_or_id.id ? entity_or_id.id : entity_or_id))
        if (!bp.store.has(key)) {
            throw new Error('Cannot remove entity, key ' + key + ' does not exist')
        }
        bp.store.remove(key)
    },
    getEntityById: function (id) {
        // bp.log.info('getEntityById id {0}', id)
        const key = String('CTX.Entity: ' + id)
        if (!bp.store.has(key)) {
            throw new Error('Key ' + key + ' does not exist')
        }
        return bp.store.get(key)
        //throw new Error("Entity with id '" + id + "' does not exist")
    },
    /**
     * Returns an array of all entities that are in the given context
     * @param {string|delegate} queryName_or_function query name or a query function that represents a context in the system
     * @returns {Entity[]} all entities that are in the given context
     */
    runQuery: function (queryName_or_function) {
        let func
        let isWholeDBQuery = false
        if (typeof (queryName_or_function) === 'string') {
            const key = String(queryName_or_function)
            if (!ctx_proxy.queries.containsKey(key)) throw new Error('Query ' + queryName_or_function + ' does not exist')
            let valueOfKey = ctx_proxy.queries.get(key)
            func = valueOfKey[0]
            isWholeDBQuery = valueOfKey[1]
        }
        else if (Array.isArray(queryName_or_function)) //as first step, we will treat this case completely separately
            //TODO: in the future, make this more modular
        {

            //there is more than one query
            let functions=[]
            for (let i = 0; i < queryName_or_function.length; i++) {
                let key2 = String(queryName_or_function[i])
                if (!ctx_proxy.queries.containsKey(key2)) throw new Error('Query ' + queryName_or_function[i] + ' does not exist')
                func = ctx_proxy.queries.get(key2)
                functions.push(func)
            }
            let entitiesOfQueries=[]//this is an array of arrays. each inner array is the result of a query
            let entities= bp.store.entrySet().stream().filter(e=>e.getKey().startsWith('CTX.Entity: ')).map(e=>e.getValue()).toArray()//all the entities in the store(our DB)
            let entitiesOfQuery
            for (let i = 0; i < functions.length; i++) {
                let valueOfKey = functions[i]
                func = valueOfKey[0]
                isWholeDBQuery = valueOfKey[1]
                if (isWholeDBQuery) {
                    entitiesOfQuery = func(entities)
                    entitiesOfQueries.push(entitiesOfQuery)
                }
                else
                {
                    let ans=[]
                    entities.map(e=>func(e) ? ans.push(e): null)
                    entitiesOfQueries.push(ans)

                }
            }

            const cartesian= a=> { // a = array of array
                var i, j, l, m, a1, o = [];
                if (!a || a.length == 0) return a;

                a1 = a.splice(0, 1)[0]; // the first array of a
                a = cartesian(a);
                for (i = 0, l = a1.length; i < l; i++) {
                    if (a && a.length)
                        for (j = 0, m = a.length; j < m; j++)
                            o.push([a1[i]].concat(a[j]));
                    else
                        o.push([a1[i]]);
                }
                return o;
            }
            let cartesianProduct=cartesian(entitiesOfQueries)//this is an array of arrays. each inner array is a combination of entities, one for each query.
            //each inner array is a combination of entities that satisfy all the queries(one entity for each query)
            //print cartesianProduct
            //TODO delete this log
            for (let i = 0; i < cartesianProduct.length; i++) {
                bp.log.info('cartesianProduct[{0}]={1}',i,cartesianProduct[i])

            }
            return cartesianProduct
        }
        else {
            func = queryName_or_function
        }
        if (isWholeDBQuery) {
            let entities = bp.store.entrySet().stream().filter(e => e.getKey().startsWith('CTX.Entity: ')).map(e => e.getValue()).toArray()//all the entities in the store(our DB)
            let ansFunc = func(entities)
            return ansFunc
        }
        else {
            //can use entities declared above instead of what is in the if
            let ans = []
            let storeEntries = bp.store.entrySet().toArray()
            for (let i = 0; i < storeEntries.length; i++) {
                let entry = storeEntries[i]
                if (entry.getKey().startsWith(String('CTX.Entity:')) && func(entry.getValue()))
                    ans.push(entry.getValue())
            }
            return ans
        }
    },
    /**
     * Adds a new query to the system, The query is a function that gets all entities and returns all the entities is in the context
     * @param name query name
     * @param query query function
     */
    registerWholeDbQuery: function (name, query) {
        this.registerQuery(name,query, true)
    },
    /**
     * Adds a new query to the system, supporting both types of queries.
     * - A query that is a function that gets all entities and returns all the entities is in the context
     * - A query that is a function that gets an entity and returns true if the entity is in the context
     * @param name query name
     * @param query query function
     * @param isWholeDBQuery a boolean that indicates if the query is a whole DB query or not.
     * If it is a whole DB query, the query function gets all the entities in the DB and returns all the entities that are in the context.
     * If it is not a whole DB query, the query function gets an entity and returns true if the entity is in the context.
     */
    registerQuery: function (name, query, isWholeDBQuery) {
        if(isWholeDBQuery===undefined) isWholeDBQuery=false
        testInBThread('registerQuery', false)
        const key = String(name)
        if (ctx_proxy.queries.containsKey(key)) throw new Error('Query ' + name + ' already exists')
        ctx_proxy.queries.put(key, [query, isWholeDBQuery])
    },
    registerEffect: function (eventName, effect) {
        if (!(typeof eventName === 'string' || eventName instanceof String)) {
            throw new Error('The first parameter of registerEffect must be an event name')
        }
        testInBThread('registerEffect', false)

        const key = String('CTX.Effect: ' + eventName)
        if (ctx_proxy.effectFunctions.containsKey(key))
            throw new Error('Effect already exists for event ' + eventName)
        ctx_proxy.effectFunctions.put(key, effect)
    },
    bthread: function (name, context, bt) {
        if(Array.isArray( context))//if context is an array, it means that it is a combination of queries
        {
            // bp.log.setLevel("Fine")
            // bp.log.fine('Multiple queries')
            bthread('cbt: ' + name,
                function () {
                    bp.log.setLevel("Fine")
                    bp.log.fine('bthread ' + name + ' with context ' + context+' was created')
                    // spawn live copies for current answers
                    //this part works
                    let res = ctx.runQuery(context)
                    //this is a special case where we have multiple queries
                    //each element in res is an array of entities, one for each query(context given is an array of queries)
                    for (let i = 0; i < res.length; i++) {
                        //can be done using only reduce(TODO)
                        let resIds = res[i].map(e => e.id).reduce((a, b) => a + ',' + b, '').substring(1)//this is a string of all the ids of the entities in the current context
                        bp.log.fine('bthread: ' + name + ' resIds.length: ' + res[i].length)
                        //res[i] is an array of entities
                        ctx.__internal_fields__.createLiveCopy(String('Live copy' + ': ' + name + ' with entities: [' + resIds+']'), context, res[i], bt)
                    }
                    res = undefined
                    let changes = null
                    while (true) {
                        sync({waitFor: ContextChanged})
                        // bp.log.info("changesA {0}: {1}", context, ctx_proxy.getChanges())
                        //Filter all changes that are new and are relevant to the context
                        bp.log.setLevel("Fine")
                        // bp.log.fine(ctx_proxy.getChanges().parallelStream().map(c => c.query))
                        changes = ctx_proxy.getChanges().parallelStream()
                            .filter(function (change) {
                                // bp.log.fine('change: ' + change)
                                // bp.log.fine('is context an array: ' + Array.isArray(context))
                                // bp.log.fine('IS change.query in context: ' + context.includes(change.query))
                                return change.type.equals('new') && context.includes(change.query)
                            }).toArray()

                        for (let i = 0; i < changes.length; i++) {
                            // bp.log.info("changesC {0}: {1}", context, changes[i])
                            // bp.log.info(bp.store.keys())
                            if (changes[i].type.equals('new') && context.includes(changes[i].query)) {
                                //one of the queries in the context has changed to on. if all other queries are also on, we need to spawn a new live copy
                                let allQueriesAreOn = true

                                let index = context.indexOf(changes[i].query)
                                //deep copy of the array
                                // var tempContext = context.map(function(arr) {
                                //   return arr.slice();
                                // });
                                let tempContext = context.slice(0)
                                tempContext.splice(index, 1)
                                // bp.log.fine('tempContext: ' + tempContext)
                                res = ctx.runQuery(tempContext)//remove the query that changed to on from the context and run the rest of the queries
                                // bp.log.fine('res: ' + res[0][0].id)
                                //res is an array of possible combinations of entities that satisfy the context. without the query that changed to on(and its entity)
                                for (let j = 0; j < res.length; j++) {
                                    // res[j].push(ctx.getEntityById( changes[i].entityId))//add the entity that changed on the context
                                    res[j].splice(index, 0, ctx.getEntityById(changes[i].entityId))//add the id of the entity that changed on the context
                                    //TODO delete these logs
                                    // bp.log.fine('changes[i]: ' + changes[i])
                                    // bp.log.fine('changes[i].entity: ' + changes[i].entityId)
                                    // bp.log.fine('changes[i].query: ' + changes[i].query)
                                    // bp.log.fine('changes[i].type: ' + changes[i].type)
                                    // bp.log.fine('res[{0}]: {1}',j,res[j])
                                    let resIds = res[j].map(e => e.id).reduce((a, b) => a + ',' + b, '').substring(1)//this is a string of all the ids of the entities in the current context
                                    ctx.__internal_fields__.createLiveCopy(String('Live copy' + ': ' + name + ' with entities: ' + resIds), context, res[j], bt)
                                }
                            }
                        }
                        changes = null
                    }
                })
        }
        else {
            bthread('cbt: ' + name,
                function () {
                    let res = ctx.runQuery(context)
                    for (let i = 0; i < res.length; i++) {
                        ctx.__internal_fields__.createLiveCopy(String('Live copy' + ': ' + name + ' ' + res[i].id), context, res[i], bt)
                    }
                    res = undefined
                    let changes = null
                    while (true) {
                        sync({waitFor: ContextChanged})
                        // bp.log.info("changesA {0}: {1}", context, ctx_proxy.getChanges())
                        changes = ctx_proxy.getChanges().parallelStream()
                            .filter(function (change) {
                                return change.type.equals('new') && change.query.equals(context)
                            }).toArray()
                        // bp.log.info("changesB {0}: {1}", context, changes)
                        for (let i = 0; i < changes.length; i++) {
                            // bp.log.info("changesC {0}: {1}", context, changes[i])
                            // bp.log.info(bp.store.keys())
                            if (changes[i].type.equals('new') && changes[i].query.equals(context)) {
                                ctx.__internal_fields__.createLiveCopy(String('Live copy' + ': ' + name + ' ' + changes[i].entityId), context, ctx.getEntityById(changes[i].entityId), bt)
                            }
                        }
                        changes = null
                    }
                })
        }
    },
    isEndOfContextException: function (e) {
        return typeof e.javaException !== 'undefined' && ctx_proxy.isEndOfContextException(e.javaException)
    },
    rethrowException: function (e) {
        if (e.javaException) {
            ctx_proxy.rethrowException(e.javaException)
        } else if (e.rhinoException) {
            ctx_proxy.rethrowException(e.rhinoException)
        } else {
            throw e
        }
    },
    populateContext: function (entities) {
        testInBThread('populateContext', false)
        for (let i = 0; i < entities.length; i++)
            this.__internal_fields__.insertEntityUnsafe(entities[i])
    }
}
Object.freeze(ctx)
