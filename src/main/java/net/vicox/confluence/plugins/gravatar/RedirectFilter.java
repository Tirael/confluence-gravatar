package net.vicox.confluence.plugins.gravatar;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Redirects the profile picture URL to Gravatar.
 *
 * @author Georg Schmidl <georg.schmidl@vicox.net>
 */
public class RedirectFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String md5 = httpRequest.getParameter("md5");
        httpResponse.setHeader("Location", UrlUtil.getGravatarUrlFromMd5(md5));
        httpResponse.setStatus(httpResponse.SC_MOVED_PERMANENTLY);
    }

    @Override
    public void destroy() {

    }
}
