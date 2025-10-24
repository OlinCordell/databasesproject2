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
