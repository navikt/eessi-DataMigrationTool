package eu.ec.dgempl.eessi.rina.tool.migration.importer.dataimport.es.cases;

import static org.junit.Assert.*;
import static org.powermock.api.mockito.PowerMockito.*;

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
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.powermock.reflect.Whitebox;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.IamUser;
import eu.ec.dgempl.eessi.rina.model.jpa.entity.Notification;
import eu.ec.dgempl.eessi.rina.repo.DocumentTypeRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepo;
import eu.ec.dgempl.eessi.rina.repo.IamUserRepoExtended;
import eu.ec.dgempl.eessi.rina.repo.RinaCaseRepo;
import eu.ec.dgempl.eessi.rina.repo.RoleRepo;
import eu.ec.dgempl.eessi.rina.tool.migration.common.service.DefaultValuesService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.dto.MapHolder;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.esfield.NotificationFields;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.mapper.mapToEntityMapper.notification.MapToNotificationMapper;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.OrganisationService;
import eu.ec.dgempl.eessi.rina.tool.migration.importer.service.UserService;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class NotificationMapperTest {

    @Mock
    DocumentTypeRepo documentTypeRepo;
    @Mock
    RinaCaseRepo rinaCaseRepo;
    @Mock
    RoleRepo roleRepo;
    @Mock
    OrganisationService organisationService;
    @Mock
    IamUserRepo iamUserRepo;
    @Mock
    IamUserRepoExtended iamUserRepoExtended;
    @Mock
    DefaultValuesService defaultValuesService;

    MapToNotificationMapper mapToNotificationMapper;
    ObjectMapper objectMapper;

    @Before
    public void setUp() {
        UserService userService = new UserService(iamUserRepo, iamUserRepoExtended, defaultValuesService);
        mapToNotificationMapper = new MapToNotificationMapper(documentTypeRepo,
                rinaCaseRepo, roleRepo, organisationService, userService);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testNotificationWithDuplicatedResponsibleParties() throws IOException {

        String methodName = "distinctResponsibleParties";
        String duplicatedId = "AXg65hdCFYDE3SK9i9F1";

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
            result = Whitebox.invokeMethod(mapToNotificationMapper, methodName,
                    holder.listToMapHolder(NotificationFields.RESPONSIBLE_PARTIES));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check responsible parties with removed duplicates by ID
        Assert.assertEquals(23, result.size());
        List<MapHolder> duplicateAfter = result.stream().filter(it -> duplicatedId.equals(it.getHolding().get("id"))).collect(Collectors.toList());
        Assert.assertEquals(1, duplicateAfter.size());
    }

    @Test
    public void testNotificationWithResponsibleParties1() throws IOException {

        String methodName = "mapUsers";

        File notificationJsonFIle = new File(NotificationMapperTest.class.getClassLoader()
                .getResource("notification/notification_with_responsibleparties.json").getFile());
        Map<String, Object> holding = objectMapper.readValue(notificationJsonFIle, Map.class);
        MapHolder holder = new MapHolder(holding, new ConcurrentHashMap<>(), "");

        // Prepare data

        ArrayList<HashMap> responsibleParties = (ArrayList) holder.getHolding().get(NotificationFields.RESPONSIBLE_PARTIES);

        responsibleParties.forEach(x -> when(iamUserRepo.findById((String) x.get("id"))).thenReturn(new IamUser()));

        // Prepare data: 3 with Id and all found -> succeed

        try {
            Whitebox.invokeMethod(mapToNotificationMapper, methodName, holder, new Notification());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check results

        responsibleParties = (ArrayList) holder.getHolding().get(NotificationFields.RESPONSIBLE_PARTIES);
        Assert.assertEquals(3, responsibleParties.size());
    }

    @Test
    public void testNotificationWithResponsibleParties2() throws IOException {

        String methodName = "mapUsers";

        File notificationJsonFIle = new File(NotificationMapperTest.class.getClassLoader()
                .getResource("notification/notification_with_responsibleparties.json").getFile());
        Map<String, Object> holding = objectMapper.readValue(notificationJsonFIle, Map.class);
        MapHolder holder = new MapHolder(holding, new ConcurrentHashMap<>(), "");

        // Prepare data

        ArrayList<HashMap> responsibleParties = (ArrayList) holder.getHolding().get(NotificationFields.RESPONSIBLE_PARTIES);

        when(iamUserRepo.findById((String) responsibleParties.get(0).get("id"))).thenReturn(new IamUser());
        when(iamUserRepo.findById((String) responsibleParties.get(1).get("id"))).thenReturn(new IamUser());
        responsibleParties.get(2).remove("id");
        when(iamUserRepoExtended.findByUsernameAndIsDeletedIsTrue((String) responsibleParties.get(2).get("name")))
                .thenReturn(List.of(new IamUser()));

        // Prepare data: 2 with Id, 1 without Id but with Name and all found -> succeed

        try {
            Whitebox.invokeMethod(mapToNotificationMapper, methodName, holder, new Notification());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Check results

        responsibleParties = (ArrayList) holder.getHolding().get(NotificationFields.RESPONSIBLE_PARTIES);
        Assert.assertEquals(3, responsibleParties.size());
    }

    @Test(expected = RuntimeException.class)
    public void testNotificationWithResponsibleParties3() throws Exception {

        String methodName = "mapUsers";

        File notificationJsonFIle = new File(NotificationMapperTest.class.getClassLoader()
                .getResource("notification/notification_with_responsibleparties.json").getFile());
        Map<String, Object> holding = objectMapper.readValue(notificationJsonFIle, Map.class);
        MapHolder holder = new MapHolder(holding, new ConcurrentHashMap<>(), "");

        // Prepare data: 3 with Id but 1 Id not found -> throw exception

        ArrayList<HashMap> responsibleParties = (ArrayList) holder.getHolding().get(NotificationFields.RESPONSIBLE_PARTIES);

        when(iamUserRepo.findById((String) responsibleParties.get(0).get("id"))).thenReturn(new IamUser());
        when(iamUserRepo.findById((String) responsibleParties.get(1).get("id"))).thenReturn(new IamUser());

        // Call method

        Whitebox.invokeMethod(mapToNotificationMapper, methodName, holder, new Notification());
        fail("Expected RuntimeException was not thrown.");
    }

    @Test(expected = RuntimeException.class)
    public void testNotificationWithResponsibleParties4() throws Exception {

        String methodName = "mapUsers";

        File notificationJsonFIle = new File(NotificationMapperTest.class.getClassLoader()
                .getResource("notification/notification_with_responsibleparties.json").getFile());
        Map<String, Object> holding = objectMapper.readValue(notificationJsonFIle, Map.class);
        MapHolder holder = new MapHolder(holding, new ConcurrentHashMap<>(), "");

        // Prepare data: 2 with Id, 1 without Id and with Name but Name not found -> throw exception

        ArrayList<HashMap> responsibleParties = (ArrayList) holder.getHolding().get(NotificationFields.RESPONSIBLE_PARTIES);

        when(iamUserRepo.findById((String) responsibleParties.get(0).get("id"))).thenReturn(new IamUser());
        when(iamUserRepo.findById((String) responsibleParties.get(1).get("id"))).thenReturn(new IamUser());
        responsibleParties.get(2).remove("id");

        // Call method

        Whitebox.invokeMethod(mapToNotificationMapper, methodName, holder, new Notification());
        fail("Expected RuntimeException was not thrown.");
    }
}