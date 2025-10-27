package uga.menik.csx370.controllers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import uga.menik.csx370.services.ProfileImageService;
import uga.menik.csx370.services.UserService;
import uga.menik.csx370.models.User;

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
        ModelAndView mv = new ModelAndView("select-avatar");
        mv.addObject("loggedInUser", userService.getLoggedInUser());
        
        List<String> avatars = new ArrayList<>();
        for (int i = 1; i <= 20; i++) { 
            avatars.add("/avatars/avatar_" + i + ".png");
        }

        mv.addObject("avatars", avatars);
        return mv;
    }

    @PostMapping("/profile/select-avatar")
    public String updateAvatar(
        @RequestParam(name = "avatar", required = false) String avatarPath,
        @RequestParam(name = "uploadedAvatar", required = false) MultipartFile uploadedFile) {

        String userId = userService.getLoggedInUser().getUserId();

        try {
            String finalPath = null;

            if (uploadedFile != null && !uploadedFile.isEmpty()) {
                String projectRoot = System.getProperty("user.dir");
                Path uploadDir = Paths.get(projectRoot, "avatars");
                Files.createDirectories(uploadDir);

                String filename = UUID.randomUUID().toString() + "_" + uploadedFile.getOriginalFilename();
                Path targetPath = uploadDir.resolve(filename);
                uploadedFile.transferTo(targetPath.toFile());

                finalPath = "/avatars/" + filename;
            } else if (avatarPath != null && !avatarPath.isEmpty()) {
                finalPath = avatarPath;
            } else {
                return "redirect:/profile/select-avatar?error=No avatar selected";
            }

            profileImageService.updateProfileImage(userId, finalPath);

            User user = userService.getLoggedInUser();
            userService.setLoggedInUser(
                new User(user.getUserId(), user.getFirstName(), user.getLastName(), finalPath)
            );

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/profile/select-avatar?error=Failed to update avatar";
        }

        return "redirect:/profile";
    }

}

