package com.unkur.affnetui.controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Transaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.unkur.affnetui.config.HibernateUtil;
import com.unkur.affnetui.config.Links;
import com.unkur.affnetui.dao.impl.ClickDaoImpl;
import com.unkur.affnetui.dao.impl.OrderDaoImpl;
import com.unkur.affnetui.entity.User;


/**
 * Servlet implementation class GraphDataController
 */
@WebServlet("/graphData")
public class GraphDataController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GraphDataController() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		HttpSession httpSession = request.getSession();
		User user = (User) httpSession.getAttribute(Links.SESSION_USER_ATTR_NAME);
		int shopId = user.getShopId();
		
		long now = System.currentTimeMillis();
		long day = TimeUnit.DAYS.toMillis(1);
		long week = TimeUnit.DAYS.toMillis(7);
		
		Transaction tx = HibernateUtil.getCurrentSession().beginTransaction();
		
		ClickDaoImpl dao = new ClickDaoImpl(); 
		long[][] clicks = new long[][]{
				{now, dao.getNumberForPeriod(now-day, now, shopId)},
				{now-day, dao.getNumberForPeriod(now-day*2, now-day, shopId)},
				{now-day*2, dao.getNumberForPeriod(now-day*3, now-day*2, shopId)},
				{now-day*3, dao.getNumberForPeriod(now-day*4, now-day*3, shopId)},
				{now-day*4, dao.getNumberForPeriod(now-day*5, now-day*4, shopId)},
				{now-day*5, dao.getNumberForPeriod(now-day*6, now-day*5, shopId)},
				{now-day*6, dao.getNumberForPeriod(now-day*7, now-day*6, shopId)},
		};
		
		OrderDaoImpl orderDao = new OrderDaoImpl();
		long[][] orders = new long[][]{
				{now, orderDao.getNumberForPeriod(now-day, now, shopId)},
				{now-day, orderDao.getNumberForPeriod(now-day*2, now-day, shopId)},
				{now-day*2, orderDao.getNumberForPeriod(now-day*3, now-day*2, shopId)},
				{now-day*3, orderDao.getNumberForPeriod(now-day*4, now-day*3, shopId)},
				{now-day*4, orderDao.getNumberForPeriod(now-day*5, now-day*4, shopId)},
				{now-day*5, orderDao.getNumberForPeriod(now-day*6, now-day*5, shopId)},
				{now-day*6, orderDao.getNumberForPeriod(now-day*7, now-day*6, shopId)},
		};
		
		tx.commit();

		
		JSONObject json = new JSONObject();
		json.put("clicks", createJSONArr(clicks));
		json.put("orders", createJSONArr(orders));
		
		PrintWriter out = response.getWriter();
		out.write(json.toJSONString());
	}
	
	private JSONArray createJSONArr(long[][] line) {
		JSONArray result = new JSONArray();
		
		for(int i=0; i<line.length; i++) {
			JSONArray list = new JSONArray();
			for(int j=0; j<line[0].length; j++) {
				list.add(line[i][j]);
			}
			result.add(list);
		}
		
		return result;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
