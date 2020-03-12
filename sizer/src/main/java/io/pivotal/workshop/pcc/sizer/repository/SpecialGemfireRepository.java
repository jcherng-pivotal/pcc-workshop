package io.pivotal.workshop.pcc.sizer.repository;

import org.apache.geode.cache.Region;
import org.springframework.aop.framework.Advised;
import org.springframework.data.gemfire.GemfireCallback;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.repository.GemfireRepository;
import org.springframework.data.gemfire.repository.support.SimpleGemfireRepository;
import org.springframework.data.repository.core.EntityInformation;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class SpecialGemfireRepository<T, ID> extends SimpleGemfireRepository<T, ID> {

    private final EntityInformation<T, ID> entityInformation;

    private final GemfireTemplate template;

    /**
     * Creates a new {@link SimpleGemfireRepository}.
     *
     * @param template          must not be {@literal null}.
     * @param entityInformation must not be {@literal null}.
     */
    public SpecialGemfireRepository(GemfireTemplate template, EntityInformation<T, ID> entityInformation) {
        super(template, entityInformation);

        this.template = template;
        this.entityInformation = entityInformation;
    }

    public static SpecialGemfireRepository getInstance(GemfireRepository proxyRepository) {
        try {
            SimpleGemfireRepository repository = (SimpleGemfireRepository) ((Advised) proxyRepository)
                    .getTargetSource().getTarget();

            Field entityInformationField = SimpleGemfireRepository.class.getDeclaredField("entityInformation");
            entityInformationField.setAccessible(true);

            Field templateField = SimpleGemfireRepository.class.getDeclaredField("template");
            templateField.setAccessible(true);
            EntityInformation entityInformation = (EntityInformation) entityInformationField.get(repository);
            GemfireTemplate template = (GemfireTemplate) templateField.get(repository);
            return new SpecialGemfireRepository(template, entityInformation);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends T> entities) {
        final Set idsToDelete = new HashSet<>();
        entities.forEach(entity -> idsToDelete.add(this.entityInformation.getRequiredId(entity)));
        this.template.execute((GemfireCallback<Void>) region -> {
            region.removeAll(idsToDelete);
            return null;
        });
    }

    <K> void doRegionClear(Region<K, ?> region) {
        region.removeAll(region.keySetOnServer());
    }

    @Override
    public void deleteAll() {
        this.template.execute((GemfireCallback<Void>) region -> {
            doRegionClear(region);
            return null;
        });
    }
}
