package idlezoo.security;

import static org.junit.Assert.*;

import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import idlezoo.security.UsersService;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SecurityTests {

  @Autowired
  private UsersService usersService;

  @LocalServerPort
  private int port;
  @Autowired
  private TestRestTemplate template;

  @Test
  public void homePageLoads() {
    ResponseEntity<String> response = template.getForEntity("/", String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
  }

  @Test
  public void userEndpoint() {
    ResponseEntity<String> response = template.getForEntity("/user", String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  public void register() {
    assertFalse(userExists("testregister"));
    loginOrRegister("testregister", "/createuser");
    assertEquals("testregister", usersService.loadUserByUsername("testregister").getUsername());
  }

  private boolean userExists(String username) {
    try {
      usersService.loadUserByUsername(username);
      return true;
    } catch (UsernameNotFoundException notFound) {
      return false;
    }
  }

  @Test
  public void login() {
    usersService.addUser("testuser", "testuser");
    ResponseEntity<Void> location = loginOrRegister("testuser", "/login");
    assertEquals("http://localhost:" + port + "/", location.getHeaders().getFirst("Location"));
  }

  private ResponseEntity<Void> loginOrRegister(String user, String uri) {
    ResponseEntity<String> response = template.getForEntity("/resource", String.class);
    String csrf = getCsrf(response.getHeaders());
    MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
    form.set("username", user);
    form.set("password", user);
    HttpHeaders headers = new HttpHeaders();
    headers.set("X-XSRF-TOKEN", csrf);
    headers.put(HttpHeaders.COOKIE, response.getHeaders().get("Set-Cookie"));
    RequestEntity<MultiValueMap<String, String>> request = new RequestEntity<>(
        form, headers, HttpMethod.POST, URI.create("http://localhost:" + port + uri));
    return template.exchange(request, Void.class);
  }

  @Test
  public void twoLogins() {
    // if UserDetails with mutable password is used for storage - password
    // gets erased in login process and second login for the same user is
    // impossible
    login();
    login();
  }

  private String getCsrf(HttpHeaders headers) {
    for (String header : headers.get("Set-Cookie")) {
      List<HttpCookie> cookies = HttpCookie.parse(header);
      for (HttpCookie cookie : cookies) {
        if ("XSRF-TOKEN".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }

}