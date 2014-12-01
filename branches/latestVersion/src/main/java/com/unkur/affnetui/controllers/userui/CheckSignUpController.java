package com.unkur.affnetui.controllers.userui;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.google.common.base.Throwables;
import com.unkur.affnetui.config.Config;
import com.unkur.affnetui.config.HibernateUtil;
import com.unkur.affnetui.config.Links;
import com.unkur.affnetui.config.Urls;
import com.unkur.affnetui.controllers.StatusEndpoint;
import com.unkur.affnetui.dao.exceptions.DbAccessException;
import com.unkur.affnetui.dao.exceptions.UniqueConstraintViolationException;
import com.unkur.affnetui.dao.impl.ShopDaoImpl;
import com.unkur.affnetui.dao.impl.UserDaoImpl;
import com.unkur.affnetui.entity.Language;
import com.unkur.affnetui.entity.Shop;
import com.unkur.affnetui.entity.ShopSource;
import com.unkur.affnetui.entity.SupportedFileFormat;
import com.unkur.affnetui.entity.User;

/**
 * Servlet implementation class CheckSignUpController
 */
@WebServlet("/checkSignUp")
public class CheckSignUpController extends HttpServlet {
	private static final long serialVersionUID = 1L;
    
	private static Logger logger = Logger.getLogger(CheckSignUpController.class.getName());
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CheckSignUpController() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.getRequestDispatcher(Urls.ERROR_PAGE_URL).forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		if( !validateParameters(request) ) {
			response.sendRedirect(Urls.SIGNUP_PAGE_URL + Links.createQueryString(Links.ERROR_PARAM_NAME));
			return;
		}
		
		String shopName = request.getParameter(Links.SHOP_NAME_PARAM_NAME);
		String shopUrl = request.getParameter(Links.SHOP_URL_PARAM_NAME);
		String email = request.getParameter(Links.EMAIL_PARAM_NAME);
		String pass = request.getParameter(Links.PASSWORD_PARAM_NAME);
		String lang = request.getParameter(Links.LANGUAGE_PARAM_NAME);
		String firstName = request.getParameter(Links.FIRST_NAME_PARAM_NAME);
		String lastName = request.getParameter(Links.LAST_NAME_PARAM_NAME);
		
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
		
/*		//for testing
		resourceFileFormat = ".csv";
		resourceDownloadUrl = "http://help.yandex.ru/help.yandex.ru/partnermarket/docs/primer-csv-formata-dlya-proizvolnogo-tovara.csv";
		resourceAuthRequired = "false";
		resourceAuthUsername = null;
		resourceAuthPassword = null;*/

		
		User freshUser;
		Shop freshShop;
		String redirectIfDuplicateUrl = Urls.SIGNUP_PAGE_URL + Links.createQueryString(Links.ERROR_PARAM_NAME);
		
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.beginTransaction();
		try {
			
			freshShop = new Shop(shopName, shopUrl);
			int sid = (Integer) session.save(freshShop);
			freshShop.setId(sid);
			
			if(resourceAvailable) {
				SupportedFileFormat fileFormat = (SupportedFileFormat) session.createQuery("FROM SupportedFileFormat s WHERE s.extension = \'" + resourceFileFormat + "\'").uniqueResult();
				ShopSource source = new ShopSource(freshShop.getId(), fileFormat, resourceDownloadUrl,
						resourceAuthRequired, resourceAuthUsername, resourceAuthPassword, false, 0);
				source.setIs_active(true);
				int id = (Integer) session.save(source);
				source.setId(id);
			}
			
			Query q = session.createQuery("FROM Language l WHERE l.code = ?");
			q.setString(0, lang);
			Language l = (Language) q.uniqueResult();
			
			freshUser = new User(email, pass, firstName, lastName, freshShop.getId(), l);
			
			int uid = (Integer) session.save(freshUser);
			freshUser.setId(uid);
			
			tx.commit();
		}catch (Exception e) {
	         if (tx!=null) tx.rollback();
	         StatusEndpoint.incrementErrors();
	         logger.error("Problem while inserting to DB, not Shop nor User were created, " + Throwables.getStackTraceAsString(e));
	         if(e.getCause() != null && e.getMessage().contains("ERROR: duplicate key value violates unique constraint")) {
	        	 response.sendRedirect(Urls.SIGNUP_PAGE_URL + Links.createQueryString(Links.DUPLICATE_SHOP_PARAM_NAME));
	         } else {
	        	 response.sendRedirect(Urls.ERROR_PAGE_URL);
	         }
	         return;
		}finally {
			session.close(); 
		}
		
		logger.info("Successfull webshop user registration shop URL = \"" + shopUrl + "\" email=\"" + email + "\"");
		
		//register OK, create new Session and attach this user to it
		HttpSession httpSession = (HttpSession) request.getSession(true);
		httpSession.setAttribute(Links.SESSION_USER_ATTR_NAME, freshUser);
		
		String language = freshUser.getLanguage().getCode();
		httpSession.setAttribute("language", language);
		httpSession.setAttribute("name", freshUser.getFirstName());

		//redirect to upload page
		response.sendRedirect(Urls.USER_UI_MAIN_PAGE_URL);
	}
	
	private boolean validateParameters(HttpServletRequest request) {
		
		String shopName = request.getParameter(Links.SHOP_NAME_PARAM_NAME);
		String shopUrl = request.getParameter(Links.SHOP_URL_PARAM_NAME);
		String email = request.getParameter(Links.EMAIL_PARAM_NAME);
		String pass = request.getParameter(Links.PASSWORD_PARAM_NAME);
		String lang = request.getParameter(Links.LANGUAGE_PARAM_NAME);
		String firstName = request.getParameter(Links.FIRST_NAME_PARAM_NAME);
		
		if(shopName == null || shopUrl == null || email == null || pass == null || lang == null || firstName ==null ||
		   shopName.isEmpty() || shopUrl.isEmpty() || email.isEmpty() || pass.isEmpty() || firstName.isEmpty()) {
			return false;
		}
		
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
