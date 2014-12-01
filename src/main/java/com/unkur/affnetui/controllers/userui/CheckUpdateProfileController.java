package com.unkur.affnetui.controllers.userui;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.google.common.base.Throwables;
import com.unkur.affnetui.config.HibernateUtil;
import com.unkur.affnetui.config.Links;
import com.unkur.affnetui.config.Urls;
import com.unkur.affnetui.controllers.StatusEndpoint;
import com.unkur.affnetui.dao.impl.ShopDaoImpl;
import com.unkur.affnetui.dao.impl.UserDaoImpl;
import com.unkur.affnetui.entity.Language;
import com.unkur.affnetui.entity.Shop;
import com.unkur.affnetui.entity.ShopSource;
import com.unkur.affnetui.entity.SupportedFileFormat;
import com.unkur.affnetui.entity.User;

/**
 * Servlet implementation class CheckUpdateProfileController
 */
@WebServlet("/user/checkUpdateProfile")
public class CheckUpdateProfileController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private static Logger logger = Logger.getLogger(CheckUpdateProfileController.class.getName());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CheckUpdateProfileController() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//get session and user object (this request has passed Auth filter)
		HttpSession httpSession = (HttpSession) request.getSession();
		User oldUser = (User) httpSession.getAttribute(Links.SESSION_USER_ATTR_NAME);
		
		if( !validateParameters(request) ) {
			response.sendRedirect(Urls.UPDATE_USER_PROFILE_PAGE_URL + Links.createQueryString(Links.ERROR_PARAM_NAME));
			return;
		}
		
		boolean resourceAvailable = false;
		String resourceAvailableStr = request.getParameter(Links.SHOP_RESOURCE_AVAILABLE_PARAM_NAME);
		if(resourceAvailableStr != null && resourceAvailableStr.equals("ON")) {
			resourceAvailable = true;
		}
		String resourceFileFormat = request.getParameter(Links.SHOP_RESOURCE_FILE_FORMAT_PARAM_NAME);
		String resourceDownloadUrl = request.getParameter(Links.SHOP_RESOURCE_URL_PARAM_NAME);
		boolean resourceAuthRequired = false;
		String resourceAuthRequiredStr = request.getParameter(Links.SHOP_RESOURCE_AUTH_REQUIRED_PARAM_NAME);
		if(resourceAuthRequiredStr != null && resourceAuthRequiredStr.equals("ON")) {
			resourceAuthRequired = true;
		}
		String resourceAuthUsername = request.getParameter(Links.SHOP_RESOURCE_AUTH_USERNAME_PARAM_NAME);
		String resourceAuthPassword = request.getParameter(Links.SHOP_RESOURCE_AUTH_PASSWORD_PARAM_NAME);
		
		Session s = HibernateUtil.getCurrentSession();
		Transaction Tx = s.beginTransaction();
		String redirectIfDuplicateUrl = Urls.UPDATE_USER_PROFILE_PAGE_URL + Links.createQueryString(Links.ERROR_PARAM_NAME);
		User freshUser;
		Shop freshShop;
		try {
			Shop oldShop;
			try {
				oldShop = new ShopDaoImpl().selectById(oldUser.getShopId());
			} catch (Exception e1) {
				StatusEndpoint.incrementErrors();
				logger.debug("Unable to get Shop object associated with given User " + e1);
				response.sendRedirect(Urls.ERROR_PAGE_URL);
				throw e1;
			}

			freshUser = oldUser.clone(); //old User is stored in session, so don't change it, unless changes are saved to DB
			freshShop = oldShop;
			freshShop.setName(request.getParameter(Links.SHOP_NAME_PARAM_NAME));
			freshShop.setUrl(request.getParameter(Links.SHOP_URL_PARAM_NAME));

			freshUser.setEmail(request.getParameter(Links.EMAIL_PARAM_NAME));
			freshUser.setPassword(request.getParameter(Links.PASSWORD_PARAM_NAME));
			freshUser.setFirstName(request.getParameter(Links.FIRST_NAME_PARAM_NAME));
			freshUser.setLastName(request.getParameter(Links.LAST_NAME_PARAM_NAME));
			
			Query q = s.createQuery("FROM Language l WHERE l.code = ?");
			q.setString(0, request.getParameter(Links.LANGUAGE_PARAM_NAME));
			Language l = (Language) q.uniqueResult();
			
			freshUser.setLanguage(l);
			
			if(resourceAvailable) {
				SupportedFileFormat fileFormat = (SupportedFileFormat) s.createQuery("FROM SupportedFileFormat s WHERE s.extension = \'" + resourceFileFormat + "\'").uniqueResult();
				ShopSource source = (ShopSource) s.createQuery("FROM ShopSource s WHERE s.shop_id = " + freshShop.getId()).uniqueResult();
				if(source == null) {
					source = new ShopSource();
				}
				source.setShop_id(freshShop.getId());
				source.setFile_format(fileFormat);
				source.setDownload_url(resourceDownloadUrl);
				source.setBasic_http_auth_required(resourceAuthRequired);
				source.setBasic_http_auth_username(resourceAuthUsername);
				source.setBasic_http_auth_password(resourceAuthPassword);
				source.setIs_active(true);
				source.setLast_queried_at(0);
				
				s.saveOrUpdate(source);
			}
			
			ShopSource source = (ShopSource) s.createQuery("FROM ShopSource s WHERE s.shop_id = " + freshShop.getId()).uniqueResult();
			request.setAttribute("sourceObject", source);
		
			try {
				new ShopDaoImpl().updateShop(freshShop);
			} catch(Exception e) {
				redirectIfDuplicateUrl = Urls.UPDATE_USER_PROFILE_PAGE_URL + Links.createQueryString(Links.DUPLICATE_SHOP_PARAM_NAME);
				throw e;
			}
			
			try {
				new UserDaoImpl().updateUser(freshUser);
			} catch(Exception e) {
				redirectIfDuplicateUrl = Urls.UPDATE_USER_PROFILE_PAGE_URL + Links.createQueryString(Links.DUPLICATE_USER_PARAM_NAME);
				throw e;
			}
			
			//register OK, update user in session attributes
			httpSession.removeAttribute(Links.SESSION_USER_ATTR_NAME);
			httpSession.setAttribute(Links.SESSION_USER_ATTR_NAME, freshUser); // update user in session
			Tx.commit();
			logger.info("Profile updated successfully for user email=\"" + freshUser.getEmail() + "\"");
		} catch (Exception e) {
			logger.debug("Profile update failure: " + Throwables.getStackTraceAsString(e));
			Tx.rollback();
			response.sendRedirect(redirectIfDuplicateUrl);
			return;
		} 
		

		//create OK page
		request.setAttribute("userObject", freshUser);
		request.setAttribute("shopObject", freshShop);
		
		request.getRequestDispatcher(Links.UPDATE_PROFILE_SUCCESS_JSP).forward(request, response);
		
	}
	
	private boolean validateParameters(HttpServletRequest request) {
		
		String resourceAvailable = request.getParameter(Links.SHOP_RESOURCE_AVAILABLE_PARAM_NAME);
		String resourceFileFormat = request.getParameter(Links.SHOP_RESOURCE_FILE_FORMAT_PARAM_NAME);
		String resourceDownloadUrl = request.getParameter(Links.SHOP_RESOURCE_URL_PARAM_NAME);
		String resourceAuthRequired = request.getParameter(Links.SHOP_RESOURCE_AUTH_REQUIRED_PARAM_NAME);
		String resourceAuthUsername = request.getParameter(Links.SHOP_RESOURCE_AUTH_USERNAME_PARAM_NAME);
		String resourceAuthPassword = request.getParameter(Links.SHOP_RESOURCE_AUTH_PASSWORD_PARAM_NAME);
		
		if(resourceAvailable != null && resourceAvailable.equals("ON")) {
			if(resourceDownloadUrl == null || resourceFileFormat == null || 
					resourceDownloadUrl.isEmpty() || resourceFileFormat.isEmpty()) {
				return false;
			}
			
			
			if(resourceAuthRequired != null && resourceAuthRequired.equals("ON")) {
				if(resourceAuthUsername == null || resourceAuthPassword == null ||
						resourceAuthUsername.isEmpty() || resourceAuthPassword.isEmpty()) {
					return false;
				}
			}
		}
		
		return true;
	}
	
}
