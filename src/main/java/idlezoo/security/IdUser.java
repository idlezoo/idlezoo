package idlezoo.security;

import java.util.Collections;

import org.springframework.security.core.userdetails.User;

public class IdUser extends User {

    private static final long serialVersionUID = 1L;
    private final Integer id;

    public IdUser(Integer id, String username, String password) {
        super(username, password, Collections.emptyList());
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

}
