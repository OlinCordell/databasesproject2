package uga.menik.csx370.repositories;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import uga.menik.csx370.models.Post;

@Repository
public class BookmarkRepository {

    private final JdbcTemplate jdbc;

    public BookmarkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    }

    public int addBookmark(int userId, String postId) {
        String sql = "INSERT IGNORE INTO bookmark (userId, postId) VALUES (?, ?)";
        return jdbc.update(sql, userId, postId);
    }

    public int removeBookmark(int userId, String postId) {
        String sql = "DELETE FROM bookmark WHERE userId = ? AND postId = ?";
        return jdbc.update(sql, userId, postId);
    }

    public boolean isBookmarked(int userId, String postId) {
        String sql = "SELECT 1 FROM bookmark WHERE userId = ? AND postId = ? LIMIT 1";
        List<Integer> rows = jdbc.query(sql, (rs, i) -> rs.getInt(1), userId, postId);
        return !rows.isEmpty();
    }

    public List<Post> findAllBookmarkedPosts(int userId) {
        String sql = """
            SELECT
                p.postId,
                p.content,
                p.postDate,
                p.user    AS authorId,
                p.heartsCount,
                p.commentsCount,
                u.username,
                u.firstName,
                u.lastName
            FROM bookmark b
            JOIN post p ON p.postId = b.postId
            JOIN user u ON u.userId = p.user
            WHERE b.userId = ?
            ORDER BY p.postDate DESC
            """;
        return jdbc.query(sql, (rs, i) -> mapPost(rs, userId), userId);
    }

    private Post mapPost(ResultSet rs, int currentUserId) throws SQLException {
        Post p = new Post();
        p.setPostId(rs.getString("postId"));
        p.setContent(rs.getString("content"));
        p.setPostDate(rs.getTimestamp("postDate").toInstant()); // adapt to your model’s type
        p.setUserId(rs.getInt("authorId"));
        p.setHeartsCount(rs.getInt("heartsCount"));
        p.setCommentsCount(rs.getInt("commentsCount"));
        p.setUsername(rs.getString("username"));     // if your Post has these fields
        p.setFirstName(rs.getString("firstName"));
        p.setLastName(rs.getString("lastName"));
        p.setBookmarked(true); // since we're querying from the bookmark table
        return p;
    }
}
