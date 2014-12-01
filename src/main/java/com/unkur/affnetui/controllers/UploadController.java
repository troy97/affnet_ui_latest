package com.unkur.affnetui.controllers;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Throwables;
import com.unkur.affnetui.config.Config;
import com.unkur.affnetui.config.HibernateUtil;
import com.unkur.affnetui.config.Links;
import com.unkur.affnetui.config.Urls;
import com.unkur.affnetui.dao.impl.FileDaoImpl;
import com.unkur.affnetui.entity.Admin;
import com.unkur.affnetui.entity.SupportedFileFormat;
import com.unkur.affnetui.entity.UploadedFile;
import com.unkur.affnetui.entity.User;

/**
 * Servlet implementation class UploadController
 */
@WebServlet("/uploadController")
@MultipartConfig
public class UploadController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = Logger.getLogger(UploadController.class.getName());
	
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
		HttpSession httpSession = (HttpSession) request.getSession();
		
		String shopIdStr = request.getParameter("webshopname"); // Retrieves <input type="hidden" name="webshopname">
		int shopId = Integer.valueOf(shopIdStr);
		
		Part filePart = request.getPart("datafile"); // Retrieves <input type="file" name="datafile">
		String filename = getFilename(filePart);
		InputStream filecontent = filePart.getInputStream();
		File tmpFile = createEmptyTmpFile();
		saveToFile(filecontent, tmpFile);
		filecontent.close();
		
		UploadedFile file = new UploadedFile(tmpFile.getAbsolutePath(), getFileExtension(filename), System.currentTimeMillis(), shopId);
		persist(file);
		
		forwardToJsp(request, response, httpSession);
	}
	
	
	
	private String getFilename(Part part) {
	    for (String cd : part.getHeader("content-disposition").split(";")) {
	        if (cd.trim().startsWith("filename")) {
	            String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
	            return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
	        }
	    }
	    return null;
	}

	private File createEmptyTmpFile() throws IOException {
			String uploadFolderPath = Config.UPLOAD_FOLDER;
			File freshFile = File.createTempFile("upload", ".tmp", new File(uploadFolderPath));
			return freshFile;
	/*		boolean fileCreated = false;
			while(!fileCreated) {
				String tmpFileName = shopId + "_" + System.currentTimeMillis() + "_" + new Random().nextInt(2048);
				String tmpFilePath = uploadFolderPath + File.pathSeparator + tmpFileName;
				fileCreated = new File(tmpFilePath).createNewFile();
			}*/
		}

	private void saveToFile(InputStream filecontent, File tmpFile) throws IOException {
		BufferedOutputStream fileOs = new BufferedOutputStream(new FileOutputStream(tmpFile));
		IOUtils.copy(filecontent, fileOs);
		fileOs.close();
	}

	private void persist(UploadedFile file) {
		Session s = HibernateUtil.getCurrentSession();
		Transaction tx = s.beginTransaction();
		try {
			int id = new FileDaoImpl().insertOne(file);
			file.setId(id);
			tx.commit();
		} catch (Exception e) {
			logger.error("Can't save uploaded file to DB: " + Throwables.getStackTraceAsString(e));
			tx.rollback();
		}
	}

	private SupportedFileFormat getFileFormat(String fileName) {
		String extension = getFileExtension(fileName);
		SupportedFileFormat result = fetchFromDB(extension);
		return result;
	}

	private String getFileExtension(String fileName) {
		String extension = fileName.substring(fileName.length() - ".xxx".length()).toLowerCase();
		return extension;
	}
	
	private SupportedFileFormat fetchFromDB(String extension) {
		Session session = HibernateUtil.getCurrentSession();
		Transaction tx = HibernateUtil.getCurrentSession().beginTransaction();
		SupportedFileFormat result =
				(SupportedFileFormat) session.createQuery("FROM SupportedFileFormat f WHERE f.extension = \'" + extension + "\'").uniqueResult();
		tx.commit();
		return result;
	}

	private void forwardToJsp(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws ServletException, IOException {
		Object client = session.getAttribute(Links.SESSION_USER_ATTR_NAME);
		if(client == null) {
			client = session.getAttribute(Links.SESSION_ADMIN_ATTR_NAME);
			Admin admin = (Admin) client;
			request.setAttribute("logoutPage", Urls.LOGOUT_PAGE_URL);
			request.setAttribute("name", admin.getEmail());
			request.setAttribute("fileName", "insert_name_here_in_UploadController.java");
			request.setAttribute("uploadMoreLink", Urls.ADMIN_UPLOAD_PAGE_URL);
			request.getRequestDispatcher(Links.ADMIN_DOWNLOAD_SUCCESS_JSP).forward(request, response);
		} else {
			User user = (User) client;
			request.setAttribute("logoutPage", Urls.LOGOUT_PAGE_URL);
			request.setAttribute("cabinetPage", Urls.USER_CABINET_PAGE_URL);
			request.setAttribute("name", user.getEmail());
			request.setAttribute("uploadMoreLink", Urls.USER_UPLOAD_PAGE_URL);
			request.setAttribute("viewLastFiles", Urls.VIEW_LAST_FILES_PAGE_URL);
			response.sendRedirect(Urls.USER_UPLOAD_PAGE_URL);
		}
	}

}
