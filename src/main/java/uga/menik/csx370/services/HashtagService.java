package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.models.User;

@Service
public class HashtagService {
    
    private final DataSource dataSource;

    @Autowired
    public HashtagService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Post> getPostsByHashtags(String[] hashtags, String loggedInUserId) throws SQLException {
        
        if (hashtags == null || hashtags.length == 0) {
            return List.of();
        }

        StringBuilder sql = new StringBuilder("""
            SELECT p.postId, p.content, p.postDate,
                   p.heartsCount, p.commentsCount, p.isHearted, p.isBookmarked,
                   u.userId, u.firstName, u.lastName, u.profileImagePath
            FROM post p
            JOIN user u ON p.user = u.userId
            WHERE
        """);

        for (int i = 0; i < hashtags.length; i++) {
            sql.append(" LOWER(p.content) LIKE LOWER(?) ");
            if (i < hashtags.length - 1) sql.append(" OR ");
        }

        sql.append(" ORDER BY p.postDate DESC");

        List<Post> posts = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < hashtags.length; i++) {
                String tag = hashtags[i].replace("#", "").trim();
                pstmt.setString(i + 1, "%#" + tag + "%");
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    User user = new User(
                            rs.getString("userId"),
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            rs.getString("profileImagePath")
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
}
