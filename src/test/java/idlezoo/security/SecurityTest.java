package idlezoo.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SecurityTest {
    private static final String ORIGIN = "http://localhost:9000";

    private final PasswordEncoder passwordEncoder;
    private final TestRestTemplate template;
    private final int port;

    @MockBean
    private UsersService usersService;

    @Autowired
    SecurityTest(PasswordEncoder passwordEncoder, @LocalServerPort int port, TestRestTemplate template) {
        this.passwordEncoder = passwordEncoder;
        this.port = port;
        this.template = template;
    }

    @Test
    void homePageRedirects() {
        ResponseEntity<String> response = template.getForEntity("/", String.class);
        assertEquals(HttpStatus.FOUND, response.getStatusCode());
        assertEquals("https://idlezoo.github.io", response.getHeaders().getFirst(HttpHeaders.LOCATION));
    }

    @Test
    void userEndpoint() {
        ResponseEntity<String> response = template.getForEntity("/user", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void cors() throws Exception {
        user();
    }

    private <T> void assertCors(ResponseEntity<T> response) {
        assertTrue(response.getHeaders().getAccessControlAllowCredentials());
        assertEquals(ORIGIN, response.getHeaders().getAccessControlAllowOrigin());
    }

    @Test
    void register() throws Exception {
        loginOrRegister("testregister", "/createuser");
        verify(usersService).addUser(eq("testregister"), anyString());
    }

    @Test
    void login() throws Exception {
        when(usersService.loadUserByUsername("testuser"))
                .thenReturn(
                        new IdUser(0, "testuser", passwordEncoder.encode("testuser"))
                );
        ResponseEntity<Void> login = loginOrRegister("testuser", "/login");
        assertEquals(HttpStatus.OK, login.getStatusCode());
        List<String> cookies = login.getHeaders().get(HttpHeaders.SET_COOKIE);
        assertNotNull(cookies);
        ResponseEntity<String> user =
                user(cookies.toArray(new String[0]));

        assertNotNull(user.getBody());
        assertTrue(user.getBody().contains("testuser"));
    }

    private ResponseEntity<String> user(String... cookies) throws URISyntaxException {
        RequestEntity<Void> request =
                RequestEntity.get(new URI("http://localhost:" + port + "/user"))
                        .header(HttpHeaders.ORIGIN, ORIGIN)
                        .header(HttpHeaders.COOKIE, cookies)
                        .build();

        ResponseEntity<String> response = template.exchange(request, String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertCors(response);
        return response;
    }

    private ResponseEntity<Void> loginOrRegister(String user, String uri) throws URISyntaxException {
        RequestEntity<Void> request =
                RequestEntity.get(new URI("http://localhost:" + port + "/resource"))
                        .header(HttpHeaders.ORIGIN, ORIGIN)
                        .build();

        ResponseEntity<String> response = template.exchange(request, String.class);
        assertCors(response);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.set("username", user);
        form.set("password", user);
        HttpHeaders headers = new HttpHeaders();

        headers.put(HttpHeaders.ORIGIN, singletonList(ORIGIN));
        RequestEntity<MultiValueMap<String, String>> loginOrRegister = new RequestEntity<>(
                form, headers, HttpMethod.POST, URI.create("http://localhost:" + port + uri));
        ResponseEntity<Void> result = template.exchange(loginOrRegister, Void.class);
        assertCors(result);
        return result;
    }

}
