create table User (
    Id int auto_increment primary key,
    Username varchar(200) not null,
    Password varchar(200) not null,
    Role varchar(10) not null
);

create table Contract (
    Id int auto_increment primary key,
    UserId int not null,
    FileName varchar(200) not null,
    File longblob not null,
    ExpDate date not null,
    foreign key (UserId) references User(Id) on delete cascade on update cascade
);

create table "User" (
    "Id" serial primary key,
    "Username" varchar(200) not null,
    "Password" varchar(200) not null,
    "Role" varchar(10) not null
);

create table "Contract" (
    "Id" serial primary key,
    "UserId" int not null,
    "FileName" varchar(200) not null,
    "File" bytea not null,
    "ExpDate" date not null,
    constraint fk_user foreign key ("UserId") references "User"("Id") on delete cascade on update cascade
);