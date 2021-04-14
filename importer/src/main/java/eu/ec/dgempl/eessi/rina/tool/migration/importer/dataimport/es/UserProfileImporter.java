package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es;

import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.UserProfile;
import eu.ec.dgempl.eessi.rina.repo.UserProfileRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;

@Component
@ElasticTypeImporter(type = EElasticType.USER_PROFILE)
public class UserProfileImporter extends AbstractDataImporter {

    private final UserProfileRepo userProfileRepo;

    public UserProfileImporter(final UserProfileRepo userProfileRepo) {
        this.userProfileRepo = userProfileRepo;
    }

    @Override
    public void importData() {
        run(this::processUserProfile);
    }

    private void processUserProfile(MapHolder doc) {
        UserProfile userProfile = beanMapper.map(doc, UserProfile.class);
        userProfileRepo.saveAndFlush(userProfile);
    }
}
