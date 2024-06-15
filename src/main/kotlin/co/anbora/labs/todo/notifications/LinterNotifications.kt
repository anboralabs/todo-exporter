package co.anbora.labs.todo.notifications

import co.anbora.labs.todo.icons.TodoExporterIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.project.Project

object LinterNotifications {

    @JvmStatic
    fun createNotification(
        title: String,
        content: String,
        type: NotificationType,
        vararg actions: AnAction
    ): Notification {
        val notification = NotificationGroupManager.getInstance()
            .getNotificationGroup("todo.exporter.notification")
            .createNotification(content, type)
            .setTitle(title)
            .setIcon(TodoExporterIcons.TODO_32)

        for (action in actions) {
            notification.addAction(action)
        }

        return notification
    }

    @JvmStatic
    fun showNotification(notification: Notification, project: Project?) {
        try {
            notification.notify(project)
        } catch (e: Exception) {
            notification.notify(project)
        }
    }
}
