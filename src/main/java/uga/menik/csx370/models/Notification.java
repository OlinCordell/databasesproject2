/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.models;

/**
 * Represents a notification that appears for a user when another user
 * interacts with their content (e.g., comments, hearts, bookmarks).
 */
public class Notification {
    
    /**
     * The unique identifier of the notification.
     */
    private final String notificationId;
    
    /**
     * The user who receives the notification.
     */
    private final User recipient;
    
    /**
     * The user who triggered the notification.
     */
    private final User sender;
    
    /**
     * The message or description of the notification.
     * Example: "Alice commented on your post"
     */
    private final String message;
    
    /**
     * The date and time when the notification was created.
     */
    private final String createdAt;
    
    /**
     * The flag indicating whether the notification has been read by the recipient.
     */
    private boolean isRead;
    
    /**
     * Constructs a new Notification with the specified details.
     *
     * @param notificationId  the unique ID of the notification
     * @param recipient       the user who receives the notification
     * @param sender          the user who triggered the notification
     * @param message         the message of the notification
     * @param createdAt       the timestamp when it was created
     * @param isRead          whether the notification has been read
     */
    public Notification(String notificationId, User recipient, User sender, String message, String createdAt, boolean isRead) {
        this.notificationId = notificationId;
        this.recipient = recipient;
        this.sender = sender;
        this.message = message;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    /** Returns the ID of the notification. */
    public String getNotificationId() {
        return notificationId;
    }

    /** Returns the recipient user. */
    public User getRecipient() {
        return recipient;
    }

    /** Returns the sender user. */
    public User getSender() {
        return sender;
    }

    /** Returns the message text of the notification. */
    public String getMessage() {
        return message;
    }

    /** Returns the timestamp of when the notification was created. */
    public String getCreatedAt() {
        return createdAt;
    }

    /** Returns whether the notification has been read. */
    public boolean isRead() {
        return isRead;
    }

    /** Marks the notification as read. */
    public void markAsRead() {
        this.isRead = true;
    }

    /** Marks the notification as unread. */
    public void markAsUnread() {
        this.isRead = false;
    }
}
