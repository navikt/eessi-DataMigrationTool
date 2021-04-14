package eu.ec.dgempl.eessi.rina.tool.migration.importer.helper;

import java.io.File;

import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;

import eu.ec.dgempl.eessi.rina.model.enumtypes.EGlobalParam;
import eu.ec.dgempl.eessi.rina.repo.GlobalParamRepo;

import clover.org.apache.commons.lang3.StringUtils;

@Component
public class AttachmentsHelper {

    private static final String DELIMITER = "_";

    private final GlobalParamRepo globalParamRepo;

    public AttachmentsHelper(final GlobalParamRepo globalParamRepo) {
        this.globalParamRepo = globalParamRepo;
    }

    public String getAttachmentDirectoryPath() {

        String path = globalParamRepo.findByKey(EGlobalParam.APP_PROFILE_ATTACHMENT_DIRECTORY_PATH).getValue();

        if (path == null || StringUtils.isEmpty(path)) {
            throw new RuntimeException("Attachment directory path not set!");
        }

        String pathString = path;
        if (!pathString.endsWith(File.separator)) {
            pathString = pathString + File.separator;
        }
        File attachmentDirectory = new File(pathString);
        if (!attachmentDirectory.exists() && !attachmentDirectory.mkdirs()) {
            throw new RuntimeException(String.format("Attachment directory [%s] cannot be created!", attachmentDirectory));
        }
        return pathString;
    }

    public String getAttachmentPathname(String caseId, String attachmentId) {
        // version 1 for backward compatibility
        return getAttachmentDirectoryPath() + generateId(caseId, attachmentId, "1");
    }

    public String generateId(String... values) {
        Preconditions.checkArgument(values != null);
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value);
            sb.append(DELIMITER);
        }
        return org.apache.commons.lang3.StringUtils.removeEnd(sb.toString(), DELIMITER);
    }
}
