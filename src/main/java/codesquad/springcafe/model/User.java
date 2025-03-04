package codesquad.springcafe.model;

import codesquad.springcafe.dto.user.UserInfoDTO;

public class User {
    private final String userId;
    private final String password;
    private final String name;
    private final String email;

    public User(String userId, String password, String name, String email) {
        this.userId = userId;
        this.password = password;
        this.name = name;
        this.email = email;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public boolean isPasswordCorrect(String password) {
        return this.password.equals(password);
    }

    public UserInfoDTO toDTO(Long order) {
        return new UserInfoDTO(order, userId, name, email);
    }

    public UserInfoDTO toDTO() {
        return new UserInfoDTO(0L, userId, name, email);
    }
}
