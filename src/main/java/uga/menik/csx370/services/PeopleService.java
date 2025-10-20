/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.services;

import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import uga.menik.csx370.models.FollowableUser;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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
                    String lastActiveDate = rs.getString("lastActiveDate");
                    if (lastActiveDate == null) {
                        lastActiveDate = "N/A";
                    }

                    followableUsers.add(new FollowableUser(
                        userId, firstName, lastName, isFollowed, lastActiveDate));
                    
                }
            } 
        } catch (SQLException e) {
                e.printStackTrace();
        }

        return followableUsers;
    }

    public void followUser(String followsId, String followedId) throws SQLException {
        final String sql = """
                insert into follows (followsId, followedId) values (?, ?)
        """;
        final String updateSql  = """
                UPDATE user
                SET lastActive = CURRENT_TIMESTAMP
                WHERE userId = ?
        """;
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try  (PreparedStatement pstmt = conn.prepareStatement(sql);
                PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    pstmt.setString(1, followsId);
                    pstmt.setString(2, followedId);
                    pstmt.executeUpdate();

                    updateStmt.setString(1, followsId);
                    updateStmt.executeUpdate();

                    conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void unfollowUser(String followsId, String followedId) throws SQLException {
        final String sql = """
            DELETE FROM follows
            WHERE followsId = ? AND followedId = ?
        """;

        final String updateSql = """
            UPDATE user
            SET lastActive = CURRENT_TIMESTAMP
            WHERE userId = ?
        """;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                PreparedStatement updateStmt = conn.prepareStatement(updateSql)
            ) {
                stmt.setString(1, followsId);
                stmt.setString(2, followedId);
                stmt.executeUpdate();

                updateStmt.setString(1, followsId);
                updateStmt.executeUpdate();

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

}
