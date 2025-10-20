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
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

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
                    p.heartsCount, p.commentsCount, p.isHearted, p.isBookmarked, 
                    u.userId, u.firstName, u.lastName, u.profileImagePath
                from post p
                join user u on p.user = u.userId
                join follows f on u.userId = f.followedId
                where f.followsId = ?
                order by p.postDate desc
                limit 10;
                """;

        List<Post> posts = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, loggedInUserId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                        rs.getString("userId"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("profileImagePath") != null 
                            ? rs.getString("profileImagePath") : "/avatars/default.png"
                    );

                    Post post = new Post(
                        rs.getString("postId"),
                        rs.getString("content"),
                        rs.getString("postDate"),
                        user,
                        rs.getInt("heartsCount"),
                        rs.getInt("commentsCount"),
                        rs.getBoolean("isHearted"),
                        rs.getBoolean("isBookmarked")
                    );

                    posts.add(post);
                }
            }
        }
        return posts;
    }

    public void addHeart(String postId) throws SQLException {
        final String sql = """
                update post set heartsCount = heartsCount + 1, isHearted = TRUE where postId = ?
                """;
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postId);
            pstmt.executeUpdate();
        }
    }

    public void removeHeart(String postId) throws SQLException {
        final String sql = """
                update post set heartsCount = heartsCount - 1,
                    isHearted = case when heartsCount - 1 <= 0 then false else true end
                    where postId = ? and heartsCount > 0;
                """;
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, postId);
            pstmt.executeUpdate();
        }
    }

    public ExpandedPost getPostById(String postId) throws SQLException {
        final String sql = """
                select *
                from post p
                join user u on p.user = u.userId
                where p.postId = ?
                """;
        try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, postId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    
                    User postUser = new User(
                        rs.getString("userId"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("profileImagePath") != null 
                            ? rs.getString("profileImagePath") : "/avatars/default.png"
                    );

                    List<Comment> comments = commentService.getCommentsByPost(postId);

                    return new ExpandedPost(
                        rs.getString("postId"),
                        rs.getString("content"),
                        rs.getString("postDate"),
                        postUser,
                        rs.getInt("heartsCount"),
                        rs.getInt("commentsCount"),
                        rs.getBoolean("isHearted"),
                        rs.getBoolean("isBookmarked"),
                        comments
                    );
                } else {
                    return null;
                }
            }
        }
    }

}
