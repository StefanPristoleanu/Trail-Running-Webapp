package apiserver.security;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

/** @author */
public class AuthenticationFilter extends GenericFilterBean {

  private static final Logger LOG = Logger.getLogger(AuthenticationFilter.class.getName());

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    // System.out.println(".... AuthenticationFilter - doFilter ... " + ((HttpServletResponse)
    // response).getStatus());
    Authentication authentication = null;
    if (((HttpServletResponse) response).getStatus() <= 200) {
      authentication = JWTAuthService.getAuthentication((HttpServletRequest) request);
      final String expiredMsg = (String) request.getAttribute("expired");
      if (expiredMsg != null) {
        ((HttpServletResponse) response).addHeader("UNAUTHORIZED", expiredMsg);
        // ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
      }
    }
    if (authentication != null) {
      LOG.info(
          "~~~~~ AuthenticationFilter - authentication "
              + authentication.getName()
              + " - Authorities: "
              + authentication.getAuthorities().toString());
    }
    SecurityContextHolder.getContext().setAuthentication(authentication);
    filterChain.doFilter(request, response);
  }
}
