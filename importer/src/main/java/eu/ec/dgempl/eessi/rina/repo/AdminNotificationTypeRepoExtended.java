package eu.ec.dgempl.eessi.rina.repo;

import org.springframework.stereotype.Repository;

import eu.ec.dgempl.eessi.rina.model.jpa.entity.AdminNotificationType;

@Repository
public interface AdminNotificationTypeRepoExtended extends AdminNotificationTypeRepo {

    AdminNotificationType findByNotificationNameIgnoreCase(String notificationName);

}
