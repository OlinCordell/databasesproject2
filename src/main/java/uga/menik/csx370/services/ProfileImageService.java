package uga.menik.csx370.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileImageService {

    private final DataSource dataSource;

    @Autowired
    public ProfileImageService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Update user's profile image path.
     * @param userId
     * @param profileImagePath
     * @throws SQLException
     */
    public void updateProfileImage(String userId, String profileImagePath) throws SQLException {
        final String sql = "update `user` set profileImagePath = ? where userId = ?";
        try (Connection conn = dataSource.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, profileImagePath);
                pstmt.setInt(2, Integer.parseInt(userId));
                pstmt.executeUpdate();
            }
    }
}
