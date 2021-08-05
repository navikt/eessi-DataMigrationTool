package eu.ec.dgempl.eessi.rina.tool.migration.importer.helper;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.TestUtils.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.commons.transformation.RinaJsonMapper;
import eu.ec.dgempl.eessi.rina.model.enumtypes.ECasePrefillGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.tool.migration.common.model.fields.CaseFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

public class CasePrefillsHelperTest {

    private final RinaJsonMapper rinaJsonMapper = new RinaJsonMapper(new ObjectMapper());
    private final CasePrefillsHelper casePrefillsHelper = new CasePrefillsHelper(rinaJsonMapper);

    @Test
    public void shouldCreateAllSubjectPrefills() throws IOException {
        RinaCase rinaCase = createRinaCase("defaultRinaCase");
        MapHolder casestructuredmetadata = loadFromResource(
                this.getClass().getClassLoader(),
                "documents/cases/casestructuredmetadata.json");

        casePrefillsHelper.addCasePrefills(casestructuredmetadata, CaseFields.SUBJECT, rinaCase, ECasePrefillGroup.SUBJECT);

        long foundSubjectPrefills = rinaCase.getCasePrefills()
                .stream()
                .filter(casePrefill -> ECasePrefillGroup.SUBJECT == casePrefill.getPrefillGroup())
                .count();

        assertEquals(6, foundSubjectPrefills);
        assertTrue(isCasePrefillWithKeyFound(rinaCase, "address.city"));
        assertTrue(isCasePrefillWithKeyFound(rinaCase, "address.street"));
    }

    @Test
    public void shouldCreateAllPrefills() throws IOException {
        RinaCase rinaCase = createRinaCase("defaultRinaCase");
        MapHolder casemetadata = loadFromResource(
                this.getClass().getClassLoader(),
                "documents/cases/casemetadata.json");

        casePrefillsHelper.addCasePrefills(casemetadata, CaseFields.PRE_FILL, rinaCase, ECasePrefillGroup.PREFILL);

        long foundSubjectPrefills = rinaCase.getCasePrefills()
                .stream()
                .filter(casePrefill -> ECasePrefillGroup.PREFILL == casePrefill.getPrefillGroup())
                .count();

        assertEquals(30, foundSubjectPrefills);
        assertTrue(isCasePrefillWithKeyFound(rinaCase, "CaseContext"));
        assertTrue(isCasePrefillWithKeyFound(rinaCase, "InsuredPersonPersonIdentificationPersonalDetailsSex"));
    }

    private boolean isCasePrefillWithKeyFound(RinaCase rinaCase, String anotherString) {
        return rinaCase.getCasePrefills().stream().anyMatch(casePrefill -> casePrefill.getKey().equalsIgnoreCase(anotherString));
    }
}