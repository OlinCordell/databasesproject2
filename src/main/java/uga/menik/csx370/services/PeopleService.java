/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.services;

import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import org.springframework.stereotype.Service;

import uga.menik.csx370.models.FollowableUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * This service contains people related functions.
 */
@Service
public class PeopleService {

    // dataSource enables talking to the database.
    private final DataSource dataSource;

    /**
     * See AuthInterceptor notes regarding dependency injection and
     * inversion of control.
     */
    @Autowired
    public PeopleService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    /**
     * This function should query and return all users that 
     * are followable. The list should not contain the user 
     * with id userIdToExclude.
     */
    public List<FollowableUser> getFollowableUsers(String userIdToExclude) throws SQLException {
        
        // Finds users that are not the currently logged in user.
        final String sql = """
                            select u.userId, u.firstName, u.lastName, u.lastActiveDate, u.profileImagePath,
                                case when f.followedId is not null then true else false end as isFollowed
                            from user u
                            left join follows f on u.userId = f.followedId and f.followsId = ?
                            where u.userId <> ?
                        """;

        List<FollowableUser> followableUsers = new ArrayList<>();
       
        try (Connection conn = dataSource.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userIdToExclude);
            pstmt.setString(2, userIdToExclude);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    String userId = rs.getString("userId");
                    String firstName = rs.getString("firstName");
                    String lastName = rs.getString("lastName");
                    boolean isFollowed = rs.getBoolean("isFollowed");
                    String profileImagePath = rs.getString("profileImagePath");
                    Timestamp ts = rs.getTimestamp("lastActiveDate");
                    String lastActiveDate = "";
                    if (ts != null) {
                        lastActiveDate = new SimpleDateFormat("MMM dd, yyyy, hh:mm a")
                                .format(new Date(ts.getTime()));
                    } else {
                        lastActiveDate = "N/A";
                    }

                    followableUsers.add(new FollowableUser(
                        userId, firstName, lastName, profileImagePath, isFollowed, lastActiveDate));
                    
                }
            } 
        } catch (SQLException e) {
                e.printStackTrace();
        }

        return followableUsers;
    }

    public void followUser(String followsId, String followedId) throws SQLException {
        final String sql = """
            INSERT INTO follows (followsId, followedId)
            VALUES (?, ?)
        """;

        try (Connection conn = dataSource.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, Integer.parseInt(followsId));
            pstmt.setInt(2, Integer.parseInt(followedId));
            pstmt.executeUpdate();

            String followerName = userService.getUserById(followsId).getFirstName();
            String message = followerName + " started following you.";
            notificationService.createNotification(
                followedId,
                followsId,
                "FOLLOW",
                null,
                message
            );
        }
    }

    public void unfollowUser(String followsId, String followedId) throws SQLException {
        final String sql = """
            DELETE FROM follows
            WHERE followsId = ? AND followedId = ?
        """;

        try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(followsId));
            stmt.setInt(2, Integer.parseInt(followedId));
            stmt.executeUpdate();

            String followerName = userService.getUserById(followsId).getFirstName();
            String message = followerName + " unfollowed you.";
            notificationService.createNotification(
                followedId,
                followsId,
                "FOLLOW",
                null,
                message
            );
        }
    }
}
