package com.shopflow.user.exceptions;

public class UserNotFoundException extends RuntimeException {

    private final Long userId;

    public UserNotFoundException(Long userId) {
        super("User %d does not exist".formatted(userId));
        this.userId = userId;
    }
}
