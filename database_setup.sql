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

