import { initDeps } from "../../dependencies.js";
import { FirestoreNotificationRepository } from "../firestore/FirestoreNotificationRepository.js";
import { NotificationDeliveryService } from "../../services/notificationDeliveryService.js";

let singleton:
  | {
      notifications: FirestoreNotificationRepository;
      notificationDelivery: NotificationDeliveryService;
    }
  | null = null;

export function notificationContainer() {
  if (singleton) {
    return singleton;
  }

  const { firestore, messaging } = initDeps();
  const notifications = new FirestoreNotificationRepository(firestore);
  singleton = {
    notifications,
    notificationDelivery: new NotificationDeliveryService(notifications, messaging),
  };
  return singleton;
}
