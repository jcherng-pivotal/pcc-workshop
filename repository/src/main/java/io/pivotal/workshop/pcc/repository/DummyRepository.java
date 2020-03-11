package io.pivotal.workshop.pcc.repository;

import io.pivotal.workshop.pcc.entity.Dummy;
import org.springframework.data.gemfire.repository.GemfireRepository;

public interface DummyRepository extends GemfireRepository<Dummy, String> {

}