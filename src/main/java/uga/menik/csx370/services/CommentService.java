package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.csx370.models.Comment;
import uga.menik.csx370.models.User;

@Service
public class CommentService {

    private final DataSource dataSource;
    private final UserService userService;
    private final NotificationService notificationService;

    @Autowired
    public CommentService(
            DataSource dataSource,
            UserService userService,
            NotificationService notificationService) {
        this.dataSource = dataSource;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    /**
     * Get all comments
     */
    public List<Comment> getCommentsByPost(String postId) throws SQLException {
        final String sql = """
            SELECT c.commentId, c.content, c.postDate,
                   u.userId, u.firstName, u.lastName, u.profileImagePath
            FROM comment c
            JOIN user u ON c.userId = u.userId
            WHERE c.postId = ?
            ORDER BY c.postDate ASC
        """;

        List<Comment> comments = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, postId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                        rs.getString("userId"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("profileImagePath")
                    );

                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy, hh:mm a");
                    String formattedDate = dateFormat.format(rs.getTimestamp("postDate")); 

                    Comment comment = new Comment(
                        String.valueOf(rs.getInt("commentId")),
                        rs.getString("content"),
                        formattedDate,
                        user
                    );

                    comments.add(comment);
                }
            }
        }

        return comments;
    }

    /**
     * Add a comment
     */
    public boolean addComment(String postId, String userId, String content) throws SQLException {
        final String insertCommentSql = """
            INSERT INTO comment (postId, userId, content, postDate)
            VALUES (?, ?, ?, NOW())
        """;

        final String updateCommentCountSql = """
            UPDATE post SET commentsCount = commentsCount + 1
            WHERE postId = ?
        """;

        final String getPostOwnerSql = """
            SELECT user FROM post WHERE postId = ?
        """;

        try (Connection conn = dataSource.getConnection();
            PreparedStatement insertStmt = conn.prepareStatement(insertCommentSql);
            PreparedStatement updateStmt = conn.prepareStatement(updateCommentCountSql);
            PreparedStatement getOwnerStmt = conn.prepareStatement(getPostOwnerSql)) {

            insertStmt.setString(1, postId);
            insertStmt.setInt(2, Integer.parseInt(userId));
            insertStmt.setString(3, content);
            int rowsAffected = insertStmt.executeUpdate();

            if (rowsAffected > 0) {
                updateStmt.setString(1, postId);
                updateStmt.executeUpdate();

                getOwnerStmt.setString(1, postId);
                String postOwnerId = null;
                try (ResultSet rs = getOwnerStmt.executeQuery()) {
                    if (rs.next()) {
                        postOwnerId = rs.getString("user");
                    }
                }

                User actor = userService.getLoggedInUser();
                if (postOwnerId != null && !postOwnerId.equals(actor.getUserId())) {
                    String message = actor.getFirstName() + " commented on your post: \"" + content + "\"";
                    notificationService.createNotification(
                        postOwnerId,
                        actor.getUserId(),
                        "COMMENT",
                        postId,
                        message
                    );
                }

                return true;
            }

            return false;
        }
    }
}