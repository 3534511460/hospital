package com.hospital.appointment.security;

public class UserContext {
    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> usernameHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> roleHolder = new ThreadLocal<>();
    private static final ThreadLocal<String> tokenHolder = new ThreadLocal<>();
    private static final ThreadLocal<Long> departmentIdHolder = new ThreadLocal<>();

    public static void setUserId(Long userId) { userIdHolder.set(userId); }
    public static Long getUserId() { return userIdHolder.get(); }

    public static void setUsername(String username) { usernameHolder.set(username); }
    public static String getUsername() { return usernameHolder.get(); }

    public static void setRole(String role) { roleHolder.set(role); }
    public static String getRole() { return roleHolder.get(); }

    public static void setToken(String token) { tokenHolder.set(token); }
    public static String getToken() { return tokenHolder.get(); }

    public static void setDepartmentId(Long departmentId) { departmentIdHolder.set(departmentId); }
    public static Long getDepartmentId() { return departmentIdHolder.get(); }

    public static void clear() {
        userIdHolder.remove();
        usernameHolder.remove();
        roleHolder.remove();
        tokenHolder.remove();
        departmentIdHolder.remove();
    }
}
