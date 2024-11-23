package com.armendtahiraga.fileupload;

public class UserSession {
    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User currentUser) {
        UserSession.currentUser = currentUser;
    }

    public static void logout() {
        currentUser = null;
    }
}
