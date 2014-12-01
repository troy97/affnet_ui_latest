package com.unkur.affnetui.config;

import java.util.Map;

public class Links {
	
	//jsp URLs for forwarding
		//admin ui
	public static final String LOGIN_PAGE_JSP = "/loginPage.jsp";
	public static final String ADMIN_DOWNLOAD_SUCCESS_JSP = "/adminDownloadSuccess.jsp";
	public static final String ADMIN_UPLOAD_PAGE_JSP = "/adminUploadPage.jsp";
		//user ui
	public static final String SIGNIN_PAGE_JSP = "/signInPage.jsp";
	public static final String USER_DOWNLOAD_SUCCESS_JSP = "/userDownloadSuccess.jsp";
	public static final String SIGNUP_PAGE_JSP = "/signUpPage.jsp";
	public static final String USER_UPLOAD_PAGE_JSP = "/userUploadPage.jsp";
	public static final String USER_CABINET_JSP = "/userCabinetPage.jsp";
	public static final String UPDATE_USER_PROFILE_JSP = "/updateProfile.jsp";
	public static final String UPDATE_PROFILE_SUCCESS_JSP = "/updateProfileSuccess.jsp";
	public static final String VIEW_LAST_FILES_JSP = "/viewLastFiles.jsp";
		//common ui
	public static final String LOGOUT_PAGE_JSP = "/logOutPage.jsp";
	public static final String MAIN_PAGE_JSP = "/index.jsp";
	public static final String ERROR_PAGE_JSP = "/errorPage.jsp";
	
	
	//attribute names
	   //for exchange object
	public static final String EXCHANGE_CLICK_COUNT_ATTR_NAME = "clickCount";
	   //for session object
	public static final String SESSION_USER_ATTR_NAME = "user";
	public static final String SESSION_ADMIN_ATTR_NAME = "admin";
	
	//query parameter names
		//credentials, registration info
	public static final String EMAIL_PARAM_NAME = "email";
	public static final String PASSWORD_PARAM_NAME = "password";
	public static final String NAME_PARAM_NAME = "name";
	public static final String FIRST_NAME_PARAM_NAME = "nameFirst";
	public static final String LAST_NAME_PARAM_NAME = "nameLast";
	public static final String LANGUAGE_PARAM_NAME = "language";
	public static final String SHOP_NAME_PARAM_NAME = "shopName";
	public static final String SHOP_URL_PARAM_NAME = "shopUrl";
	
	public static final String SHOP_RESOURCE_AVAILABLE_PARAM_NAME = "resourceAvailable";
	public static final String SHOP_RESOURCE_FILE_FORMAT_PARAM_NAME = "fileFormat";
	public static final String SHOP_RESOURCE_URL_PARAM_NAME = "resourceUrl";
	public static final String SHOP_RESOURCE_AUTH_REQUIRED_PARAM_NAME = "resourceAuthRequired";
	public static final String SHOP_RESOURCE_AUTH_USERNAME_PARAM_NAME = "basicAuthUsername";
	public static final String SHOP_RESOURCE_AUTH_PASSWORD_PARAM_NAME = "basicAuthPassword";
	
		//params that indicate errors
	public static final String ERROR_PARAM_NAME = "wrong";
	public static final String INVALID_FILE_PARAM_NAME = "invalidFile";
	public static final String DUPLICATE_USER_PARAM_NAME = "duplicateUser";
	public static final String DUPLICATE_SHOP_PARAM_NAME = "duplicateShop";
		//params for distributor links
	public static final String PRODUCT_ID_PARAM_NAME = "productId";
	public static final String DISTRIBUTOR_ID_PARAM_NAME = "distributorId";
	public static final String DISTRIBUTOR_SUB_ID_PARAM_NAME = "distributorSubId";
	public static final String SHOP_ID_PARAM_NAME = "shopId";
	public static final String FILE_TEMPLATE_ID_PARAM_NAME = "fileTemplateId";
	public static final String CLICK_ID_PARAM_NAME = "affiliateNetworkClickId";
	public static final String SUB_ID_PARAM_NAME = "subId";
	
	
	/**
	 * Creates string of type "?paramName1=true&paramName2=true..."
	 * @param paramNames
	 * @return
	 */
	public static String createQueryString(String... paramNames) {
		StringBuilder result = new StringBuilder("?");
		for(String name : paramNames) {
			result.append(name + "=true&");
		}
		result.deleteCharAt(result.length()-1); //delete last "&"
		return result.toString();
	}
	
	/**
	 * Creates string of type "?paramName1=true&paramName2=true..."
	 * @param paramNames
	 * @return
	 */
	public static String createQueryString(Map<String, String> data) {
		StringBuilder result = new StringBuilder("?");
		for(Map.Entry<String, String> entry : data.entrySet()) {
			result.append(entry.getKey() + "=" + entry.getValue() + "&");
		}
		result.deleteCharAt(result.length()-1); //delete last "&"
		return result.toString();
	}
	
	/**
	 * Creates html "a href" from given uri and link name
	 * @param uri
	 * @param linkName
	 * @return a tag link
	 */
	public static String wrapWithA(String uri, String linkName) {
		return "<a href=\"" + uri + "\">" + linkName + "</a>";
	}
	
	/**
	 * Strips query string from given url or does nothing
	 * if no query string was attached
	 * @param urlWithQuery
	 * @return url without query string
	 */
	public static String stripQuery(String urlWithQuery) {
		String result = urlWithQuery;
		if(urlWithQuery.contains("?")) {
			result = urlWithQuery.split("\\?")[0];
		}
		return result;
	}
	
}
