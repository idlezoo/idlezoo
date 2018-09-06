package idlezoo.security

import org.springframework.security.core.GrantedAuthority
import java.util.Collections

import org.springframework.security.core.userdetails.User

class IdUser(val id: Int?, username: String, password: String) : User(username, password, emptyList<GrantedAuthority>())
