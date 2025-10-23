package uga.menik.csx370.controllers;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import uga.menik.csx370.services.BookmarkService;

@Controller
@RequestMapping("/posts")
public class BMPostController {

    private final BookmarkService bookmarkService;

    public BMPostController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping("/{postId}/bookmark")
    @ResponseBody
    public ResponseEntity<?> ctrlBookmark(
            @PathVariable String postId,
            HttpServletRequest request) {

        int userId = Auth.currentUserId(request); 

        boolean nowBookmarked = bookmarkService.ctrlBookmark(userId, postId);

        return ResponseEntity.ok().body("{\"bookmarked\": " + nowBookmarked + "}");
    }
}
