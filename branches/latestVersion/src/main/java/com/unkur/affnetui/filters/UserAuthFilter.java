package com.unkur.affnetui.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.unkur.affnetui.config.Links;
import com.unkur.affnetui.config.Urls;
import com.unkur.affnetui.entity.Admin;
import com.unkur.affnetui.entity.User;

/**
 * Servlet Filter implementation class UserAuthFilter
 */
@WebFilter(urlPatterns = "/user/*")
public class UserAuthFilter implements Filter {

    /**
     * Default constructor. 
     */
    public UserAuthFilter() {
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Filter#destroy()
	 */
	public void destroy() {
		// TODO Auto-generated method stub
	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		HttpSession session = request.getSession(false);
		if(session == null) {
			response.sendRedirect(Urls.SIGNIN_PAGE_URL);
			return;
		}
		Object obj = session.getAttribute(Links.SESSION_USER_ATTR_NAME);
		if(obj == null) {
			response.sendRedirect(Urls.SIGNIN_PAGE_URL);
			return;
		}
		User user = (User) obj;
		//OK, there's user already logged in, continue
		//if there's a language parameter, update it in session
		String language = request.getParameter(Links.LANGUAGE_PARAM_NAME);
		if(language != null) {
			session.setAttribute("language", language);
		}
		chain.doFilter(request, response);
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

}
