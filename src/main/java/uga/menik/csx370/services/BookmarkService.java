package uga.menik.csx370.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import uga.menik.csx370.models.Post;
import uga.menik.csx370.repo.BookmarkRepo;

@Service
public class BookmarkService {

    private final BookmarkRepo repo;

    public BookmarkService(BookmarkRepo repo) {
        this.repo = repo;
    } // BookmarkService 

    public List<Post> getBookmarkedPosts(String userId) {
        return repo.findAllBMs(userId);
    } // getBookmarkedPosts

    @Transactional
    public boolean ctrlBookmark(String userId, String postId) {
        boolean already = repo.isBM(userId, postId);
        if (isBookmarked) {
            repo.removeBM(userId, postId);
            return false;
        } else {
            repo.addBM(userId, postId);
            return true;
        } // if else
    } // ctrlBookmark

    public boolean isBookmarked(String userId, String postId) {
        return repo.isBM(userId, postId);
    } // isBookmarked
    
} // BookmarkService
