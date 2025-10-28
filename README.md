Group 22:

Olin Cordell (okc05539)
-> Implemented Home Page Logic, add/remove heart functionality, and hashtag search.

-> Designed and implemented select avatar feature that allows users to
  select from 20 default avatars or upload their own avatar icon. It is non-trivial
  as it integrates front-end UI, controller logic, service-layer database operations,
  server-side file management into a smooth, interactive experience. To access this
  feature, simply click on the avatar icon located in the top bar field to the right
  of the hashtag search feature. Upon clicking the icon, the user will be redirected
  to the select avatar page.
  - UI: resources/templates/select-avatar.mustache
  - Controller: Controllers/ProfileImageController.java
  - Service: Services/ProfileImageService.java
  - SQL: database operations with profileImagePath in user field.
  - File Handling: uses Java's Path/Files API + MultipartFile


Sean McGrath (smm94426)
-> Implemented Comments Logic and Bookmarks fixes.

-> Implemented a remove post feature. There is a button on the profile screen which users
can use to remove their own posts.
  - UI: resources\templates\fragments\post.mustache
  - Controller: Controllers/PostController.java - deletePost() method.
  - Service: Services/PostService.java - deletePost() method.
  - SQL: Delete sql query only when postId and user match.
  - Model: models/ExpandedPost.java - IsOwnProfile() method, delete button only shows on your own profile.


Pushya Damania (phd87389)
-> Created ER models and recorded video demonstration

-> Designed and implemented notification system that alerts the user when another user follows,
comments, bookmarks, or likes their content. It is non-trivial as it integrates front-end UI, 
controller logic, and service-layer database operations. To access the feature click on the
notifications tab. After clicking the tab the user will see all the notifications they have
and they can choose to delete specific notifications or delete all of them.
 - UI: src/main/resources/templates/notifications_page.mustache
 -    UI: src/main/resources/templates/fragments/notifications_dropdown.mustache
 - Controller: src/main/java/uga/menik/csx370/controllers/NotificationController.java
 - Service: src/main/java/uga/menik/csx370/services/NotificationService.java
 -    Modified: src/main/java/uga/menik/csx370/services/UserService.java - getUserById() method
 -    Modified: Post, people, bookmark, comment services to call + create a notification on action
 - SQL: Add database operations related to creating and deleting notifications


Meghana Madduri (vm84607)
-> Created hashtag, hashtag_post, like_post, and bookmark tables in databse_setup.sql.  
Bookmark feature 
- Controller: src/main/java/uga/menik/csx370/controllers/PeopleController.java
- Service: src/main/java/uga/menik/csx370/service/BookmarkService.java
- Data insertions for all tables in database_setup.sql

Shafat Hasan (smh18904)
-> Implemented Logic work and people page and follow/unfollow feature with Last Active display also worked on some listing issues.
Debugging and code integration. 

- UI: resources/templates/people_page.mustache; resources/templates/fragments/followable_user.mustache
- Controller: src/main/java/uga/menik/csx370/controllers/PeopleController.java
- Service: src/main/java/uga/menik/csx370/services/PeopleService.java
- SQL: SELECT with LEFT JOIN on follows; INSERT/DELETE for follow/unfollow; uses user.lastActiveDate for display
