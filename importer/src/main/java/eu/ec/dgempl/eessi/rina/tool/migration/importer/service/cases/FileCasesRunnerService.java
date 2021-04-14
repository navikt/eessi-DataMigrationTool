package eu.ec.dgempl.eessi.rina.tool.migration.importer.service.cases;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.cases._abstract.AbstractCasesRunnerService;

@Service
public class FileCasesRunnerService extends AbstractCasesRunnerService {

    private String casesInputFile;

    @Override
    public List<String> getCases() {
        List<String> cases = new ArrayList<>();
        try {
            cases = Files.readAllLines(new File(casesInputFile).toPath(), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return cases;
    }

    public void setCasesInputFile(final String casesInputFile) {
        this.casesInputFile = casesInputFile;
    }
}
