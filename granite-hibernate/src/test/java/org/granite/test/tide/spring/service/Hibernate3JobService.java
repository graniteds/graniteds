/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of Granite Data Services.
 *
 *   Granite Data Services is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or (at your
 *   option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful, but
 *   WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *   FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
 *   for more details.
 *
 *   You should have received a copy of the GNU Library General Public License
 *   along with this library; if not, see <http://www.gnu.org/licenses/>.
 */
package org.granite.test.tide.spring.service;

import java.sql.Date;
import java.util.HashSet;

import org.granite.test.tide.data.Customer;
import org.granite.test.tide.data.Job;
import org.granite.test.tide.data.JobApplication;
import org.granite.test.tide.data.Meeting;
import org.granite.test.tide.data.User;
import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.hibernate.Hibernate;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@DataEnabled(topic="testTopic", publish=PublishMode.ON_COMMIT, useInterceptor=true)
public class Hibernate3JobService implements JobService {
	
	protected SessionFactory sessionFactory;
	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}
	
	public void init() {
		Customer customer;
		try {
			customer = (Customer)getSession().load(Customer.class, 1L);
			Hibernate.initialize(customer);
		}
		catch (ObjectNotFoundException e) {
			customer = new Customer();
			customer.initIdUid(1L, "C1");
			Job job = new Job();
			job.initIdUid(1L, "J1");
			customer.setJobs(new HashSet<Job>());
			customer.getJobs().add(job);
			job.setCustomer(customer);
			job.setApplications(new HashSet<JobApplication>());
			User user = new User();
			user.initIdUid(1L, "U1");
			user.setApplications(new HashSet<JobApplication>());
			getSession().persist(customer);
			getSession().persist(user);
		}
	}
    
	public Object[] apply(Long jobId, Long userId) {
		Session session = getSession();
		
		Job job = (Job)session.load(Job.class, jobId);
		User user = (User)session.load(User.class, userId);
		
		JobApplication application = new JobApplication();
		application.initUid();
		application.setDate(new Date(new java.util.Date().getTime()));
		application.setJob(job);
		application.setUser(user);
		session.persist(application);
		
		job.getApplications().add(application);
		user.getApplications().add(application);
		
		return new Object[] { job, user, application };
	}
    
	public Object[] createMeeting(Long customerId, Long userId) {
		Session session = getSession();
		
		Customer customer = (Customer)session.load(Customer.class, customerId);
		User user = (User)session.load(User.class, userId);
		
		Meeting meeting = new Meeting();
		meeting.initUid();
		meeting.setCustomer(customer);
		meeting.setUser(user);
		customer.getMeetings().add(meeting);
		user.getMeetings().add(meeting);
		
		return new Object[] { customer, user, meeting };
	}
	
	public void newMeeting(Long customerId, Long userId) {
		Session session = getSession();
		
		Customer customer = (Customer)session.load(Customer.class, customerId);
		User user = (User)session.load(User.class, userId);
		
		Meeting meeting = new Meeting();
		meeting.initUid();
		meeting.setCustomer(customer);
		meeting.setUser(user);
		customer.getMeetings().add(meeting);
		user.getMeetings().add(meeting);
	}
    
	public Object[] closeMeeting(Long meetingId) {
		Session session = getSession();
		
		Meeting meeting = (Meeting)session.load(Meeting.class, meetingId);
		
		meeting.getUser().getMeetings().remove(meeting);
		meeting.getCustomer().getMeetings().remove(meeting);
		
		return new Object[] { meeting };
	}
}
