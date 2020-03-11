package io.pivotal.workshop.pcc.sizer.repository;

import io.pivotal.workshop.pcc.repository.CustomerRepository;
import io.pivotal.workshop.pcc.repository.DummyRepository;


public enum RepositoryType {
    CUSTOMER_REPOSITORY(CustomerRepository.class),
    DUMMY_REPOSITORY(DummyRepository.class);

    private final Class<?> beanClass;

    RepositoryType(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }
}
