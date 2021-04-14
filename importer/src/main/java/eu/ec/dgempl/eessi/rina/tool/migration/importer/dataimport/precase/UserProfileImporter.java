package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.UserProfile;
import eu.ec.dgempl.eessi.rina.repo.UserProfileRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PreCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.USER_PROFILE)
public class UserProfileImporter extends AbstractDataImporter implements PreCaseImporter {

    private final UserProfileRepo userProfileRepo;

    public UserProfileImporter(final UserProfileRepo userProfileRepo) {
        this.userProfileRepo = userProfileRepo;
    }

    @Override
    public DocumentsReport importData() {
        return run(this::processUserProfile);
    }

    private void processUserProfile(MapHolder doc) {
        UserProfile userProfile = beanMapper.map(doc, UserProfile.class);
        userProfileRepo.saveAndFlush(userProfile);
    }
}
