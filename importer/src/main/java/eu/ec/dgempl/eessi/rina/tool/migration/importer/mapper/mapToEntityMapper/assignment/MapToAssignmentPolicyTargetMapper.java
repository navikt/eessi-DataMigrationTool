package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.assignment;

import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignmentPolicy;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignmentPolicyTarget;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Tenant;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.repo.AssignmentPolicyRepo;
import eu.ec.dgempl.eessi.rina.repo.TenantRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.AssignmentPolicyFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.BonitaProcessDefMappingsService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToAssignmentPolicyTargetMapper extends AbstractMapToEntityMapper<MapHolder, AssignmentPolicyTarget> {

    private static final Logger logger = LoggerFactory.getLogger(MapToAssignmentPolicyTargetMapper.class);
    public static final String TAGET_SEPARATOR = "-";
    public static final String PROCESS_SEPARATOR = "_";

    @Value("${ignore.invalid.references.policy:#{false}}")
    private Boolean ignoreInvalidReferencesPolicy;

    private final AssignmentPolicyRepo assignmentPolicyRepo;
    private final TenantRepo tenantRepo;

    @Autowired
    private BonitaProcessDefMappingsService bonitaProcessDefMappingsService;

    public MapToAssignmentPolicyTargetMapper(final AssignmentPolicyRepo assignmentPolicyRepo,
            final TenantRepo tenantRepo) {
        this.assignmentPolicyRepo = assignmentPolicyRepo;
        this.tenantRepo = tenantRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final AssignmentPolicyTarget b, final MappingContext context) {
        mapTenant(a, b);
        mapPolicies(a, b);
        mapTarget(a, b);

        b.setStorageId(String.join(PROCESS_SEPARATOR, b.getTenant().getId(), b.getTarget()));
    }

    private void mapTarget(final MapHolder a, final AssignmentPolicyTarget b) {
        String targetId = a.string(AssignmentPolicyFields.TARGET_ID);
        if (StringUtils.isNotBlank(targetId)) {

            String target;

            String[] parts = targetId.split(TAGET_SEPARATOR);
            if (parts.length == 2) {
                String[] processParts = parts[0].split(PROCESS_SEPARATOR);
                if (processParts.length != 4) {
                    throw new RuntimeException("Could not process targetId");
                }
                String actorName = bonitaProcessDefMappingsService.getActorName(
                        processParts[0],
                        String.join(PROCESS_SEPARATOR, processParts[1], processParts[2], processParts[3]),
                        parts[1]);

                target = parts[0] + TAGET_SEPARATOR + actorName;
            } else {
                target = targetId;
            }

            b.setTarget(target);
        }
    }

    private void mapPolicies(final MapHolder a, final AssignmentPolicyTarget b) {
        List<String> policies = a.list(AssignmentPolicyFields.TARGET_POLICIES, String.class, false);
        if (CollectionUtils.isNotEmpty(policies)) {
            policies.stream()
                .map(policyId -> {
                    try {
                        return RepositoryUtils.findById(policyId, assignmentPolicyRepo::findById, AssignmentPolicy.class);
                    } catch (EntityNotFoundEessiRuntimeException ex) {
                        if (ignoreInvalidReferencesPolicy) {
                            logger.info(String.format(
                                    "Policy with id %s from assignmentTarget was not found in policies repository but the policy was ignored because the property to ignore invalid policy references was set to true",
                                    policyId));
                        } else {
                            throw ex;
                        }
                        return null;
                    }})
                .filter(Objects::nonNull)
                .forEach(b::addAssignmentPolicy);
        }
    }

    private void mapTenant(final MapHolder a, final AssignmentPolicyTarget b) {
        String institutionId = a.string(AssignmentPolicyFields.TARGET_INSTITUTION_ID);
        Tenant tenant = RepositoryUtils.findById(institutionId, tenantRepo::findById, Tenant.class);
        b.setTenant(tenant);
    }
}
