package uga.menik.csx370.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.services.ProfileImageService;
import uga.menik.csx370.services.UserService;

@Controller
public class ProfileImageController {
    
    private final ProfileImageService profileImageService;
    private final UserService userService;

    public ProfileImageController(ProfileImageService profileImageService, UserService userService) {
        this.profileImageService = profileImageService;
        this.userService = userService;
    }

    @GetMapping("/profile/select-avatar")
    public ModelAndView selectAvatarPage() {
        ModelAndView mv = new ModelAndView("select_avatar");
        mv.addObject("loggedInUser", userService.getLoggedInUser());
        
        List<String> avatars = new ArrayList<>();
        for (int i = 1; i <= 20; i++) { 
            avatars.add("/avatars/avatar_" + i + ".png");
        }

        mv.addObject("avatars", avatars);
        return mv;
    }

    @PostMapping("/profile/select-avatar")
    public String updateAvatar(@RequestParam("avatar") String avatarPath) {
        String userId = userService.getLoggedInUser().getUserId();
        try {
            profileImageService.updateProfileImage(userId, avatarPath);
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/profile/select-avatar?error=Failed to update avatar";
        }
        return "redirect:/profile";
    }

}
