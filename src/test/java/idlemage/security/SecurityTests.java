package idlemage.security;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SecurityTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate template;

	@Autowired
	private UsersService usersService;

	@Test
	public void homePageLoads() {
		ResponseEntity<String> response = template.getForEntity("http://localhost:" + port + "/", String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void userEndpointProtected() {
		ResponseEntity<String> response = template.getForEntity("http://localhost:" + port + "/user", String.class);
		assertEquals(HttpStatus.FOUND, response.getStatusCode());
	}

	@Test
	public void register() {
		assertNull(usersService.getUser("testregister"));
		loginOrRegister("testregister", "/createuser");
		assertEquals("testregister", usersService.getUser("testregister").getName());
	}

	@Test
	public void login() {
		usersService.addUser("testuser", "testuser");
		ResponseEntity<Void> location = loginOrRegister("testuser", "/login");
		assertEquals("http://localhost:" + port + "/", location.getHeaders().getFirst("Location"));
	}

	private ResponseEntity<Void> loginOrRegister(String user, String uri) {
		ResponseEntity<String> response = template.getForEntity("http://localhost:" + port + "/resource", String.class);
		String csrf = getCsrf(response.getHeaders());
		MultiValueMap<String, String> form = new LinkedMultiValueMap<String, String>();
		form.set("username", user);
		form.set("password", user);
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-XSRF-TOKEN", csrf);
		headers.put("COOKIE", response.getHeaders().get("Set-Cookie"));
		RequestEntity<MultiValueMap<String, String>> request = new RequestEntity<MultiValueMap<String, String>>(
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
