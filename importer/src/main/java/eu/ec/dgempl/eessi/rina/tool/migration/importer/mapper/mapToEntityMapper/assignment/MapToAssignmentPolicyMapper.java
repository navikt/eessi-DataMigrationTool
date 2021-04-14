package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.assignment;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EApplicationRole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EPolicyType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignmentPolicy;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignmentPolicyRule;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Tenant;
import eu.ec.dgempl.eessi.rina.repo.AssignmentPolicyRepo;
import eu.ec.dgempl.eessi.rina.repo.TenantRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.DmtEnumNotFoundException;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.AssignmentPolicyFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToAssignmentPolicyMapper extends AbstractMapToEntityMapper<MapHolder, AssignmentPolicy> {

    private final AssignmentPolicyRepo assignmentPolicyRepo;
    private final TenantRepo tenantRepo;

    public MapToAssignmentPolicyMapper(final AssignmentPolicyRepo assignmentPolicyRepo,
            final TenantRepo tenantRepo) {
        this.assignmentPolicyRepo = assignmentPolicyRepo;
        this.tenantRepo = tenantRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final AssignmentPolicy b, final MappingContext context) {
        mapAppRole(a, b);
        b.setColor(a.string(AssignmentPolicyFields.COLOR));
        b.setDescription(a.string(AssignmentPolicyFields.DESCRIPTION));
        b.setId(a.string(AssignmentPolicyFields.ID));
        b.setName(a.string(AssignmentPolicyFields.NAME));

        mapType(a, b);
        mapChildPolicies(a, b);
        mapTenant(a, b);
        mapRules(a, b);
        mapAudit(a, b);
    }

    private void mapAudit(final MapHolder a, final AssignmentPolicy b) {
        setDefaultAudit(b::setAudit);
    }

    private void mapRules(final MapHolder a, final AssignmentPolicy b) {
        List<MapHolder> rules = a.listToMapHolder(AssignmentPolicyFields.RULES);
        if (CollectionUtils.isNotEmpty(rules)) {
            rules.stream()
                    .map(rule -> mapperFacade.map(rule, AssignmentPolicyRule.class))
                    .forEach(b::addAssignmentPolicyRule);
        }
    }

    private void mapType(final MapHolder a, final AssignmentPolicy b) {
        String type = a.string(AssignmentPolicyFields.TYPE);
        EPolicyType ePolicyType = EPolicyType.valueOf(type);
        b.setType(ePolicyType);
    }

    private void mapChildPolicies(final MapHolder a, final AssignmentPolicy b) {
        List<String> policies = a.list(AssignmentPolicyFields.POLICIES, String.class, false);
        if (CollectionUtils.isNotEmpty(policies)) {
            policies.stream()
                    .map(id -> findById(id, assignmentPolicyRepo::findById, AssignmentPolicy.class))
                    .forEach(b::addChildAssignmentPolicy);
        }
    }

    private void mapTenant(final MapHolder a, final AssignmentPolicy b) {
        String institutionId = a.string(AssignmentPolicyFields.INSTITUTION_ID);
        Tenant tenant = findById(institutionId, tenantRepo::findById, Tenant.class);
        b.setTenant(tenant);
    }

    private void mapAppRole(final MapHolder a, final AssignmentPolicy b) {
        String appRole = a.string(AssignmentPolicyFields.APP_ROLE);
        if (StringUtils.isNotBlank(appRole)) {

            EApplicationRole eApplicationRole = EApplicationRole.lookup(appRole).orElseThrow(
                    () -> new DmtEnumNotFoundException(EApplicationRole.class, a.addPath(AssignmentPolicyFields.APP_ROLE), appRole)
            );
            b.setApplicationRole(eApplicationRole);
        }
    }
}
