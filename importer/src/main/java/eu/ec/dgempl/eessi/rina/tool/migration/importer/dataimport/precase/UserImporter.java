package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase;

import static eu.ec.dgempl.eessi.rina.tool.migration.importer.utils.RepositoryUtils.*;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamOrigin;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.repo.IamOriginRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserGroupRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PreCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.UserFields;

@Component
@ElasticTypeImporter(type = EElasticType.USER)
public class UserImporter extends AbstractDataImporter implements PreCaseImporter {

    private final IamOriginRepo iamOriginRepo;
    private final IamUserRepo iamUserRepo;
    private final IamUserGroupRepo iamUserGroupRepo;

    public UserImporter(
            final IamOriginRepo iamOriginRepo,
            final IamUserRepo iamUserRepo,
            final IamUserGroupRepo iamUserGroupRepo) {
        this.iamOriginRepo = iamOriginRepo;
        this.iamUserRepo = iamUserRepo;
        this.iamUserGroupRepo = iamUserGroupRepo;
    }

    @Override
    public DocumentsReport importData() {
        return run(this::processUserData);
    }

    private void processUserData(MapHolder doc) {
        String origin = doc.string(UserFields.IAM_ORIGIN);
        if (StringUtils.isNotBlank(origin)) {
            IamOrigin iamUserOrigin = iamOriginRepo.findByName(origin);
            if (iamUserOrigin == null) {
                iamUserOrigin = new IamOrigin(origin);
                iamOriginRepo.saveAndFlush(iamUserOrigin);
            }
        }

        IamUser user = beanMapper.map(doc, IamUser.class);
        iamUserRepo.saveAndFlush(user);

        saveInBulk(user::getIamUserGroups, () -> iamUserGroupRepo);
    }
}
