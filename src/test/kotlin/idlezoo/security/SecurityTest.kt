package idlezoo.security

import com.nhaarman.mockito_kotlin.whenever
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.*
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.net.URI

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SecurityTest(@Autowired val passwordEncoder: PasswordEncoder,
                   @Autowired val template: TestRestTemplate
) {

    @MockBean
    private lateinit var usersService: UsersService

    @Test
    fun homePageRedirects() {
        val response = template.getForEntity("/", String::class.java)
        assertEquals(HttpStatus.FOUND, response.statusCode)
        assertEquals("https://idlezoo.github.io", response.headers.getFirst(HttpHeaders.LOCATION))
    }

    @Test
    fun userEndpoint() {
        val response = template.getForEntity("/user", String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNull(response.body)
    }

    @Test
    fun cors() {
        user()
    }

    private fun <T> assertCors(response: ResponseEntity<T>) {
        assertTrue(response.headers.accessControlAllowCredentials)
        assertEquals(ORIGIN, response.headers.accessControlAllowOrigin)
    }

    @Test
    fun register() {
        loginOrRegister("testregister", "/createuser")
        verify(usersService).addUser(eq("testregister"), anyString())
    }

    @Test
    fun login() {
        whenever(usersService.loadUserByUsername("testuser"))
                .thenReturn(
                        IdUser(0, "testuser", passwordEncoder.encode("testuser"))
                )
        val login = loginOrRegister("testuser", "/login")
        assertEquals(HttpStatus.OK, login.statusCode)
        val cookies = login.headers[HttpHeaders.SET_COOKIE]
        assertNotNull(cookies)
        val user = user(*cookies!!.toTypedArray())

        assertNotNull(user.body)
        assertTrue(user.body!!.contains("testuser"))
    }

    private fun user(vararg cookies: String): ResponseEntity<String> {
        val request = RequestEntity.get(URI("/user"))
                .header(HttpHeaders.ORIGIN, ORIGIN)
                .header(HttpHeaders.COOKIE, *cookies)
                .build()

        val response = template.exchange(request, String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertCors(response)
        return response
    }

    private fun loginOrRegister(user: String, uri: String): ResponseEntity<Void> {
        val request = RequestEntity.get(URI("/resource"))
                .header(HttpHeaders.ORIGIN, ORIGIN)
                .build()

        val response = template.exchange(request, String::class.java)
        assertCors(response)

        val form = LinkedMultiValueMap<String, String>()
        form.set("username", user)
        form.set("password", user)
        val headers = HttpHeaders()

        headers[HttpHeaders.ORIGIN] = listOf(ORIGIN)
        val loginOrRegister = RequestEntity<MultiValueMap<String, String>>(form, headers, HttpMethod.POST, URI(uri))
        val result = template.exchange(loginOrRegister, Void::class.java)
        assertCors(result)
        return result
    }

    companion object {
        private val ORIGIN = "http://localhost:9000"
    }

}
