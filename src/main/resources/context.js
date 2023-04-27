/* global bp, ctx_proxy, Packages, EventSets */ // <-- Turn off warnings
// importPackage(Packages.il.ac.bgu.cs.bp.bpjs.model.eventsets);

/**
 * An EventSet that represents all events with an effect function.
 * @type {bp.EventSet}
 */
const ContextChanged = EventSet('CTX.ContextChanged', function (e) {
    return ctx_proxy.effectFunctions.containsKey(String('CTX.Effect: ' + e.name))
})

const __base_sync__ = __sync__
__sync__ = function (stmt, syncData, isHot) {
    let ret = null
    let changes = null
    let query = null
    let id = null
    while (true) {
        bp.thread.data.waitFor.push(ContextChanged)
        ret = __base_sync__(stmt, syncData, isHot)
        if (ContextChanged.contains(ret)) {
            ctx_proxy.waitForEffect(bp.store, ret, ctx)
            bp.thread.data.waitFor.pop()
            changes = ctx_proxy.getChanges().toArray()
            query = bp.thread.data.query //TODO move before while (and remove null assignment at the end)
            id = bp.thread.data.seed //TODO move before while (and remove null assignment at the end)
            if (query) {
                for (let i = 0; i < changes.length; i++) { //TODO use parallelStream and filter like in cbt.bthread
                    if (changes[i].type.equals('end') && changes[i].query.equals(query) && changes[i].entityId.equals(id)) {
                        ctx_proxy.throwEndOfContext()
                        //TODO check if we can add break here
                    }
                }
            }
            if (ctx_proxy.shouldWake(__prepareStatement__(stmt), ret)) {
                return ret
            }
        } else {
            bp.thread.data.waitFor.pop()
            return ret
        }
        ret = null
        changes = null
        query = null
        id = null
    }
}

const ctx = {
    __internal_fields__: {
        createLiveCopy: function (name, query, entity, bt) {
            bthread(name,
                {query: query, seed: entity.id},
                function () {
                    try {
                        bt(entity)
                    } catch (ex) {
                        if (!ctx.isEndOfContextException(ex)) {
                            ctx.rethrowException(ex)
                        }
                    }
                })
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
    /**
     * @class Entity
     * @param {string} id - the entity's id
     * @param {string} type - the entity's type
     * @param {*} data
     * @returns {Entity}
     * @constructor
     * @throws {Error} if data contains "id" or "type" fields
     */
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
    /**
     * Inserts an entity to the context.
     * @param {Entity}entity
     * @returns {Entity} the inserted entity
     * @throws {Error} if the entity's id already exists
     */
    insertEntity: function (entity) {
        this.__internal_fields__.testIsEffect('insertEntity', true)

        return this.__internal_fields__.insertEntityUnsafe(entity)
    },
    /**
     * Removes an entity from the context.
     * @param {Entity|string}entity - the entity to remove or the id of the entity to remove.
     */
    removeEntity: function (entity) {
        this.__internal_fields__.testIsEffect('removeEntity', true)

        const key = String('CTX.Entity: ' + (entity.id ? entity.id : entity))
        if (!bp.store.has(key)) {
            throw new Error('Cannot remove entity, key ' + key + ' does not exist')
        }
        bp.store.remove(key)
    },
    /**
     * Retrieves an entity from the context given an id.
     * @param {string}id - the entity's id
     * @returns {Entity}
     * @throws {Error} if the entity does not exist
     */
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
     * @param {string|delegate} query query name or a query function that represents a context in the system
     * @returns {Entity[]} all entities that are in the given context
     */
    runQuery: function (query) {
        let func
        let isWholeDBQuery = false
        if (typeof (query) === 'string') {
            const key = String(query)
            if (!ctx_proxy.queries.containsKey(key)) throw new Error('Query ' + query + ' does not exist')
            let valueOfKey = ctx_proxy.queries.get(key)
            func = valueOfKey[0]
            isWholeDBQuery = valueOfKey[1]
        } else {
            func = query
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
     * @callback mapQueryFunction A function that represents a context in the system, returns a list of all entities that are in the context.
     * @param {java.util.Map<string,Entity>} entities - a Map of all entities in the system.
     * @returns {Entity[]} all the entities that are in the context
     */

    /**
     * @callback entityQueryFunction A function that represents a context in the system, returns whether an entity is in the context.
     * @param {Entity} entity - an entity
     * @returns {boolean} whether the entity is in the context or not
     */


    /**
     * Register a new query to the system.
     *
     * A query represents a context of the system and is defined as a function that filters
     * the context entities.
     * Specifically, the query is a function that gets a map all entities and returns all the entities is in the context
     * @param {string}name query name
     * @param {mapQueryFunction}[query] query function
     */
    registerWholeDbQuery: function (name, query) {
        this.registerQuery(name,query, true)
    },

    /**
     * Register a new query to the system.
     *
     * A query represents a context of the system and is defined as a function that filters
     * the context entities.
     * The function can support one of the following two types:
     * - A function that gets an entity and returns true if the entity is in the context
     * - A function that gets a map of all entities and returns all the entities is in the context
     *
     * By default, the query is of the first type. If the query is of the second type, the isWholeDBQuery parameter should be set to true.
     * @param {string}name query name
     * @param {mapQueryFunction|entityQueryFunction}[query] query function
     * @param {boolean}[isWholeDBQuery=false] a boolean that indicates whether the {@link query} param takes a map or not.
     * @throws {Error} if a query with the same name already exists or if the method is called from a b-thread.
     */
    registerQuery: function (name, query, isWholeDBQuery) {
        if(isWholeDBQuery===undefined || isWholeDBQuery=== null) isWholeDBQuery=false
        testInBThread('registerQuery', false)
        const key = String(name)
        if (ctx_proxy.queries.containsKey(key)) throw new Error('Query ' + name + ' already exists')
        ctx_proxy.queries.put(key, [query, isWholeDBQuery])
    },

    /**
     * @callback effectFunction A function that takes an event data and changes the context entities.
     * @param {*} data - the event's data
     */

    /**
     * Register a new effect to the system.
     * The effect is a function that may change the context entities whenever an event with the name {@link eventName} is selected.
     * @param {string}eventName - the name of the event that triggers the effect
     * @param {effectFunction}effect - the effect function to apply on the event's data.
     * @throws {Error} if an effect already exists for the given event name or if the method is called from a b-thread.
     */
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
    /**
     * Register a new CBT. A CBT is a b-thread template that binds a behavior to a specific query (i.e., context).
     * The CBT does not execute the behavior immediately, but rather creates a new live copy for each entity in the context.
     * New live copies contain the query name and the id of the entity that created the live copy.
     *
     * @param {string}name - the name of the CBT
     * @param {string}context - the query's name
     * @param {function(*):void}bt - the behavior to execute for each live copy
     */
    bthread: function (name, contexts, bt) {
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
                        .filter(function (change) { //update to get new for any query of mine
                            return change.type.equals('new') && change.query.equals(context)
                        }).toArray()
                    if(changes.length>0) {
                        let queriesEntities = contexts.map(c=>ctx.runQuery(c))
                        // bp.log.info("changesB {0}: {1}", context, changes)
                        for (let i = 0; i < changes.length; i++) {
                            // bp.log.info("changesC {0}: {1}", context, changes[i])
                            // bp.log.info(bp.store.keys())
                            if (changes[i].type.equals('new') && changes[i].query.equals(context)) {
                                ctx.__internal_fields__.createLiveCopy(String('Live copy' + ': ' + name + ' ' + changes[i].entityId), context, ctx.getEntityById(changes[i].entityId), bt)
                            }
                        }
                    }
                    changes = null
                }
            })
    },
    /**
     * Check if the given error is an end of context exception.
     * @param e
     * @returns {boolean}
     */
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
    /**
     * Adds new entities to the context before the start of the program.
     * @param {Entity[]}entities
     * @throws {Error} if the method is called from a b-thread.
     */
    populateContext: function (entities) {
        testInBThread('populateContext', false)
        for (let i = 0; i < entities.length; i++)
            this.__internal_fields__.insertEntityUnsafe(entities[i])
    }
}
Object.freeze(ctx)
