package il.ac.bgu.cs.bp.bpjs.context;

import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.StorageConsolidationResult;
import il.ac.bgu.cs.bp.bpjs.model.StorageModificationStrategy;

import java.util.Set;

public class CtxStorageModificationStrategy implements StorageModificationStrategy {

    private final ContextProxy proxy;

    public CtxStorageModificationStrategy(ContextProxy proxy) {
        this.proxy = proxy;
    }

    @Override
    public StorageConsolidationResult.Success incomingModifications(StorageConsolidationResult.Success modifications, BProgramSyncSnapshot bpss, Set<BThreadSyncSnapshot> nextRoundBThreads) {
        this.proxy.resetEffect();
        return modifications;
    }

    @Override
    public StorageConsolidationResult resolve(StorageConsolidationResult.Conflict conflict, BProgramSyncSnapshot bpss, Set<BThreadSyncSnapshot> nextRoundBThreads) {
        return conflict;
    }
}
