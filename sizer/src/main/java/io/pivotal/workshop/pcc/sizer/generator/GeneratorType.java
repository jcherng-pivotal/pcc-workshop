package io.pivotal.workshop.pcc.sizer.generator;

public enum GeneratorType {
    CUSTOMER_JAVA_FAKER(CustomerJavaFakerGenerator.class),
    CUSTOMER_EASY_RANDOM(CustomerEasyRandomGenerator.class),
    DUMMY_JAVA_FAKER(DummyJavaFakerGenerator.class),
    DUMMY_EASY_RANDOM(DummyEasyRandomGenerator.class),
    DUMMY_FIXED(DummyFixedGenerator.class);

    private final Class<?> beanClass;

    GeneratorType(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }
}
