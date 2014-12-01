package com.unkur.affnetui.controllers;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.log4j.Logger;

import com.unkur.affnetui.config.AppConfig;
import com.unkur.affnetui.config.Config;
import com.unkur.affnetui.config.HibernateUtil;
import com.unkur.affnetui.config.Urls;
import com.unkur.affnetui.controllers.userui.CheckSignUpController;

/**
 * Application Lifecycle Listener implementation class AppInitListener
 *
 */
@WebListener
public class AppInitListener implements ServletContextListener {

    /**
     * Default constructor. 
     */
    public AppInitListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * Initialize application here.
     */
    public void contextInitialized(ServletContextEvent event)  { 
    	System.out.println("Context initialization...");
    	//Locate and read properties
    	AppConfig.getInstance();
    	//Init status end point
    	StatusEndpoint.init(System.currentTimeMillis());
    	//Start Hibernate
    	HibernateUtil.getCurrentSession();
    	Logger logger = Logger.getLogger(this.getClass().getName());
    	logger.debug("Initialization OK.");
    	System.out.println("Initialized");

    	//Set all application-wide attributes
    		//links that are used on most pages
    	ServletContext context = event.getServletContext();
    	context.setAttribute("logoutPage", Urls.LOGOUT_PAGE_URL);
    	context.setAttribute("dashPage", Urls.USER_DASHBOARD_PAGE_URL);
    	context.setAttribute("updateProfilePage", Urls.UPDATE_USER_PROFILE_PAGE_URL);
    	context.setAttribute("userUploadPage", Urls.USER_UPLOAD_PAGE_URL);
    	context.setAttribute("signInPage", Urls.SIGNIN_PAGE_URL);
    		//Language bundle name for i18n
    	context.setAttribute("bundleBasename", Config.BUNDLE_BASENAME);
         
    }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent arg0)  { 
         // TODO Auto-generated method stub
    }
	
}
