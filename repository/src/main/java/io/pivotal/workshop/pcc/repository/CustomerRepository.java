package io.pivotal.workshop.pcc.repository;

import io.pivotal.workshop.pcc.entity.Customer;
import org.springframework.data.gemfire.repository.GemfireRepository;

public interface CustomerRepository extends GemfireRepository<Customer, String> {

}