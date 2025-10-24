/**
Copyright (c) 2024 Sami Menik, PhD. All rights reserved.

This is a project developed by Dr. Menik to give the students an opportunity to apply database concepts learned in the class in a real world project. Permission is granted to host a running version of this software and to use images or videos of this work solely for the purpose of demonstrating the work to potential employers. Any form of reproduction, distribution, or transmission of the software's source code, in part or whole, without the prior written consent of the copyright owner, is strictly prohibited.
*/
package uga.menik.csx370.controllers;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uga.menik.csx370.models.Post;
import uga.menik.csx370.services.BookmarkService;
import uga.menik.csx370.services.UserService;
import uga.menik.csx370.models.User;

@Controller
@RequestMapping("/bookmarks")
public class BookmarksController {

    private final BookmarkService bookmarkService;
    private final UserService userService;

    public BookmarksController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
        this.userService = userService;
    } // BookmarksController

    @GetMapping
    public ModelAndView webpage(HttpServletRequest request) {
        ModelAndView mv = new ModelAndView("posts_page");
        String userId = userService.getLoggedInUser().getUserId();
        List<Post> posts = bookmarkService.getBookmarkedPosts(userId);
        mv.addObject("posts", posts);
        if (posts.isEmpty()) {
            mv.addObject("isNoContent", true);
        } // if 
        return mv;
    } // webpage
    
} // BookmarksController
