package com.unkur.affnetui.controllers.userui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.unkur.affnetui.config.HibernateUtil;
import com.unkur.affnetui.config.Links;
import com.unkur.affnetui.entity.Order;
import com.unkur.affnetui.entity.Product;
import com.unkur.affnetui.entity.User;
import com.unkur.affnetui.utils.NotificationCreator;

/**
 * Servlet implementation class DashboardController
 */
@WebServlet("/user/dash")
public class DashboardController extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DashboardController() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//get session and user object (this request has passed Auth filter)
		HttpSession httpsession = (HttpSession) request.getSession();
		User user = (User) httpsession.getAttribute(Links.SESSION_USER_ATTR_NAME);
		request.setAttribute("user", user);
		
		//read data from DB
		Session session = HibernateUtil.getCurrentSession();
		Transaction tx = session.beginTransaction();
		
		NotificationCreator notifCreator = new NotificationCreator(user.getShopId());
		notifCreator.fill(session);
		
/*		Order firstOrder = (Order) session.createQuery("FROM Order order ORDER BY order.created_at ASC").setMaxResults(1).uniqueResult();
		System.out.println("First order = " + new Date(firstOrder.getCreated_at()));
		Order latestOrder = (Order) session.createQuery("FROM Order order ORDER BY order.created_at DESC").setMaxResults(1).uniqueResult(); 
		System.out.println("Latest order = " + new Date(latestOrder.getCreated_at()));*/
		
/*		Click firstClick = (Click) session.createQuery("FROM Click order ORDER BY order.clickTime ASC").setMaxResults(1).uniqueResult();
		System.out.println("First click = " + new Date(firstClick.getClickTime()));
		Click lastClick = (Click) session.createQuery("FROM Click order ORDER BY order.clickTime DESC").setMaxResults(1).uniqueResult();
		System.out.println("Last click = " + new Date(lastClick.getClickTime()));*/
		
/*		long clicksLastWeek = getClicksNumber(session, lastClick.getClickTime()-week, lastClick.getClickTime());
		System.out.println("Clicks last week: " + clicksLastWeek);
		long ordersLastWeek = getOrdersNumber(session, latestOrder.getCreated_at()-week, latestOrder.getCreated_at());
		System.out.println("Orders last week: " + ordersLastWeek);
		long confirmedOrdersLastWeek = ((Long) session.createQuery("SELECT count(*) FROM Order o WHERE o.created_at > " + (latestOrder.getCreated_at()-week) + "AND o.status = \'Confirmed\'").uniqueResult()).intValue();
		System.out.println("Confirmed orders last week: " + confirmedOrdersLastWeek);
		long cancelledOrdersLastWeek = ((Long) session.createQuery("SELECT count(*) FROM Order o WHERE o.created_at > " + (latestOrder.getCreated_at()-week) + "AND o.status = \'Cancelled\'").uniqueResult()).intValue();
		System.out.println("Canceled orders last week: " + cancelledOrdersLastWeek);*/
		
		List<Order> lastTenOrders = session.createQuery("FROM Order o WHERE o.shop_id = " +user.getShopId()+ " ORDER BY o.updated_at DESC").setMaxResults(10).list();
		request.setAttribute("lastTenOrders", lastTenOrders);
		
		Set<Product> topGrowingProducts = getTopGrowing(session);
		request.setAttribute("topGrowingProducts", topGrowingProducts);
		
		long to = System.currentTimeMillis();
		long from = to - TimeUnit.DAYS.toMillis(1);
		List<Product> topProductsVisited = getMostVisited(session, user.getShopId(), from, to);
		request.setAttribute("topProductsVisited", topProductsVisited);
		List<Integer> visitsDay = getVisitsDay(session, topProductsVisited);
		request.setAttribute("visitsDay", visitsDay);
		List<Integer> visitsWeek = getVisitsWeek(session, topProductsVisited);
		request.setAttribute("visitsWeek", visitsWeek);
		

		tx.commit();
		
		//render page
		request.getRequestDispatcher("/dash.jsp").forward(request, response);
	}
	
	
	private List<Integer> getVisitsDay(Session s, List<Product> products) {
		List<Integer> result = new ArrayList<Integer>();
		long to = System.currentTimeMillis();
		long from = to - TimeUnit.DAYS.toMillis(1);
		for(Product p : products) {
			Query q = s.createQuery("SELECT count(*) FROM Click c WHERE c.productId = " + p.getId() + " AND c.clickTime > " + from);
			result.add( (int) (long) q.uniqueResult() );
		}
		return result;
	}
	
	private List<Integer> getVisitsWeek(Session s, List<Product> products) {
		List<Integer> result = new ArrayList<Integer>();
		long to = System.currentTimeMillis();
		long from = to - TimeUnit.DAYS.toMillis(7);
		for(Product p : products) {
			Query q = s.createQuery("SELECT count(*) FROM Click c WHERE c.productId = " + p.getId() + " AND c.clickTime > " + from);
			result.add( (int) (long) q.uniqueResult() );
		}
		return result;
	}
	
	
	private List<Product> getMostVisited(Session s, int shopId, long from, long to) {
		List<Product> result = new ArrayList<>(10);
		Query q = s.createQuery("SELECT c.productId FROM Click c "
						+ "WHERE c.shopId= "+ shopId +" AND c.clickTime > " + from + " AND c.clickTime < " + to + " "
						+ "GROUP BY c.productId "
						+ "ORDER BY count(*) DESC");
		q.setMaxResults(10);
		List<Long> ids = q.list();
		for(long id : ids) {
			result.add( (Product) s.get(Product.class, id) );
		}
		return result;
	}

	private Set<Product> getTopGrowing(Session s) {
		Set<Product> result = new LinkedHashSet<>();
		Random r = new Random();
		while(result.size()<10) {
			try {
				long id = 1+r.nextInt(350);
				result.add( (Product) s.get(Product.class, id) );
			} catch (Exception e) {
				System.out.println("Top growing orders exception: " + e);
			}
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
