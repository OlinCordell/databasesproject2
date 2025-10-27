-- Retrieve all comments for a specific post, including user info
-- Url: http://localhost:8081/post/{postId}
SELECT 
    c.commentId, 
    c.content, 
    c.postDate,
    u.userId, 
    u.firstName, 
    u.lastName, 
    u.profileImagePath
FROM comment c
JOIN user u ON c.userId = u.userId
WHERE c.postId = ?
ORDER BY c.postDate ASC;

-- Insert a new comment for a post
-- Url: http://localhost:8081/post/{postId}
INSERT INTO comment (postId, userId, content, postDate)
VALUES (?, ?, ?, NOW());

-- Update comment count for a post
-- Url: http://localhost:8081/post/{postId}
UPDATE post SET commentsCount = commentsCount + 1
WHERE postId = ?;

-- Retrieves the owner of a post
-- Url: http://localhost:8081/post/{postId}
SELECT user FROM post WHERE postId = ?

-- Retrieve all bookmarked posts for a user, including post and author info, and like/bookmark status
-- Url: http://localhost:8081/bookmarks
SELECT 
    p.postId,
    p.content,
    p.postDate,
    p.user AS authorId,
    p.heartsCount,
    p.commentsCount,
    u.userId,
    u.username,
    u.firstName,
    u.lastName,
    u.profileImagePath,
    EXISTS(SELECT 1 FROM like_post WHERE postId = p.postId AND userId = ?) as isHearted,
    EXISTS(SELECT 1 FROM bookmark WHERE postId = p.postId AND userId = ?) as isBookmarked
FROM bookmark b, post p, user u
WHERE p.postId = b.postId
  AND u.userId = p.user
  AND b.userId = ?
ORDER BY p.postDate DESC;

-- Add a bookmark for a post by a user
-- Url: http://localhost:8081/post/{postId}/bookmark
INSERT INTO bookmark (userId, postId) VALUES (?, ?);

-- Remove a bookmark for a post by a user
-- Url: http://localhost:8081/post/{postId}/bookmark
DELETE FROM bookmark WHERE userId = ? AND postId = ?;


