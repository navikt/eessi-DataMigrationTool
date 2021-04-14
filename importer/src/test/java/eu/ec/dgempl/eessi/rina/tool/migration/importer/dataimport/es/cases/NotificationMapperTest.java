package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.cases;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.reflect.Whitebox;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.repo.DocumentTypeRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepoExtended;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.repo.RoleRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.NotificationFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.notification.MapToNotificationMapper;

@ExtendWith(MockitoExtension.class)
public class NotificationMapperTest {

    @Mock
    IamUserRepo iamUserRepo;
    @InjectMocks
    IamUserRepoExtended iamUserRepoExtended;
    @Mock
    DocumentTypeRepo documentTypeRepo;
    @Mock
    RinaCaseRepo rinaCaseRepo;
    @Mock
    RoleRepo roleRepo;

    @Test
    public void testNotificationWithDuplicatedResponsibleParties() throws IOException {

        String duplicatedId = "AXg65hdCFYDE3SK9i9F1";

        MapToNotificationMapper mapToNotificationMapper = new MapToNotificationMapper(iamUserRepo, iamUserRepoExtended, documentTypeRepo,
                rinaCaseRepo, roleRepo);
        ObjectMapper objectMapper = new ObjectMapper();

        File notificationJsonFIle = new File(
                NotificationMapperTest.class.getClassLoader().getResource("notification/notification_duplicated_responsibleParties.json").getFile());
        Map<String, Object> holding = objectMapper.readValue(notificationJsonFIle, Map.class);

        MapHolder holder = new MapHolder(holding, new ConcurrentHashMap<>(), "");

        // Check original responsible parties with duplicates
        ArrayList<HashMap> responsibleParties = (ArrayList) holder.getHolding().get(NotificationFields.RESPONSIBLE_PARTIES);
        Assert.assertEquals(24, responsibleParties.size());
        List<HashMap> duplicate = responsibleParties.stream().filter(it -> duplicatedId.equals(it.get("id"))).collect(Collectors.toList());
        Assert.assertEquals(2, duplicate.size());

        List<MapHolder> result = Collections.emptyList();
        try {
            result = Whitebox.invokeMethod(mapToNotificationMapper, "distinctResponsibleParties",
                    holder.listToMapHolder(NotificationFields.RESPONSIBLE_PARTIES));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check responsible parties with removed duplicates by ID
        Assert.assertEquals(23, result.size());
        List<MapHolder> duplicateAfter = result.stream().filter(it -> duplicatedId.equals(it.getHolding().get("id"))).collect(Collectors.toList());
        Assert.assertEquals(1, duplicateAfter.size());
    }
}