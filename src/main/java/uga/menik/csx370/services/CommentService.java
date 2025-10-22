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

import uga.menik.csx370.models.Comment;

@Service
public class CommentService {
    
    private final DataSource dataSource;

    @Autowired
    public CommentService(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Comment> getCommentsByPostId(String postId) throws SQLException {
        return new ArrayList<>();
    }

    public List<Comment> getCommentsByPost(String postId) throws SQLException {
        return new ArrayList<>();
    }

    public boolean addComment(String postId, String userId, String content) throws SQLException {   
        return false;
    
    }
}