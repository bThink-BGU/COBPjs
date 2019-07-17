package il.ac.bgu.cs.bp.bpjs.context;

import javax.persistence.EntityManager;

public interface EntityManagerCreateHook {
  public void hook(EntityManager em);
}