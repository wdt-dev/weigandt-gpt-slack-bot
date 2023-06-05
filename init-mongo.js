var admin = db.getSiblingDB("admin");
admin.auth("mongoadmin", "password321");
admin.createUser({
    user: "mongouser",
    pwd: "password333",
    roles: [{
        role: "readWrite",
        db: "usersettings"
    }]
});