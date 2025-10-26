package uga.menik.csx370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.ModelAttribute;
import java.util.stream.Collectors;


import uga.menik.csx370.models.Notification;
import uga.menik.csx370.services.NotificationService;
import uga.menik.csx370.services.UserService;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @Autowired
    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    /**
     * Show all notifications for the logged-in user
     */
    @GetMapping
    public ModelAndView notificationsPage(@RequestParam(name = "error", required = false) String error) {
        ModelAndView mv = new ModelAndView("notifications_page");
        mv.addObject("loggedInUser", userService.getLoggedInUser());

        try {
            String userId = userService.getLoggedInUser().getUserId();
            List<Notification> notifications = notificationService.getNotificationsForUser(userId);
            mv.addObject("notifications", notifications);
            if (notifications.isEmpty()) {
                mv.addObject("isNoContent", true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mv.addObject("errorMessage", "Failed to load notifications.");
        }

        if (error != null) {
            mv.addObject("errorMessage", error);
        }

        return mv;
    }

    /**
     * Mark all notifications as read
     */
    @PostMapping("/markAllRead")
    public String markAllAsRead() {
        try {
            String userId = userService.getLoggedInUser().getUserId();
            notificationService.markAllAsRead(userId);
        } catch (SQLException e) {
            e.printStackTrace();
            String message = URLEncoder.encode("Failed to mark notifications as read.", StandardCharsets.UTF_8);
            return "redirect:/notifications?error=" + message;
        }
        return "redirect:/notifications";
    }

    /**
     * Delete a single notification
     */
    @PostMapping("/delete")
    public String deleteNotification(@RequestParam("notificationId") String notificationId) {
        try {
            String userId = userService.getLoggedInUser().getUserId();
            boolean deleted = notificationService.deleteNotification(notificationId, userId);
            if (!deleted) {
                System.out.println("Failed to delete notification with ID: " + notificationId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "redirect:/notifications";
    }

    @ModelAttribute("topBarNotifications")
    public List<Notification> topBarNotifications() {
        try {
            String userId = userService.getLoggedInUser().getUserId();
            List<Notification> notifications = notificationService.getNotificationsForUser(userId);

            // Only send latest 5 notifications
            return notifications.stream().limit(5).collect(Collectors.toList());
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    @ModelAttribute("unreadCount")
    public long topBarUnreadCount() {
        return topBarNotifications().stream().filter(n -> !n.isRead()).count();
    }
}
