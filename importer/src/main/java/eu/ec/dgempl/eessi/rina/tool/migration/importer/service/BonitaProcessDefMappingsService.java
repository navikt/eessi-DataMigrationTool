package eu.ec.dgempl.eessi.rina.tool.migration.importer.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EApplicationRole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ERole;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.DmtEnumNotFoundException;

@Service
public class BonitaProcessDefMappingsService {
    private final RinaJsonMapper jsonMapper;
    /**
     * Map<version, Map<processDefinitionName, Map<actorId, actorName>>>
     */
    private final Map<String, Map<String, Map<String, String>>> mapping;

    @Autowired
    public BonitaProcessDefMappingsService(RinaJsonMapper jsonMapper, @Value("${policy.assignments.export.file}") String path)
            throws IOException {
        this.jsonMapper = jsonMapper;
        mapping = new HashMap<>();

        processBonitaProcessDefActors(loadJsonFromDisk(path));
    }

    /**
     * Method for getting the actor name based on the {@code applicationRole}, {@code processDefinition} and {@code roleId}.
     *
     * @param applicationRole   the role (see {@link EApplicationRole})
     * @param processDefinition the process definition (e.g. P_BUC_01, R_BUC_05)
     * @param roleId            the role identifier used for lookup
     * @return the role name associated with the {@code roleId}
     */
    public String getActorName(String applicationRole, String processDefinition, String roleId) {
        PreconditionsHelper.notEmpty(applicationRole, "applicationRole");
        PreconditionsHelper.notEmpty(processDefinition, "processDefinition");
        PreconditionsHelper.notEmpty(roleId, "roleId");

        // check the params
        EApplicationRole enumApplicationRole = EApplicationRole.lookup(applicationRole).orElseThrow(
                () -> new DmtEnumNotFoundException(EApplicationRole.class, applicationRole)
        );

        String key = enumApplicationRole.name() + "_" + processDefinition;

        Optional<String> actorRoleOptional = mapping.values().stream()
                .map(processDefMap -> processDefMap.get(key))
                .filter(Objects::nonNull)
                .map(actorsMap -> actorsMap.get(roleId))
                .filter(Objects::nonNull)
                .findFirst();

        String actorRole = null;

        if (actorRoleOptional.isPresent()) {
            actorRole = actorRoleOptional.get();
        }

        return actorRole;
    }

    private void processBonitaProcessDefActors(List<Map> processDefinitions) {
        for (Object processDef : processDefinitions) {
            handleProcessDefinition((Map<String, Object>) processDef);
        }
    }

    private void handleProcessDefinition(Map<String, Object> obj) {
        String id = (String) obj.get("processDefinition");
        String version = (String) obj.get("version");

        Map<String, Map<String, String>> processDefinitions = mapping.computeIfAbsent(version, k -> new HashMap<>());
        Map<String, String> actorsMap = processDefinitions.computeIfAbsent(id, k -> new HashMap<>());

        Object actors = obj.get("actors");

        if (actors instanceof ArrayList) {
            for (Object actor : (ArrayList) actors) {
                Map<String, String> actorAsMap = (Map<String, String>) actor;
                String actorId = actorAsMap.get("id");
                String actorName = actorAsMap.get("name");

                if (actorId != null && actorName != null) {
                    ERole enumActorName = ERole.lookup(actorName).orElseThrow(() -> new DmtEnumNotFoundException(ERole.class, actorId));
                    actorsMap.put(actorId, enumActorName.name());
                }
            }
        }
    }

    private List<Map> loadJsonFromDisk(String path) throws IOException, InvalidParameterException {
        File f = new File(path);

        if (f.exists() == false) {
            throw new InvalidParameterException(path);
        }

        String content = FileUtils.readFileToString(f, StandardCharsets.UTF_8.name());
        return jsonMapper.jsonToListOfObjects(content, Map.class);
    }
}
