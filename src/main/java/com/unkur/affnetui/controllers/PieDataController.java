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
 * Servlet implementation class PieDataController
 */
@WebServlet("/pieData")
public class PieDataController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public PieDataController() {
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
		
		String confirmedLabel = "Confirmed";
		String cancelledLabel = "Cancelled";
		String clicksLabel = "Clicks";
		String ordersLabel = "Orders";

		long now = System.currentTimeMillis();
		long month = TimeUnit.DAYS.toMillis(30);
		
		Transaction tx = HibernateUtil.getCurrentSession().beginTransaction();
		ClickDaoImpl clickDao = new ClickDaoImpl();
		OrderDaoImpl orderDao = new OrderDaoImpl();
		
		JSONArray result = new JSONArray();
		
		String name = request.getParameter("name");
		String period = request.getParameter("period");
		
		if(period.equals("current")){
			if(name.equals("orders")) {   //Orders chart
				JSONObject ordersTotal = new JSONObject();
				ordersTotal.put("label", confirmedLabel);
				ordersTotal.put("data", orderDao.getNumberForPeriod(now-month, now, shopId));

				JSONObject ordersCancelled = new JSONObject();
				ordersCancelled.put("label", cancelledLabel);
				ordersCancelled.put("data", orderDao.getNumberOfCancelledForPeriod(now-month, now, shopId));

				result.add(ordersTotal);
				result.add(ordersCancelled);
			} else {						//Conversion chart
				JSONObject clicks = new JSONObject();
				clicks.put("label", clicksLabel);
				clicks.put("data", clickDao.getNumberForPeriod(now-month, now, shopId));

				JSONObject ordersConfirmed = new JSONObject();
				ordersConfirmed.put("label", ordersLabel);
				ordersConfirmed.put("data", orderDao.getNumberForPeriod(now-month, now, shopId));

				result.add(clicks);
				result.add(ordersConfirmed);
			}
		} else {
			if(name.equals("orders")) {   //Orders chart
				JSONObject ordersTotal = new JSONObject();
				ordersTotal.put("label", confirmedLabel);
				ordersTotal.put("data", orderDao.getNumberForPeriod(now-month*2, now-month, shopId));

				JSONObject ordersCancelled = new JSONObject();
				ordersCancelled.put("label", cancelledLabel);
				ordersCancelled.put("data", orderDao.getNumberOfCancelledForPeriod(now-month*2, now-month, shopId));

				result.add(ordersTotal);
				result.add(ordersCancelled);
			} else {						//Conversion chart
				JSONObject clicks = new JSONObject();
				clicks.put("label", clicksLabel);
				clicks.put("data", clickDao.getNumberForPeriod(now-month*2, now-month, shopId));

				JSONObject ordersConfirmed = new JSONObject();
				ordersConfirmed.put("label", ordersLabel);
				ordersConfirmed.put("data", orderDao.getNumberForPeriod(now-month*2, now-month, shopId));

				result.add(clicks);
				result.add(ordersConfirmed);
			}
		}
		
		tx.commit();
		
		PrintWriter w = response.getWriter();
		w.write(result.toJSONString());
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
