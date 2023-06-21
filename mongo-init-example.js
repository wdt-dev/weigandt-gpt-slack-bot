use admin
db.auth('root', 'example')

db.createUser({
    user: 'botuser',
    pwd: 'botuserpassword',
    roles: [
        {
            role: 'readWrite',
            db: 'usersettings',
        },
        {
            role: 'read',
            db: 'admin',
        },
    ],
});
