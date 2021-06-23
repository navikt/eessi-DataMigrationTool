package eu.ec.dgempl.eessi.rina.tool.migration.importer.utils;

import static eu.ec.dgempl.eessi.rina.tool.migration.common.model.fields.CaseFields.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.CasesUtils.*;
import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.TestUtils.*;
import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import eu.ec.dgempl.eessi.rina.model.enumtypes.ECasePrefillGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.RinaCase;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

public class CasesUtilsTest {

    @Test
    public void shouldCreateAllPreFills() throws IOException {

        RinaCase rinaCase = createRinaCase("defaultRinaCase");
        MapHolder casestructuredmetadata = loadFromResource(
                this.getClass().getClassLoader(),
                "documents/cases/casestructuredmetadata.json");

        addCasePrefills(casestructuredmetadata, SUBJECT, rinaCase, ECasePrefillGroup.SUBJECT);

        long foundSubjectPrefills = rinaCase.getCasePrefills()
                .stream()
                .filter(casePrefill -> ECasePrefillGroup.SUBJECT == casePrefill.getPrefillGroup())
                .count();

        assertEquals(6, foundSubjectPrefills);
        assertTrue(isCasePrefillWithKeyFound(rinaCase, "address.city"));
        assertTrue(isCasePrefillWithKeyFound(rinaCase, "address.street"));
    }

    private boolean isCasePrefillWithKeyFound(RinaCase rinaCase, String anotherString) {
        return rinaCase.getCasePrefills().stream().anyMatch(casePrefill -> casePrefill.getKey().equalsIgnoreCase(anotherString));
    }
}