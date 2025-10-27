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
import uga.menik.csx370.models.ExpandedPost;
import uga.menik.csx370.models.User;

@Service
public class BookmarkService {

    private final DataSource dataSource;
    private final CommentService commentService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final PostService postService;

    @Autowired
    public BookmarkService(DataSource dataSource, CommentService commentService,
            UserService userService, NotificationService notificationService, PostService postService) {
        this.dataSource = dataSource;
        this.commentService = commentService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.postService = postService;
    } // BookmarkService


    public List<ExpandedPost> getBookmarkedPosts(String userId) throws SQLException{
        String sql =
        "SELECT " +
        "  p.postId, " +
        "  p.content, " +
        "  p.postDate, " +
        "  p.user AS authorId, " +
        "  p.heartsCount, " +
        "  p.commentsCount, " +
        "  u.userId, " + 
        "  u.username, " +
        "  u.firstName, " +
        "  u.lastName, " +
        "  u.profileImagePath, " +  
        "  EXISTS(SELECT 1 FROM like_post WHERE postId = p.postId AND userId = ?) as isHearted, " +
        "  EXISTS(SELECT 1 FROM bookmark WHERE postId = p.postId AND userId = ?) as isBookmarked " +
        "FROM bookmark b, post p, user u " +
        "WHERE p.postId = b.postId " +
        "  AND u.userId = p.user " +
        "  AND b.userId = ? " +
        "ORDER BY p.postDate DESC";

        List<ExpandedPost> posts = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, Integer.parseInt(userId));
            pstmt.setInt(2, Integer.parseInt(userId)); 
            pstmt.setInt(3, Integer.parseInt(userId));
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

                    List<Comment> comments = commentService.getCommentsByPost(rs.getString("postId"));

                    ExpandedPost post = new ExpandedPost(
                        rs.getString("postId"),
                        rs.getString("content"),
                        formattedDate,
                        user,
                        rs.getInt("heartsCount"),
                        rs.getInt("commentsCount"),
                        rs.getInt("isHearted") == 1,
                        rs.getInt("isBookmarked") == 1,
                        comments
                    );

                    posts.add(post);
                }
                
            }
            return posts; 
        } catch (SQLException e) { 
            throw e;
        }
    } // getBookmarkedPosts


    public void addBookmark(String postId, String userId) throws SQLException {
        String sql = "INSERT INTO bookmark (userId, postId) VALUES (?, ?)";
    
        try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(userId));
            stmt.setString(2, postId);
            stmt.executeUpdate();
        }

        
        User actor = userService.getLoggedInUser();
        String postOwnerId = postService.getPostOwnerId(postId);

        if (postOwnerId != null && !postOwnerId.equals(actor.getUserId())) {
            String message = actor.getFirstName() + " bookmarked your post";
            notificationService.createNotification(
                postOwnerId,
                actor.getUserId(),
                "BOOKMARK",
                postId,
                message
            );
        }

    }
    public void removeBookmark(String postId, String userId) throws SQLException {
        String sql = "DELETE FROM bookmark WHERE userId = ? AND postId = ?";
        
        try (Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(userId));
            stmt.setString(2, postId);
            stmt.executeUpdate();
        }

        User actor = userService.getLoggedInUser();
        String postOwnerId = postService.getPostOwnerId(postId);

        if (postOwnerId != null && !postOwnerId.equals(actor.getUserId())) {
            String message = actor.getFirstName() + " removed a bookmark from your post";
            notificationService.createNotification(
                postOwnerId,
                actor.getUserId(),
                "BOOKMARK",
                postId,
                message
            );
        }
    }
    
} // BookmarkService
