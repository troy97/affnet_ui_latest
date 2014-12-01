package com.unkur.affnetui.dao.impl;

import org.hibernate.Query;
import org.hibernate.Session;

import com.unkur.affnetui.config.HibernateUtil;

public class OrderDaoImpl {
	
	public long getNumberForPeriod(long from, long to, int shopId) {
		long result = 0;
		Session s = HibernateUtil.getCurrentSession();
		Query q = s.createQuery("SELECT count(*) FROM Order c WHERE c.created_at > " + from + 
				" AND c.created_at < " + to + " AND c.shop_id = " + shopId);
		result = (Long) q.uniqueResult();
		return result;
	}
	
	public long getNumberOfCancelledForPeriod(long from, long to, int shopId) {
		long result = 0;
		Session s = HibernateUtil.getCurrentSession();
		Query q = s.createQuery("SELECT count(*) FROM Order c WHERE c.created_at > " + from
				+ " AND c.created_at < " + to + " AND c.status='Cancelled' AND c.shop_id = " + shopId);
		result = (Long) q.uniqueResult();
		return result;
	}

}
