package uga.menik.csx370.services;

import uga.menik.csx370.models.ExpandedPost;
import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;
import uga.menik.csx370.models.Comment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostService {
    
    private final DataSource dataSource;
    private final CommentService commentService;

    @Autowired
    public PostService(DataSource dataSource, CommentService commentService) {
        this.dataSource = dataSource;
        this.commentService = commentService;
    }

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    public boolean createPost(String userId, String content) throws SQLException {

        final String sql = """
                insert into post (postId, content, postDate, user, heartsCount, commentsCount, isHearted, isBookmarked) 
                values (?, ?, ?, ?, 0, 0, FALSE, FALSE)
                """;

        // Automatically updates user's last active date
        final String updateSql = """
                update user set lastActiveDate = ? where userId = ?
                """;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (
                PreparedStatement pstmt = conn.prepareStatement(sql);
                PreparedStatement updateStmt = conn.prepareStatement(updateSql)
            ) {

                String postId = UUID.randomUUID().toString();
                Timestamp postDate = new Timestamp(System.currentTimeMillis());

                pstmt.setString(1, postId);
                pstmt.setString(2, content);
                pstmt.setTimestamp(3, postDate);
                pstmt.setInt(4, Integer.parseInt(userId));
                int rowsAffected = pstmt.executeUpdate();

                updateStmt.setTimestamp(1, postDate);
                updateStmt.setInt(2, Integer.parseInt(userId));
                updateStmt.executeUpdate();
                
                conn.commit();
                return rowsAffected > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }

    }


    public List<Post> getFollowedUsersPosts(String loggedInUserId) throws SQLException {
        
        final String sql = """
                select p.postId, p.content, p.postDate, p.user,
                    p.heartsCount, p.commentsCount, 
                    u.userId, u.firstName, u.lastName, u.profileImagePath,
                    EXISTS(SELECT 1 FROM like_post WHERE postId = p.postId AND userId = ?) as isHearted,
                    EXISTS(SELECT 1 FROM bookmark WHERE postId = p.postId AND userId = ?) as isBookmarked
                from post p
                join user u on p.user = u.userId
                join follows f on u.userId = f.followedId
                where f.followsId = ?
                order by p.postDate desc
                """;

        List<Post> posts = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, loggedInUserId);
            pstmt.setString(2, loggedInUserId);
            pstmt.setString(3, loggedInUserId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                        rs.getString("userId"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("profileImagePath")
                    );

                    Timestamp ts = rs.getTimestamp("postDate");
                    String formattedDate = "";
                    if (ts != null) {
                        formattedDate = new SimpleDateFormat("MMM dd, yyyy, hh:mm a")
                                .format(new Date(ts.getTime()));
                    }

                    Post post = new Post(
                        rs.getString("postId"),
                        rs.getString("content"),
                        formattedDate,
                        user,
                        rs.getInt("heartsCount"),
                        rs.getInt("commentsCount"),
                        rs.getInt("isHearted") == 1,
                        rs.getInt("isBookmarked") == 1
                    );

                    posts.add(post);
                }
            }
        }
        return posts;
    }

    public void addHeart(String postId, String userId) throws SQLException {
        String insertLikeSql = """
            INSERT IGNORE INTO like_post (userId, postId)
            VALUES (?, ?)
        """;

        String updateCountSql = """
            UPDATE post
            SET heartsCount = (SELECT COUNT(*) FROM like_post WHERE postId = ?)
            WHERE postId = ?
        """;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement insertStmt = conn.prepareStatement(insertLikeSql);
                PreparedStatement updateStmt = conn.prepareStatement(updateCountSql)) {

                insertStmt.setInt(1, Integer.parseInt(userId));
                insertStmt.setString(2, postId);
                insertStmt.executeUpdate();

                updateStmt.setString(1, postId);
                updateStmt.setString(2, postId);
                updateStmt.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }

        User actor = userService.getLoggedInUser();
        String postOwnerId = getPostOwnerId(postId);
        if (postOwnerId != null && !postOwnerId.equals(actor.getUserId())) { 
            String message = actor.getFirstName() + " liked your post";
            notificationService.createNotification(
                postOwnerId,
                actor.getUserId(),
                "LIKE",
                postId,
                message
            );
        }
    }

    public void removeHeart(String postId, String userId) throws SQLException {
        String deleteLikeSql = """
            DELETE FROM like_post WHERE userId = ? AND postId = ?
        """;

        String updateCountSql = """
            UPDATE post
            SET heartsCount = (SELECT COUNT(*) FROM like_post WHERE postId = ?)
            WHERE postId = ?
        """;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteLikeSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateCountSql)) {

                deleteStmt.setInt(1, Integer.parseInt(userId));
                deleteStmt.setString(2, postId);
                deleteStmt.executeUpdate();

                updateStmt.setString(1, postId);
                updateStmt.setString(2, postId);
                updateStmt.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
        User actor = userService.getLoggedInUser();
        String postOwnerId = getPostOwnerId(postId);
        if (postOwnerId != null && !postOwnerId.equals(actor.getUserId())) { 
            String message = actor.getFirstName() + " unliked your post";
            notificationService.createNotification(
                postOwnerId,
                actor.getUserId(),
                "LIKE",
                postId,
                message
            );
        }
    }

    public ExpandedPost getPostById(String postId, String currentUserId) throws SQLException {
        final String sql = """
                select p.postId, p.content, p.postDate, p.user, p.heartsCount, p.commentsCount,
                    u.userId, u.firstName, u.lastName, u.profileImagePath,
                    EXISTS(SELECT 1 FROM like_post WHERE postId = p.postId AND userId = ?) as isHearted,
                    EXISTS(SELECT 1 FROM bookmark WHERE postId = p.postId AND userId = ?) as isBookmarked
                from post p
                join user u on p.user = u.userId
                where p.postId = ?
                """;

    
        try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(currentUserId));
            pstmt.setInt(2, Integer.parseInt(currentUserId));
            pstmt.setString(3, postId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    
                    User postUser = new User(
                        rs.getString("userId"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("profileImagePath")
                    );

                    List<Comment> comments = commentService.getCommentsByPost(rs.getString("postId"));

                    Timestamp ts = rs.getTimestamp("postDate");
                    String formattedDate = "";
                    if (ts != null) {
                        formattedDate = new SimpleDateFormat("MMM dd, yyyy, hh:mm a")
                                .format(new Date(ts.getTime()));
                    }

                    return new ExpandedPost(
                        rs.getString("postId"),
                        rs.getString("content"),
                        formattedDate,
                        postUser,
                        rs.getInt("heartsCount"),
                        rs.getInt("commentsCount"),
                        rs.getInt("isHearted") == 1,
                        rs.getInt("isBookmarked") == 1,
                        comments
                    );
                } else {
                    return null;
                }
            }
        }
    }

    public List<ExpandedPost> getPostsById(String userId, String currentUserId) throws SQLException {
        final String sql = """
            select p.postId, p.content, p.postDate, p.user, p.heartsCount, p.commentsCount,
                u.userId, u.firstName, u.lastName, u.profileImagePath,   
                EXISTS(SELECT 1 FROM like_post WHERE postId = p.postId AND userId = ?) as isHearted,
                EXISTS(SELECT 1 FROM bookmark WHERE postId = p.postId AND userId = ?) as isBookmarked
            from post p
            join user u on p.user = u.userId
            where p.user = ?
            order by p.postDate desc
        """;

        List<ExpandedPost> posts = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, Integer.parseInt(currentUserId));
            pstmt.setInt(2, Integer.parseInt(currentUserId));
            pstmt.setInt(3, Integer.parseInt(userId));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User postUser = new User(
                        rs.getString("userId"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("profileImagePath")
                    );

                    List<Comment> comments = commentService.getCommentsByPost(rs.getString("postId"));

                    Timestamp ts = rs.getTimestamp("postDate");
                    String formattedDate = "";
                    if (ts != null) {
                        formattedDate = new SimpleDateFormat("MMM dd, yyyy, hh:mm a")
                            .format(new Date(ts.getTime()));
                    }

                    posts.add(new ExpandedPost(
                        rs.getString("postId"),
                        rs.getString("content"),
                        formattedDate,
                        postUser,
                        rs.getInt("heartsCount"),
                        rs.getInt("commentsCount"),
                        rs.getInt("isHearted") == 1,
                        rs.getInt("isBookmarked") == 1,
                        comments
                    ));
                }
            }
        }

        return posts;
    }

    public String getPostOwnerId(String postId) throws SQLException {
        final String sql = "SELECT user FROM post WHERE postId = ?";
        try (Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("user");
                }
            }
        }
        return null;
    }


    /**
     * Deletes a post
     */
    public boolean deletePost(String postId, String userId) throws SQLException {
        final String sql = """
            DELETE FROM post WHERE postId = ? AND user = ?
        """;

        try (Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, postId);
            stmt.setInt(2, Integer.parseInt(userId));
            return stmt.executeUpdate() > 0;
        }
}
}
