package idlezoo.security;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SecurityTests {

  private static final String ORIGIN = "http://localhost:9000";

  @Autowired
  private UsersService usersService;

  @LocalServerPort
  private int port;
  @Autowired
  private TestRestTemplate template;

  @Test
  public void homePageRedirects() {
    ResponseEntity<String> response = template.getForEntity("/", String.class);
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertEquals("https://idlezoo.github.io", response.getHeaders().getFirst(HttpHeaders.LOCATION));
  }

  @Test
  public void userEndpoint() {
    ResponseEntity<String> response = template.getForEntity("/user", String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  public void cors() throws Exception {
    user();
  }

  private <T> void assertCors(ResponseEntity<T> response) {
    assertEquals(true, response.getHeaders().getAccessControlAllowCredentials());
    assertEquals(ORIGIN, response.getHeaders().getAccessControlAllowOrigin());
  }

  @Test
  public void register() throws Exception {
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
  public void login() throws Exception {
    usersService.addUser("testuser", "testuser");
    ResponseEntity<Void> login = loginOrRegister("testuser", "/login");
    assertEquals("http://localhost:" + port + "/",
        login.getHeaders().getFirst(HttpHeaders.LOCATION));
    ResponseEntity<String> user =
        user(login.getHeaders().get(HttpHeaders.SET_COOKIE).toArray(new String[0]));

    assertTrue(user.getBody().contains("testuser"));
  }

  private ResponseEntity<String> user(String... coockies) throws URISyntaxException {
    RequestEntity<Void> request =
        RequestEntity.get(new URI("http://localhost:" + port + "/user"))
            .header(HttpHeaders.ORIGIN, ORIGIN)
            .header(HttpHeaders.COOKIE, coockies)
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
//    String csrf = getCsrf(response.getHeaders());
    MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
    form.set("username", user);
    form.set("password", user);
    HttpHeaders headers = new HttpHeaders();
//    headers.set("X-XSRF-TOKEN", csrf);
//    headers.put(HttpHeaders.COOKIE, response.getHeaders().get("Set-Cookie"));
    headers.put(HttpHeaders.ORIGIN, asList(ORIGIN));
    RequestEntity<MultiValueMap<String, String>> loginOrRegister = new RequestEntity<>(
        form, headers, HttpMethod.POST, URI.create("http://localhost:" + port + uri));
    ResponseEntity<Void> result = template.exchange(loginOrRegister, Void.class);
    assertCors(result);
    return result;
  }

  @Test
  public void twoLogins() throws Exception {
    // if UserDetails with mutable password is used for storage - password
    // gets erased in login process and second login for the same user is
    // impossible
    login();
    login();
  }

//  private static String getCsrf(HttpHeaders headers) {
//    for (String header : headers.get("Set-Cookie")) {
//      List<HttpCookie> cookies = HttpCookie.parse(header);
//      for (HttpCookie cookie : cookies) {
//        if ("XSRF-TOKEN".equals(cookie.getName())) {
//          return cookie.getValue();
//        }
//      }
//    }
//    return null;
//  }

}
