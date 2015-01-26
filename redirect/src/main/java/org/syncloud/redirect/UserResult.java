package org.syncloud.redirect;

import com.google.common.base.Optional;

import org.syncloud.redirect.model.*;

public class UserResult {
    public Optional<RedirectError> error = Optional.absent();
    public Optional<User> user = Optional.absent();

    public UserResult(User user) {
        this.user = Optional.of(user);
    }

    public UserResult(RedirectError error) {
        this.error = Optional.of(error);
    }

    public UserResult(int statusCode, RestUser restUser) {
        if (statusCode == 200)
            user = Optional.of(restUser.data);
        else {
            boolean expected = statusCode == 400 && statusCode == 403;
            error = Optional.of(new RedirectError(expected, restUser));
        }
    }

    public boolean hasError() { return error.isPresent(); }

    public RedirectError error() {
        return error.get();
    }

    public User user() {
        return user.get();
    }

    public static UserResult error(String message) {
        return new UserResult(new RedirectError(message));
    }

    public static UserResult error(String message, Throwable throwable) {
        return new UserResult(new RedirectError(message));
    }

    public static UserResult value(User user) {
        return new UserResult(user);
    }
}