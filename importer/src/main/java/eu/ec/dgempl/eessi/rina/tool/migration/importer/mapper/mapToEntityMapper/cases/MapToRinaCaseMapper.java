package eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.cases;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.CaseFields.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.CasesUtils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.common.BucProcessDefinition;
import eu.ec.dgempl.eessi.rina.model.enumtypes.EApplicationRole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECasePrefillGroup;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECasePropertyKey;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECaseStatus;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Assignment;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.CaseParticipant;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Organisation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ProcessDefVersion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Tenant;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.EntityNotFoundEessiRuntimeException;
import eu.ec.dgempl.eessi.rina.model.jpa.exception.UniqueIdentifier;
import eu.ec.dgempl.eessi.rina.repo.DocumentTypeRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.repo.ProcessDefVersionRepo;
import eu.ec.dgempl.eessi.rina.repo.TenantRepo;
import eu.ec.dgempl.eessi.rina.service.tx.util.ProcessDefUtil;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.DmtEnumNotFoundException;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.AssignmentFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.CaseFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper._abstract.AbstractMapToEntityMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.OrganisationService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils;

import ma.glasnost.orika.MappingContext;

@Component
public class MapToRinaCaseMapper extends AbstractMapToEntityMapper<MapHolder, RinaCase> {

    private static final Logger logger = LoggerFactory.getLogger(MapToRinaCaseMapper.class);

    private final DocumentTypeRepo documentTypeRepo;
    private final IamUserRepo iamUserRepo;
    private final ProcessDefVersionRepo processDefVersionRepo;
    private final TenantRepo tenantRepo;

    @Autowired
    private OrganisationService organisationService;

    public MapToRinaCaseMapper(
            final DocumentTypeRepo documentTypeRepo, final IamUserRepo iamUserRepo,
            final ProcessDefVersionRepo processDefVersionRepo,
            final TenantRepo tenantRepo) {
        this.documentTypeRepo = documentTypeRepo;
        this.iamUserRepo = iamUserRepo;
        this.processDefVersionRepo = processDefVersionRepo;
        this.tenantRepo = tenantRepo;
    }

    @Override
    public void mapAtoB(final MapHolder a, final RinaCase b, final MappingContext context) {
        EElasticType eElasticType = (EElasticType) context.getProperty("type");

        if (EElasticType.CASES_CASESTRUCTUREDMETADATA == eElasticType) {
            mapCaseStructuredMetadata(a, b, context);
        } else {
            if (EElasticType.CASES_CASEMETADATA == eElasticType) {
                mapCaseMetadata(a, b, context);
            }
        }
    }

    private void mapCaseStructuredMetadata(final MapHolder a, final RinaCase b, final MappingContext context) {
        mapProcessDefVersion(a, b);
        mapTenant(a, b);

        b.setId(a.string(CaseFields.ID));
        b.setBusinessId(a.string(BUSINESS_ID));
        b.setInternationalId(a.string(INTERNATIONAL_ID));
        b.setSensitive(a.bool(SENSITIVE, Boolean.FALSE));
        b.setSensitiveCommited(a.bool(SENSITIVE_COMMITTED, Boolean.FALSE));
        b.setStoreLocationOfArchive(a.string(STORE_LOCATION_OF_ARCHIVE));

        mapCounter(a, b);
        mapStatus(a, b);
        mapSubject(a, b);
        mapSearchMetadata(a, b);
        mapParticipants(a, b);
        mapCaseAssignment(a, b, context);
        mapCaseProperties(a, b);
        mapAudit(a, b);
    }

    private void mapAudit(final MapHolder a, final RinaCase b) {
        setDefaultAudit(b::setAudit);

        String creatorId = a.string("creator.id", true);
        IamUser creator = RepositoryUtils.findById(creatorId, iamUserRepo::findById, IamUser.class);

        b.getAudit().setCreatedBy(creator.getId());
        b.getAudit().setUpdatedBy(creator.getId());

        mapDate(a, "startDate", b.getAudit()::setCreatedAt);
        mapDate(a, "lastUpdate", b.getAudit()::setUpdatedAt);
    }

    private void mapCounter(final MapHolder a, final RinaCase b) {
        Integer counter = a.integer(COUNTER);
        b.setCounter(counter != null ? counter : 0);
    }

    private void mapCaseMetadata(final MapHolder a, final RinaCase b, final MappingContext context) {
        b.setStarterSent(a.bool(IS_STARTER_SENT));
        b.setRemoveMeOnly(a.bool(REMOVE_ONLY_ME));
        b.setHasValidStarter(a.bool(HAS_VALID_STARTER));

        mapDocumentType(a, b);
        mapPrefill(a, b);
    }

    private void mapDocumentType(final MapHolder a, final RinaCase b) {
        String starterDocumentType = a.string(STARTER_TYPE);

        DocumentType documentType = documentTypeRepo.findByType(starterDocumentType);
        b.setStarterDocumentType(documentType);
    }

    private void mapPrefill(final MapHolder a, final RinaCase b) {
        addCasePrefills(a, PRE_FILL, b, ECasePrefillGroup.PREFILL);
    }

    private void mapSubject(final MapHolder a, final RinaCase b) {
        addCasePrefills(a, SUBJECT, b, ECasePrefillGroup.SUBJECT);
    }

    private void mapSearchMetadata(final MapHolder a, final RinaCase b) {
        addCasePrefills(a, SEARCH_METADATA, b, ECasePrefillGroup.SEARCH_METADATA);
    }

    private void mapParticipants(final MapHolder a, final RinaCase b) {
        b.getParticipants();

        List<MapHolder> participants = a.listToMapHolder(PARTICIPANTS);

        List<CaseParticipant> caseParticipants = participants.stream()
                .map(participant -> {
                    String organisationId = participant.string(ORGANISATION_ID, true);
                    Organisation organisation = organisationService.getOrSaveOrganisation(organisationId);

                    String applicationRole = participant.string(ROLE);
                    EApplicationRole eApplicationRole = EApplicationRole.lookup(applicationRole)
                            .orElseThrow(
                                    () -> new DmtEnumNotFoundException(EApplicationRole.class, participant.addPath(ROLE), applicationRole)
                            );

                    return new CaseParticipant(b, organisation, eApplicationRole);
                })
                .collect(Collectors.toList());

        b.setParticipants(caseParticipants);
    }

    private void mapCaseAssignment(final MapHolder a, final RinaCase b, final MappingContext context) {
        List<MapHolder> actors = a.listToMapHolder(CASE_ASSIGNMENT_ACTORS, true);

        if (CollectionUtils.isNotEmpty(actors)) {
            context.setProperty("caseId", a.string(ID));
            List<Assignment> assignments = actors.stream()
                    .filter(actor -> !"System".equalsIgnoreCase(actor.string(AssignmentFields.NAME)))
                    .map(actor -> mapperFacade.map(actor, Assignment.class, context))
                    .collect(Collectors.toList());
            assignments.forEach(b::addAssignment);
        }
    }

    private void mapCaseProperties(final MapHolder a, final RinaCase b) {
        MapHolder propertiesHolder = a.getMapHolder(CASE_ASSIGNMENT_PROPERTIES, true);

        b.setImportance(getCaseProperty(propertiesHolder, ECasePropertyKey.IMPORTANCE));
        b.setCriticality(getCaseProperty(propertiesHolder, ECasePropertyKey.CRITICALITY));
    }

    private void mapStatus(final MapHolder a, final RinaCase b) {
        String status = a.string(STATUS);
        ECaseStatus caseStatus = ECaseStatus.lookup(status).orElseThrow(
                () -> new DmtEnumNotFoundException(ECaseStatus.class, a.addPath(STATUS), status)
        );
        b.setStatus(caseStatus);
    }

    private void mapTenant(final MapHolder a, final RinaCase b) {
        String tenantId = a.string(WHOAMI_ID, true);
        if (StringUtils.isEmpty(tenantId)) {
            throw new RuntimeException("Could not import case without tenant id");
        }

        Tenant tenant = tenantRepo.findById(tenantId);
        if (tenant == null) {
            throw new EntityNotFoundEessiRuntimeException(Tenant.class, UniqueIdentifier.id, tenantId);
        }
        b.setTenant(tenant);
    }

    private void mapProcessDefVersion(final MapHolder a, final RinaCase b) {
        String processDefName = a.string(PROCESS_DEFINITION_NAME);

        if (StringUtils.isEmpty(processDefName)) {
            throw new RuntimeException("Could not import case without process definition name");
        }

        BucProcessDefinition bucProcessDefinition = ProcessDefUtil.retrieveProcessDefinitionInfo(processDefName);
        String processDefinitionName = bucProcessDefinition.getProcessDefinitionName();
        ProcessDefVersion processDefVersion = processDefVersionRepo.findByProcessDefIdAndBusinessVersion(
                processDefinitionName,
                bucProcessDefinition.getVersion());
        if (processDefVersion == null) {
            throw new EntityNotFoundEessiRuntimeException(ProcessDefVersion.class, UniqueIdentifier.processId, processDefinitionName);
        }
        b.setProcessDefVersion(processDefVersion);
        b.setApplicationRole(bucProcessDefinition.getApplicationRole());
    }

    private int getCaseProperty(final MapHolder a, final ECasePropertyKey key) {

        if (a == null || MapUtils.isEmpty(a.getHolding())) {
            logger.info("CaseAssignment property {} value not found. Setting default value 1.", key);
            return 1;
        }

        String propertyValue = a.string(key.getPropertyKey());

        if (StringUtils.isEmpty(propertyValue)) {
            logger.info("CaseAssignment property {} value not found. Setting default value 1.", key);
            return 1;
        }

        return Integer.parseInt(propertyValue);
    }
}
