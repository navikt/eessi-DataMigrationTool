package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase.applicationProfile._abstract;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EGlobalParam;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.GlobalParam;
import eu.ec.dgempl.eessi.rina.model.jpa.entity._abstraction.Persistable;
import eu.ec.dgempl.eessi.rina.repo.GlobalParamRepo;
import eu.ec.dgempl.eessi.rina.repo._abstraction.RinaJpaRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PreCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

public abstract class AbstractApplicationProfileImporter extends AbstractDataImporter implements PreCaseImporter {

    private GlobalParamRepo globalParamRepo;

    protected void processGlobalParam(
            final Supplier<MapHolder> docSupplier,
            final Supplier<Map<String, EGlobalParam>> mapSupplier) {
        this.processGlobalParam(
                docSupplier,
                mapSupplier,
                (key, value) -> value
        );
    }

    protected void processGlobalParam(
            final Supplier<MapHolder> docSupplier,
            final Supplier<Map<String, EGlobalParam>> mapSupplier,
            final BiFunction<EGlobalParam, String, String> fieldSupplier) {

        MapHolder doc = docSupplier.get();
        Map<String, EGlobalParam> map = mapSupplier.get();
        List<GlobalParam> globalParams = map.entrySet()
                .stream()
                .map(entry -> {
                    Object value = doc.get(entry.getKey(), true);
                    String stringValue = value != null ? String.valueOf(value) : null;

                    GlobalParam globalParam = globalParamRepo.findByKey(entry.getValue());

                    if (globalParam != null) {
                        globalParam.setValue(fieldSupplier.apply(entry.getValue(), stringValue));
                    } else {
                        globalParam = new GlobalParam(entry.getValue(),fieldSupplier.apply(entry.getValue(), stringValue));
                    }

                    return globalParam;
                })
                .collect(Collectors.toList());
        globalParamRepo.saveAll(globalParams);
        globalParamRepo.flush();
    }

    protected <T extends Persistable> List<T> processListData(
            final Supplier<List<MapHolder>> mapSupplier,
            final Supplier<RinaJpaRepo<T, Long>> repoSupplier,
            final Supplier<Class<T>> classSupplier) {

        List<MapHolder> maps = mapSupplier.get();
        if (!CollectionUtils.isEmpty(maps)) {
            List<T> entities = maps
                    .stream()
                    .map(map -> beanMapper.map(map, classSupplier.get()))
                    .collect(Collectors.toList());

            RinaJpaRepo<T, Long> repo = repoSupplier.get();
            repo.saveAll(entities);
            repo.flush();

            return entities;
        }
        return Collections.emptyList();
    }

    @Autowired
    public void setGlobalParamRepo(GlobalParamRepo globalParamRepo) {
        this.globalParamRepo = globalParamRepo;
    }
}
