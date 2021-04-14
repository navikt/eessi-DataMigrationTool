package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.precase;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamGroup;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamOrigin;
import eu.ec.dgempl.eessi.rina.repo.IamGroupRepo;
import eu.ec.dgempl.eessi.rina.repo.IamOriginRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.ElasticTypeImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.PreCaseImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport._abstract.AbstractDataImporter;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.report.DocumentsReport;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.EElasticType;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.GroupFields;

@Component
@ElasticTypeImporter(type = EElasticType.GROUP)
public class GroupImporter extends AbstractDataImporter implements PreCaseImporter {

    private final IamGroupRepo iamGroupRepo;
    private final IamOriginRepo iamOriginRepo;

    public GroupImporter(
            final IamGroupRepo iamGroupRepo,
            final IamOriginRepo iamOriginRepo) {
        this.iamGroupRepo = iamGroupRepo;
        this.iamOriginRepo = iamOriginRepo;
    }

    @Override
    public DocumentsReport importData() {
       return run(this::processGroupData);
    }

    private void processGroupData(MapHolder doc) {

        String origin = doc.string(GroupFields.ORIGIN);
        if (StringUtils.isNotBlank(origin)) {
            IamOrigin iamUserOrigin = iamOriginRepo.findByName(origin);
            if (iamUserOrigin == null) {
                iamUserOrigin = new IamOrigin(origin);
                iamOriginRepo.saveAndFlush(iamUserOrigin);
            }
        }

        IamGroup iamGroup = beanMapper.map(doc, IamGroup.class);
        iamGroupRepo.saveAndFlush(iamGroup);
    }
}
