/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/


package uga.menik.csx370.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.services.BookmarkService;
import uga.menik.csx370.models.ExpandedPost;  
import uga.menik.csx370.models.User;
import uga.menik.csx370.services.UserService;

@Controller
@RequestMapping("/bookmarks")
public class BookmarksController {

    private final BookmarkService bookmarkService;
    private final UserService userService;

    @Autowired
    public BookmarksController(BookmarkService bookmarkService, UserService userService) {
        this.bookmarkService = bookmarkService;
        this.userService = userService;
    }

    @GetMapping
    public ModelAndView webpage() {
        ModelAndView mv = new ModelAndView("posts_page");

        User loggedInUser = userService.getLoggedInUser();
        String userId = loggedInUser.getUserId();

        try {
            List<ExpandedPost> posts = bookmarkService.getBookmarkedPosts(userId);
            mv.addObject("posts", posts);
            mv.addObject("loggedInUser", loggedInUser);

            if (posts.isEmpty()) {
                mv.addObject("isNoContent", true);
            }

            mv.addObject("pageTitle", "Bookmarks");
        
        } catch (Exception e) {  
            e.printStackTrace();
            mv.addObject("errorMessage", "Error loading bookmarks");
        }
        return mv;
    }
}
