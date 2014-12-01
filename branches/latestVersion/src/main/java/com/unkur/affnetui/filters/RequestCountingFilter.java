package com.unkur.affnetui.filters;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import com.unkur.affnetui.controllers.StatusEndpoint;

/**
 * Servlet Filter implementation class RequestCountingFilter
 */
@WebFilter("/*")
public class RequestCountingFilter implements Filter {

	private static AtomicLong requestCount = new AtomicLong();
	
    /**
     * Default constructor. 
     */
    public RequestCountingFilter() {
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
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		requestCount.incrementAndGet();
		chain.doFilter(request, response);
	}
	
	public static long getRequestCount() {
		return requestCount.get();
	}

	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig fConfig) throws ServletException {
	}

}
