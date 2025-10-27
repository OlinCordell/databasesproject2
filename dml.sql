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

-- Retrieve all hashtags from a user's post
-- Url: http://localhost:8081/hashtagsearch
SELECT p.postId, p.content, p.postDate,
        p.heartsCount, p.commentsCount, p.isHearted, p.isBookmarked,
        u.userId, u.firstName, u.lastName, u.profileImagePath
FROM post p
JOIN user u ON p.user = u.userId
WHERE

        /*
        for (int i = 0; i < hashtags.length; i++) {
            sql.append(" LOWER(p.content) LIKE LOWER(?) ");
            if (i < hashtags.length - 1) sql.append(" OR ");
        }
        */
ORDER BY p.postDate DESC

-- Retrieve followable users
-- Url: http://localhost:8081/people
select u.userId, u.firstName, u.lastName, u.lastActiveDate, u.profileImagePath,
                                case when f.followedId is not null then true else false end as isFollowed
                            from user u
                            left join follows f on u.userId = f.followedId and f.followsId = ?
                            where u.userId <> ?
    
-- Create new follow relation
-- Url: http://localhost:8081/people
INSERT INTO follows (followsId, followedId)
            VALUES (?, ?)


-- Remove follow relation
-- Url: http://localhost:8081/people
DELETE FROM follows
            WHERE followsId = ? AND followedId = ?

-- Populate post data when creating new post
-- Url: http://localhost:8081/home
insert into post (postId, content, postDate, user, heartsCount, commentsCount, isHearted, isBookmarked) 
                values (?, ?, ?, ?, 0, 0, FALSE, FALSE)

-- update user lastActive
update user set lastActiveDate = ? where userId = ?

-- Retrieve all followable users
select p.postId, p.content, p.postDate, p.user,
                    p.heartsCount, p.commentsCount, 
                    u.userId, u.firstName, u.lastName, u.profileImagePath,
                    EXISTS(SELECT 1 FROM like_post WHERE postId = p.postId AND userId = ?) as isHearted,
                    EXISTS(SELECT 1 FROM bookmark WHERE postId = p.postId AND userId = ?) as isBookmarked
                from post p
                join user u on p.user = u.userId
                join follows f on u.userId = f.followedId
                where f.followsId = ?
                order by p.postDate desc

INSERT IGNORE INTO like_post (userId, postId)
            VALUES (?, ?)

DELETE FROM like_post WHERE userId = ? AND postId = ?

UPDATE post
            SET heartsCount = (SELECT COUNT(*) FROM like_post WHERE postId = ?)
            WHERE postId = ?

 -- Retrieve post by their id
select p.postId, p.content, p.postDate, p.user, p.heartsCount, p.commentsCount,
                    u.userId, u.firstName, u.lastName, u.profileImagePath,
                    EXISTS(SELECT 1 FROM like_post WHERE postId = p.postId AND userId = ?) as isHearted,
                    EXISTS(SELECT 1 FROM bookmark WHERE postId = p.postId AND userId = ?) as isBookmarked
                from post p
                join user u on p.user = u.userId
                where p.postId = ?
    
-- Retrieve posts by their id
select p.postId, p.content, p.postDate, p.user, p.heartsCount, p.commentsCount,
                u.userId, u.firstName, u.lastName, u.profileImagePath,   
                EXISTS(SELECT 1 FROM like_post WHERE postId = p.postId AND userId = ?) as isHearted,
                EXISTS(SELECT 1 FROM bookmark WHERE postId = p.postId AND userId = ?) as isBookmarked
            from post p
            join user u on p.user = u.userId
            where p.user = ?
            order by p.postDate desc

-- Retrieve notifications by their id    
SELECT n.notificationId, n.userId, n.actorId, n.type, n.postId, n.message, n.isRead, n.createdAt,
                u.userId AS senderId, u.firstName, u.lastName, u.profileImagePath
            FROM notification n
            JOIN user u ON n.actorId = u.userId
            WHERE n.userId = ?
            ORDER BY n.createdAt DESC

-- Deletes all notifications for a user
DELETE FROM notification
            WHERE userId = ?

-- Deletes a specific notification for a user
DELETE FROM notification WHERE notificationId = ? AND userId = ?
