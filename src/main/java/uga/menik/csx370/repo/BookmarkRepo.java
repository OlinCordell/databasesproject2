package uga.menik.csx370.repo;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import uga.menik.csx370.models.Post;

@Repository
public class BookmarkRepo {

    private final JdbcTemplate jdbc;

    public BookmarkRepo(JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbcTemplate;
    } //BookmarkRepo

    public int addBM(String userId, String postId) {
        String sql = "INSERT IGNORE INTO bookmark (userId, postId) VALUES (?, ?)";
        return jdbc.update(sql, userId, postId);
    } // addBM

    public int removeBM(String userId, String postId) {
        String sql = "DELETE FROM bookmark WHERE userId = ? AND postId = ?";
        return jdbc.update(sql, userId, postId);
    } // removeBM

    public boolean isBM(String userId, String postId) {
        String sql = "SELECT 1 FROM bookmark WHERE userId = ? AND postId = ? LIMIT 1";
        List<Integer> rows = jdbc.query(sql, (rs, i) -> rs.getInt(1), userId, postId);
        return !rows.isEmpty();
    } // isBM

public List<Post> findAllBMs(String userId) {
    String sql =
        "SELECT " +
        "  p.postId, " +
        "  p.content, " +
        "  p.postDate, " +
        "  p.user AS authorId, " +
        "  p.heartsCount, " +
        "  p.commentsCount, " +
        "  u.username, " +
        "  u.firstName, " +
        "  u.lastName " +
        "FROM bookmark b, post p, user u " +
        "WHERE p.postId = b.postId " +
        "  AND u.userId = p.user " +
        "  AND b.userId = ? " +
        "ORDER BY p.postDate DESC";
    return jdbc.query(sql, (rs, i) -> mapPost(rs, userId), userId);
} // findAllBMs
    

    private Post mapPost(ResultSet rs, String currentUserId) throws SQLException {
        Post p = new Post();
        p.setPostId(rs.getString("postId"));
        p.setContent(rs.getString("content"));
        p.setPostDate(rs.getTimestamp("postDate").toInstant()); 
        p.setUserId(rs.getInt("authorId"));
        p.setHeartsCount(rs.getInt("heartsCount"));
        p.setCommentsCount(rs.getInt("commentsCount"));
        p.setUsername(rs.getString("username"));    
        p.setFirstName(rs.getString("firstName"));
        p.setLastName(rs.getString("lastName"));
        p.setBookmarked(true);
        return p;
    } // mapPost

    
} // BookmarkRepo
