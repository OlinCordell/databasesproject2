
-----------------------
  DB SETUP
----------------------

-- Create the database.
create database if not exists csx370_mb_platform;

-- Use the created database.
use csx370_mb_platform;

-- Create the user table.
create table if not exists user (
    userId int auto_increment,
    username varchar(255) not null,
    password varchar(255) not null,
    firstName varchar(255) not null,
    lastName varchar(255) not null,
    lastActiveDate datetime,
    profileImagePath varchar(255) default '/avatars/avatar_1.png',
    primary key (userId),
    unique (username),
    constraint username_min_length check (char_length(trim(username)) >= 2),
    constraint firstName_min_length check (char_length(trim(firstName)) >= 2),
    constraint lastName_min_length check (char_length(trim(lastName)) >= 2)
);

-- Create the post table
create table if not exists post (
    postId varchar(255),
    content varchar(255) not null,
    postDate datetime not null,
    user int not null,
    heartsCount int not null default 0,
    commentsCount int not null default 0,
    isHearted boolean not null default FALSE,
    isBookmarked boolean not null default FALSE,
    primary key (postId),
    foreign key (`user`) references `user`(userId) on delete cascade
);

-- Create the comment table
create table if not exists comment (
    commentId int auto_increment,
    postId varchar(255) not null,
    content varchar(255) not null,
    postDate datetime not null,
    userId int not null,
    primary key (commentId),
    foreign key (postId) references post(postId) on delete cascade,
    foreign key (userId) references user(userId) on delete cascade
);

-- Create the follows table
create table if not exists follows (
    followsId int not null,
    followedId int not null,
    primary key (followsId, followedId),
    foreign key (followsId) references user(userId) on delete cascade,
    foreign key (followedId) references user(userId) on delete cascade
);

-- Create the hashtag table
CREATE TABLE IF NOT EXISTS hashtag (
    hashtagId int auto_increment,
    tag varchar(100) not null,
    primary key (hashtagId),
    unique (tag)
);

-- Create the hashtag_post table (link hashtag to posts table)
CREATE TABLE IF NOT EXISTS hashtag_post (
    postId varchar(255) not null,
    hashtagId int not null,
    primary key (postId, hashtagId),
    foreign key (postId) references post(postId) on delete cascade,
    foreign key (hashtagId) references hashtag(hashtagId) on delete cascade
);

-- Create the like_post table (to track which user liked which post)
CREATE TABLE IF NOT EXISTS like_post (
    userId int not null,
    postId varchar(255) not null,
    primary key (userId, postId),
    foreign key (userId) references user(userId) on delete cascade,
    foreign key (postId) references post(postId) on delete cascade
);

-- Create the like_post table (to track which user liked which post)
CREATE TABLE IF NOT EXISTS bookmark (
    userId int not null,
    postId varchar(255) not null,
    createdAt datetime not null default current_timestamp,
    primary key (userId, postId),
    foreign key (userId) references user(userId) on delete cascade,
    foreign key (postId) references post(postId) on delete cascade
);

-- Create the notification table (to track user notifications)
CREATE TABLE IF NOT EXISTS notification (
    notificationId INT AUTO_INCREMENT,
    userId INT NOT NULL,
    actorId INT NOT NULL,
    type ENUM('LIKE', 'COMMENT', 'FOLLOW', 'BOOKMARK') NOT NULL,
    postId VARCHAR(255) DEFAULT NULL,
    message VARCHAR(255) NOT NULL,
    isRead BOOLEAN DEFAULT FALSE,
    createdAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (notificationId),
    FOREIGN KEY (userId) REFERENCES user(userId) ON DELETE CASCADE,
    FOREIGN KEY (actorId) REFERENCES user(userId) ON DELETE CASCADE,
    FOREIGN KEY (postId) REFERENCES post(postId) ON DELETE CASCADE
);

-------------------------
SAMPLE DATA INSERTIONS 
-------------------------

insert into user(userId,username,password,firstName,lastName,lastActiveDate,profileImagePath) values
(1, 'harryp',  'pw_1','Harry','Potter','2025-03-07 22:54:00'),
(2, 'hermioneg', 'pw_2','Hermione','Granger','2025-03-08 09:15:00'),
(3, 'ronw',     'pw_3',    'Ron',    'Weasley', '2025-03-08 11:30:00'),
(4, 'dracom', 'pw_4',    'Draco',  'Malfoy',  '2025-03-06 18:20:00'),
(5, 'lunal', 'pw_5',      'Luna',   'Lovegood','2025-03-05 13:05:00');

insert into post(postId,content, postDate, user, heartsCount,commentsCount,isHearted,isBookmarked) values
('p001','FDOC!! #gryffindor', '2025-03-07 22:54:00', 1, 2, 1, false, false),
('p002', 'Need to study, so behind! #magic','2025-03-08 08:05:00', 2, 1, 2, false, false),
('p003', 'I lost my wand again #hufflepuff','2025-03-08 10:45:00', 3, 2, 1, false, false),
('p004', 'Beat that Potter! #slytherin','2025-03-06 17:10:00', 4, 1, 1, false, false),
('p005', 'Magic in the air #ravenclaw','2025-03-05 12:00:00',5, 0, 0, false, false);

insert into follows(followsId,followedId) values
(1, 2), -- harry to hermione
(1, 3), -- harry to ron
(2, 1), -- hermione to harry
(3, 4), -- ron to draco
(4, 5); -- draco to luna 

insert into hashtag(hashtagId,tag) values
(1, 'gryffindor'),
(2, 'slytherin'),
(3, 'hufflepuff'),
(4, 'ravenclaw'),
(5, 'magic');

insert into hashtag_post (postId, hashtagId) values
('p001', 1),
('p002', 5),
('p003', 3),
('p004', 2),
('p005', 4);

insert into like_post(userId,postId) values
(2, 'p001'),
(3, 'p001'),
(1, 'p002'),
(1, 'p003'),
(2, 'p005');

insert into bookmark(userId,postId,createdAt) values
(1, 'p005', '2025-03-08 08:30:00'),
(2, 'p004', '2025-03-08 08:30:00'),
(3, 'p003', '2025-03-08 08:30:00'),
(4, 'p002', '2025-03-08 08:30:00'),
(5, 'p001', '2025-03-08 08:30:00');

insert into notification(notificationId,userId,actorId,type, postId,message, isRead,createdAt) values
(1, 1, 2, 'LIKE','p001','Hermione liked your post.',false, '2025-03-08 08:21:00'),
(2, 1, 3, 'LIKE','p001','Ron liked your post.',false, '2025-03-08 08:21:00'),
(3,2, 1, 'COMMENT','p002','Harry commented on your post.',false, '2025-03-08 08:21:00'),
(4,5, 4, 'COMMENT','p004','Draco commented on you post.',false, '2025-03-08 08:21:00'),
(5,5, 4, 'BOOKMARK','p005','Draco bookmarked your post.',false, '2025-03-08 08:21:00');





