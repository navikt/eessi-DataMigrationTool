package eu.ec.dgempl.eessi.rina.tool.migration.importer.helper;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECasePrefillGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.CasePrefill;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Service
public class CasePrefillsHelper {

    private final RinaJsonMapper rinaJsonMapper;

    public CasePrefillsHelper(RinaJsonMapper rinaJsonMapper) {
        this.rinaJsonMapper = rinaJsonMapper;
    }

    public void addCasePrefills(MapHolder a, String prefillKey, RinaCase b, ECasePrefillGroup eCasePrefillGroup) {
        MapHolder prefills = a.getMapHolder(prefillKey);
        getPrefills(prefills, eCasePrefillGroup, "")
                .forEach(b::addCasePrefill);
    }

    private List<CasePrefill> getPrefills(final MapHolder prefill, final ECasePrefillGroup prefillGroup, String path) {

        Function<String, List<CasePrefill>> prefillsMapper;
        if (prefillGroup == ECasePrefillGroup.SUBJECT) {
            prefillsMapper = getSubjectPrefillsMapper(prefill, prefillGroup, path);
        } else {
            prefillsMapper = getGenericPrefillsMapper(prefill, prefillGroup, path);
        }

        return prefill.fields()
                .stream()
                .filter(key -> prefill.get(key, true) != null)
                .map(prefillsMapper)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(List::stream)
                .collect(Collectors.toList());

    }

    @NotNull
    private Function<String, List<CasePrefill>> getSubjectPrefillsMapper(MapHolder prefill, ECasePrefillGroup prefillGroup,
            String path) {
        return key -> {
            Object value = prefill.get(key, true);

            if (value instanceof String) {
                CasePrefill casePrefill = new CasePrefill();
                casePrefill.setKey(addPath(path, key));
                casePrefill.setValue(String.valueOf(value));
                casePrefill.setPrefillGroup(prefillGroup);
                return Collections.singletonList(casePrefill);
            }

            if (value instanceof Map) {
                return getPrefills(
                        new MapHolder((Map<String, Object>) value, prefill.getVisitedFields(), prefill.addPath(key)),
                        prefillGroup,
                        addPath(path, key));
            }
            return Collections.emptyList();
        };
    }

    @NotNull
    private Function<String, List<CasePrefill>> getGenericPrefillsMapper(MapHolder prefill, ECasePrefillGroup prefillGroup,
            String path) {
        return key -> {
            Object value = prefill.get(key, true);

            CasePrefill casePrefill = new CasePrefill();
            casePrefill.setKey(addPath(path, key));
            casePrefill.setPrefillGroup(prefillGroup);

            if (value instanceof String) {
                casePrefill.setValue((String) value);
            } else {
                try {
                    casePrefill.setValue(rinaJsonMapper.objToJson(value));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(String.format("Could not map case prefill with key=[%s]", key), e);
                }
            }
            return Collections.singletonList(casePrefill);
        };
    }

    private String addPath(String path, String currentKey) {
        return StringUtils.isNotBlank(path) ? path + "." + currentKey : currentKey;
    }
}
