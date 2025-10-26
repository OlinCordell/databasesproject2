package uga.menik.csx370.services;

import uga.menik.csx370.models.Notification;
import uga.menik.csx370.models.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * Handles creation, retrieval, and update of notifications for users.
 */
@Service
public class NotificationService {

    private final DataSource dataSource;

    @Autowired
    public NotificationService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Creates a new notification for the recipient user.
     *
     * @param recipientId the user receiving the notification
     * @param senderId    the user who triggered the notification
     * @param message     the text of the notification
     */
    public void createNotification(String recipientId, String actorId, String type, String postId, String message) throws SQLException {
        final String sql = "INSERT INTO notification (userId, actorId, type, postId, message) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(recipientId));
            pstmt.setInt(2, Integer.parseInt(actorId));
            pstmt.setString(3, type);
            pstmt.setString(4, postId);
            pstmt.setString(5, message);

            pstmt.executeUpdate();
        }
    }


    /**
     * Retrieves all notifications for a specific user.
     *
     * @param userId the ID of the user
     * @return list of notifications
     */
    public List<Notification> getNotificationsForUser(String userId) throws SQLException {
        final String sql = """
            SELECT n.notificationId, n.userId, n.actorId, n.type, n.postId, n.message, n.isRead, n.createdAt,
                u.userId AS senderId, u.firstName, u.lastName, u.profileImagePath
            FROM notification n
            JOIN user u ON n.actorId = u.userId
            WHERE n.userId = ?
            ORDER BY n.createdAt DESC
        """;

        List<Notification> notifications = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(userId));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("createdAt");
                    String formattedDate = ts != null
                            ? new SimpleDateFormat("MMM dd, yyyy, hh:mm a").format(new Date(ts.getTime()))
                            : "";

                    User sender = new User(
                        rs.getString("senderId"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("profileImagePath")
                    );

                    Notification notification = new Notification(
                        rs.getString("notificationId"),
                        null,
                        sender,
                        rs.getString("message"),
                        formattedDate,
                        rs.getBoolean("isRead")
                    );

                    notifications.add(notification);
                }
            }
        }

        return notifications;
    }


    /**
     * Marks all notifications for a given user as read.
     *
     * @param userId the ID of the user
     */
    public void markAllAsRead(String userId) throws SQLException {
        final String sql = """
            DELETE FROM notification
            WHERE userId = ?
        """;

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(userId));
            pstmt.executeUpdate();
        }
    }

    /**
     * Deletes a specific notification.
     *
     * @param notificationId the ID of the notification
     * @param userId         the ID of the recipient (for security)
     * @return true if deleted, false otherwise
     */
    public boolean deleteNotification(String notificationId, String userId) throws SQLException {
        final String sql = """
            DELETE FROM notification WHERE notificationId = ? AND userId = ?
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, notificationId);
            pstmt.setInt(2, Integer.parseInt(userId));
            return pstmt.executeUpdate() > 0;
        }
    }
}