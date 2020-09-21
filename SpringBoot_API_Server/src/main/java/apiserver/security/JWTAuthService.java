package apiserver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/** @author */
public class JWTAuthService {

  private static final Logger LOG = Logger.getLogger(JWTAuthService.class.getName());
  static final long EXPIRATIONTIME = Integer.MAX_VALUE; // TODO 5 minutes = 300_000

  static final String SECRET_KEY = "my_secret_key_for_JWT";

  static final String TOKEN_PREFIX = "Bearer";
  static final String HEADER_STRING = "Authorization";

  public static void addAuthentication(
      HttpServletResponse res, String username, String role, Long userId) {
    res.addHeader(
        HEADER_STRING, TOKEN_PREFIX + " " + generateAuthentication(username, role, userId));
  }

  public static String generateAuthentication(String username, String role, Long userId) {

    Claims claims = Jwts.claims().setSubject(username);
    claims.put("role", role);
    claims.put("userId", userId + "");
    String jwt =
        Jwts.builder()
            // .setSubject(username)
            .setClaims(claims)
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
            .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
            .compact();
    return TOKEN_PREFIX + " " + jwt;
  }

  public static Authentication getAuthentication(HttpServletRequest request) {
    // System.out.println("~~~~~ getAuthentication: " + request.getServletPath());
    String token = request.getHeader(HEADER_STRING);
    if (token != null) {
      // parse the token.
      String username = null;
      Set<GrantedAuthority> grantedAuthorities = new LinkedHashSet<>();
      try {
        Claims claims =
            Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token.replace(TOKEN_PREFIX, ""))
                .getBody();
        username = claims.getSubject();
        String role = (String) claims.get("role");
        grantedAuthorities.add(new SimpleGrantedAuthority(role));
        String userId = (String) claims.get("userId");
        grantedAuthorities.add(new SimpleGrantedAuthority(userId));
        // System.out.println("~~~~ claims: " + username + " - " + role + " " + userId);
      } catch (ExpiredJwtException ex) {
        request.setAttribute("expired", ex.getMessage());
      } catch (Exception ex) {
        LOG.info("getAuthentication: " + ex.toString());
      }
      return username != null
          ? new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities)
          : null;
    }
    return null;
  }
}
