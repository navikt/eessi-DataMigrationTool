package eu.ec.dgempl.eessi.rina.tool.migration.importer.service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EApplicationRole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ERole;
import eu.ec.dgempl.eessi.rina.tool.migration.common.util.PreconditionsHelper;

@Service
public class AssignmentPolicyService {
    private final RinaJsonMapper jsonMapper;
    private final Map<String, Map<String, String>> mapping;

    @Autowired
    public AssignmentPolicyService(RinaJsonMapper jsonMapper, @Value("${policy.assignments.export.file}") String path) throws IOException {
        this.jsonMapper = jsonMapper;
        mapping = new HashMap<>();

        processAssignments(loadJsonFromDisk(path));
    }

    /**
     * Method for getting the actor name based on the {@code applicationRole}, {@code processDefinition} and {@code roleId}.
     * 
     * @param applicationRole
     *            the role (see {@link EApplicationRole})
     * @param processDefinition
     *            the process definition (e.g. P_BUC_01, R_BUC_05)
     * @param roleId
     *            the role identifier used for lookup
     * @return the role name associated with the {@code roleId}
     */
    public String getActorName(String applicationRole, String processDefinition, String roleId) {
        PreconditionsHelper.notEmpty(applicationRole, "applicationRole");
        PreconditionsHelper.notEmpty(processDefinition, "processDefinition");
        PreconditionsHelper.notEmpty(roleId, "roleId");

        // check the params
        EApplicationRole enumApplicationRole = EApplicationRole.lookup(applicationRole).orElseThrow();

        String key = enumApplicationRole.name() + "_" + processDefinition;

        Map<String, String> actorsMap = mapping.get(key);

        if (actorsMap == null) {
            return null;
        }

        return actorsMap.get(roleId);
    }

    private void processAssignments(Map<String, Object> obj) {
        Object definitions = obj.get("definitions");

        if (definitions == null || (!(definitions instanceof ArrayList))) {
            throw new IllegalStateException("Could not extract the definitions from the process definition assignment file.");
        }

        for (Object def : (ArrayList) definitions) {
            handleSector((Map<String, Object>) def);
        }
    }

    private void handleSector(Map<String, Object> obj) {
        Object processDefinitions = obj.get("processDefinitions");

        if (processDefinitions == null || (!(processDefinitions instanceof ArrayList))) {
            throw new IllegalStateException("Could not extract the process definitions from the process definition assignment file.");
        }

        for (Object def : (ArrayList) processDefinitions) {
            handleProcessDefinition((Map<String, Object>) def);
        }
    }

    private void handleProcessDefinition(Map<String, Object> obj) {
        Object roles = obj.get("appRoles");

        if (roles == null || (!(roles instanceof ArrayList))) {
            throw new IllegalStateException("Could not extract the application roles from the process definition assignment file.");
        }

        for (Object role : (ArrayList) roles) {
            Map<String, Object> roleAsMap = (Map<String, Object>) role;
            String id = (String) roleAsMap.get("id");

            Map<String, String> actorsMap = mapping.get(id);
            if (actorsMap == null) {
                actorsMap = new HashMap<>();
            }

            Object actors = roleAsMap.get("actors");

            if (actors == null || (!(actors instanceof ArrayList))) {
                throw new IllegalStateException("Could not extract the actors from the process definition assignment file.");
            }

            for (Object actor : (ArrayList) actors) {
                Map<String, String> actorAsMap = (Map<String, String>) actor;
                String actorId = actorAsMap.get("id");
                String actorName = actorAsMap.get("name");

                if (actorId == null || actorName == null) {
                    throw new IllegalStateException("Could not extract the actor information from the process definition assignment file.");
                }

                ERole enumActorName = ERole.lookup(actorName).orElseThrow();

                actorsMap.put(actorId, enumActorName.name());
            }

            mapping.put(id, actorsMap);
        }
    }

    private Map<String, Object> loadJsonFromDisk(String path) throws IOException, InvalidParameterException {
        File f = new File(path);

        if (f.exists() == false) {
            throw new InvalidParameterException(path);
        }

        String content = FileUtils.readFileToString(f, StandardCharsets.UTF_8.name());
        return jsonMapper.jsonToMap(content);
    }
}
