package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.RandomStringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.model.enumtypes.ECountryCode;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ERole;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ESector;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.AssignmentPolicy;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Document;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentType;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.DocumentTypeVersion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamOrigin;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Organisation;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ProcessDef;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.ProcessDefVersion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Role;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Sector;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Subdocument;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.SubdocumentBversion;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Tenant;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

public class TestUtils {

    private static final int RANDOM_STRING_SIZE = 255;
    private static final String NULL_UTF8_CHAR = "\u0000";
    private static final String SPACE = "";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static MapHolder loadFromResource(ClassLoader classLoader, String path) throws IOException {
        File auditEntryFile = new File(classLoader.getResource(path).getFile());

        Map<String, Object> map = OBJECT_MAPPER.readValue(auditEntryFile, new TypeReference<>() {
        });

        return new MapHolder(map, new ConcurrentHashMap<>(), "");
    }

    public static RinaCase createRinaCase(String caseId) {
        ProcessDefVersion processDefVersion = createRandomProcessDefVersion();
        Tenant tenant = createRandomTenant();

        return new RinaCase(tenant, processDefVersion, caseId);
    }

    public static Organisation createRandomOrg() {
        return new Organisation(randomString(), ECountryCode.BE);
    }

    public static Tenant createRandomTenant() {
        return new Tenant(createRandomOrg(), randomString());
    }

    public static Sector createRandomSector() {
        return new Sector(ESector.PENSION);
    }

    public static ProcessDef createRandomProcessDef() {
        return new ProcessDef(randomString(), createRandomSector(), randomString());
    }

    public static ProcessDefVersion createRandomProcessDefVersion() {
        return new ProcessDefVersion(createRandomProcessDef(), randomString());
    }

    public static IamUser createRandomIamUser() {
        return new IamUser(createRandomTenant(), createRandomIamOrigin(), randomString(), randomString());
    }

    public static IamGroup createRandomIamGroup() {
        return new IamGroup(createRandomTenant(), randomString(), randomString());
    }

    public static IamOrigin createRandomIamOrigin() {
        return new IamOrigin(randomString());
    }

    public static Role createRandomRole() {
        return new Role(ERole.SUPERVISOR);
    }

    public static AssignmentPolicy createRandomAssignmentPolicy() {
        return new AssignmentPolicy(createRandomTenant(), randomString(), randomString());
    }

    public static DocumentType createRandomDocumentType() {
        return new DocumentType(createRandomProcessDefVersion(), randomString());
    }

    public static DocumentTypeVersion createRandomDocumentTypeVersion() {
        return new DocumentTypeVersion(createRandomDocumentType(), randomString());
    }

    public static Document createRandomDocument() {
        return new Document(createRandomDocumentTypeVersion(), randomString());
    }

    public static Subdocument createRandomSubdocument() {
        return new Subdocument(createRandomDocument(), randomString());
    }

    public static SubdocumentBversion createRandomSubdocumentBversion() {
        return new SubdocumentBversion(createRandomSubdocument());

    }

    public static String randomString() {
        final String s = RandomStringUtils.random(RANDOM_STRING_SIZE);
        return s.replaceAll(NULL_UTF8_CHAR, SPACE);
    }
}
