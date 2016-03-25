package ru.ssp.synch.impl.persistence;

import org.springframework.data.repository.CrudRepository;
import ru.ssp.synch.model.SyncData;

import java.util.Collection;
import java.util.List;

/**
 * Created by PakAI on 23.03.2016.
 */
public interface SyncDataRepository extends CrudRepository<SyncData, String> {

    List<SyncData> findByExtJiraKeyIn(Collection<String> extJiraKeys);
}
