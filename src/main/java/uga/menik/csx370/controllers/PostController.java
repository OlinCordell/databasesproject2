/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.controllers;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.models.ExpandedPost;
import uga.menik.csx370.services.PostService;
import uga.menik.csx370.services.UserService;
import uga.menik.csx370.services.CommentService;
import uga.menik.csx370.services.BookmarkService;

/**
 * Handles /post URL and its sub urls.
 */
@Controller
@RequestMapping("/post")
public class PostController {

    private final UserService userService;
    private final PostService postService;
    private final CommentService commentService;
    private final BookmarkService bookmarkService;

    @Autowired
    public PostController(UserService userService, PostService postService, CommentService commentService, BookmarkService bookmarkService) {
        this.userService = userService;
        this.postService = postService;
        this.commentService = commentService;
        this.bookmarkService = bookmarkService;
    }

    /**
     * This function handles the /post/{postId} URL.
     * This handlers serves the web page for a specific post.
     * Note there is a path variable {postId}.
     * An example URL handled by this function looks like below:
     * http://localhost:8081/post/1
     * The above URL assigns 1 to postId.
     * 
     * See notes from HomeController.java regardig error URL parameter.
     */
    @GetMapping("/{postId}")
    public ModelAndView webpage(@PathVariable("postId") String postId,
            @RequestParam(name = "error", required = false) String error) {
        System.out.println("The user is attempting to view post with id: " + postId);
        // See notes on ModelAndView in BookmarksController.java.
        ModelAndView mv = new ModelAndView("posts_page");
        mv.addObject("loggedInUser", userService.getLoggedInUser());
        String currentUserId = userService.getLoggedInUser().getUserId();

        try {
            ExpandedPost post = postService.getPostById(postId, currentUserId);
            if (post != null) {
                mv.addObject("posts", List.of(post));
            } else {
                mv.addObject("isNoContent", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = error;
            mv.addObject("errorMessage", errorMessage);
        }

        return mv;
    }

    /**
     * Handles comments added on posts.
     * See comments on webpage function to see how path variables work here.
     * This function handles form posts.
     * See comments in HomeController.java regarding form submissions.
     */
    @PostMapping("/{postId}/comment")
    public String postComment(@PathVariable("postId") String postId,
            @RequestParam(name = "comment") String comment) {
        System.out.println("The user is attempting add a comment:");
        System.out.println("\tpostId: " + postId);
        System.out.println("\tcomment: " + comment);

        try {
            String userId = userService.getLoggedInUser().getUserId();
            boolean success = commentService.addComment(postId, userId, comment);

            if (success) {
                return "redirect:/post/" + postId;
            } else {
                String message = URLEncoder.encode("Failed to post the comment. Please try again.",
                    StandardCharsets.UTF_8);
                return "redirect:/post/" + postId + "?error=" + message;    
            }
        } catch (Exception e) {
            e.printStackTrace();
            String message = URLEncoder.encode(
                "Failed to post the comment. Please try again.",
                StandardCharsets.UTF_8
            );
            return "redirect:/post/" + postId + "?error=" + message;
        }
        
    }

    /**
     * Handles likes added on posts.
     * See comments on webpage function to see how path variables work here.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * get type form submissions and how path variables work.
     */
    @GetMapping("/{postId}/heart/{isAdd}")
    public String addOrRemoveHeart(@PathVariable("postId") String postId,
            @PathVariable("isAdd") Boolean isAdd) {
        System.out.println("The user is attempting add or remove a heart:");
        System.out.println("\tpostId: " + postId);
        System.out.println("\tisAdd: " + isAdd);

        try {
            if (isAdd) {
                postService.addHeart(postId, userService.getLoggedInUser().getUserId());
            } else {
                postService.removeHeart(postId, userService.getLoggedInUser().getUserId());
            }
            return "redirect:/post/" + postId;
        } catch (Exception e) {
            e.printStackTrace();
            String message = URLEncoder.encode("Failed to (un)like the post. Please try again.",
                StandardCharsets.UTF_8);
            return "redirect:/post/" + postId + "?error=" + message;
        }    
    }

    /**
     * Handles bookmarking posts.
     * See comments on webpage function to see how path variables work here.
     * See comments in PeopleController.java in followUnfollowUser function regarding 
     * get type form submissions.
     */
    @GetMapping("/{postId}/bookmark/{isAdd}")
    public String addOrRemoveBookmark(@PathVariable("postId") String postId,
            @PathVariable("isAdd") Boolean isAdd) {
        System.out.println("The user is attempting add or remove a bookmark:");
        System.out.println("\tpostId: " + postId);
        System.out.println("\tisAdd: " + isAdd);

        // Redirect the user if the comment adding is a success.
        // return "redirect:/post/" + postId;

        // Redirect the user with an error message if there was an error.
        try {
            String userId = userService.getLoggedInUser().getUserId();
            
            if (isAdd) {
                bookmarkService.addBookmark(postId, userId);
            } else {
                bookmarkService.removeBookmark(postId, userId);
            }
            
            return "redirect:/";
        
        } catch (SQLException e) {
            e.printStackTrace();
            String message = URLEncoder.encode("Failed to (un)bookmark the post. Please try again.",
                    StandardCharsets.UTF_8);
            return "redirect:/post/" + postId + "?error=" + message;
        }
    }

    @PostMapping("/delete/{postId}")
    public String deletePost(@PathVariable("postId") String postId) {
        try {
            String userId = userService.getLoggedInUser().getUserId();
            boolean deleted = postService.deletePost(postId, userId);
            
            if (!deleted) {
                System.out.println("Failed to delete post");
            }
        } catch (SQLException e) { 
            e.printStackTrace();
            System.out.println("Error deleting post: " + e.getMessage());
        }
        return "redirect:/profile";
    }
    
}
