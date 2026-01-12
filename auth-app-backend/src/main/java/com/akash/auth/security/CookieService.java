package com.akash.auth.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
@Getter
@Service
public class CookieService {
    private final String refreshTokenCookieName;
    private final boolean cookieHttpOnly;
    private final boolean cookieSecure;
    private final String cookieDomain;
    private final String cookieSameSite;

    private  final Logger logger = org.slf4j.LoggerFactory.getLogger(CookieService.class);

    public CookieService(

            @Value("${security.jwt.refresh-token-cookie-name}") String refreshTokenCookieName,
            @Value("${security.jwt.cookie-http-only}") boolean cookieHttpOnly,
            @Value("${security.jwt.cookie-secure}") boolean cookieSecure,

            @Value("${security.jwt.cookie-same-site}") String cookieSameSite,
            @Value("${security.jwt.cookie-domain}") String cookieDomain
    ) {
        this.refreshTokenCookieName = refreshTokenCookieName;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieSecure = cookieSecure;
        this.cookieDomain = cookieDomain;
        this.cookieSameSite = cookieSameSite;
    }
    //create method to attach cookie to response.
    public void attachRefreshCookie(HttpServletResponse response, String value, int maxAge) {

        logger.info("Attaching cookie with name: {} and value: {}", refreshTokenCookieName, value);
        var responseCookieBuilder = ResponseCookie.from(refreshTokenCookieName, value)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(cookieSameSite);

        if(cookieDomain!=null && !cookieDomain.isBlank())
        {
            responseCookieBuilder.domain(cookieDomain);

        }
        ResponseCookie responseCookie = responseCookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

    }

    // Clear refresh cookie

    public void clearRefreshCookie(HttpServletResponse response) {
        var builder = ResponseCookie.from(refreshTokenCookieName, "")
                .maxAge(0)
                .httpOnly(cookieHttpOnly)
                .path("/")
                .sameSite(cookieSameSite)
                .secure(cookieSecure);

        if(cookieDomain!=null && !cookieDomain.isBlank())
        {
            builder.domain(cookieDomain);

        }

        ResponseCookie responseCookie = builder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

    }

    public void addNoStoreHeaders(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.setHeader("Pragma", "no-cache");
    }
}
/*
CookieService
No refactor, no extra theory.

📍 WHERE THIS CLASS LIVES
security/CookieService


Layer: Security / Infrastructure layer

This class is NOT controller, NOT JWT logic, NOT DB.

🧠 WHAT YOU ARE DOING (ROLE OF CookieService)
🔐 You are centralizing ALL cookie-related security logic

Meaning:

Controllers do not create cookies

Controllers do not know cookie flags

Controllers just say:

cookieService.attachRefreshCookie(...)


✔️ This is clean architecture

🧩 FIELD BY FIELD — WHAT EACH THING DOES
private final String refreshTokenCookieName;


➡️ Cookie name (example: refresh_token)

private final boolean cookieHttpOnly;


➡️ Prevents JavaScript access
➡️ Protects against XSS

private final boolean cookieSecure;


➡️ Cookie sent only over HTTPS

private final String cookieDomain;


➡️ Share cookie across subdomains
(example: .example.com)

private final String cookieSameSite;


➡️ CSRF protection
Values: Strict, Lax, None

🔧 WHERE THESE VALUES COME FROM
security.jwt.refresh-token-cookie-name=refresh_token
security.jwt.cookie-http-only=true
security.jwt.cookie-secure=true
security.jwt.cookie-same-site=None
security.jwt.cookie-domain=example.com


📌 You externalized security config → VERY GOOD

🧠 METHOD BY METHOD — WHAT YOU ARE DOING
1️⃣ attachRefreshCookie(...)

📌 Used in

/login

/refresh

What you do step-by-step:
ResponseCookie.from(name, value)


➡️ Create HTTP response cookie (Spring way)

.httpOnly(cookieHttpOnly)


➡️ Blocks JS access

.secure(cookieSecure)


➡️ HTTPS only

.sameSite(cookieSameSite)


➡️ CSRF control

.maxAge(maxAge)


➡️ Refresh token lifetime

response.addHeader("Set-Cookie", cookie)


➡️ Send cookie to browser

📌 Correct way (NOT javax.servlet.Cookie)

2️⃣ clearRefreshCookie(...)

📌 Used in

/logout

What you do:
.maxAge(0)


➡️ Tells browser to delete cookie

value = ""


➡️ Clears stored token

sameSite + secure + domain


➡️ Must MATCH original cookie
(otherwise browser won’t delete it)

✔️ You did this correctly

3️⃣ addNoStoreHeaders(...)

📌 Used in

/login

/refresh

/logout

What you do:
Cache-Control: no-store
Pragma: no-cache


➡️ Prevents:

Browser caching tokens

Proxy caching responses

🔐 Important for auth responses

🧠 WHY THIS CLASS IS IMPORTANT

Without CookieService:

❌ Cookie logic duplicated in controller
❌ Security flags easy to forget
❌ Hard to change cookie behavior

With CookieService:

✅ One place to control security
✅ Easy environment-based config
✅ Cleaner controller code

🟢 VERDICT (HONEST)

✔️ You did this RIGHT
✔️ This is professional-level
✔️ Many devs get cookie handling wrong — you didn’t

🧩 WHAT THIS CLASS DEPENDS ON

HttpServletResponse

Spring ResponseCookie

Application properties

❌ No DB
❌ No JWT parsing
❌ No controller logic

🧭 FINAL ONE-LINER

CookieService exists so your authentication cookies are secure, consistent, and controlled from one place — not scattered across controllers.
 */