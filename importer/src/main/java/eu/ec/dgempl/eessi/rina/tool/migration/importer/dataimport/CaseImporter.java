package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport;

public interface CaseImporter extends DataImporter{

    default boolean processesEmptyCase() {
        return false;
    }

}
