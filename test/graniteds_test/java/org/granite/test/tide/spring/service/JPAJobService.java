package org.granite.test.tide.spring.service;

import java.sql.Date;
import java.util.HashSet;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.granite.test.tide.data.Customer;
import org.granite.test.tide.data.Job;
import org.granite.test.tide.data.JobApplication;
import org.granite.test.tide.data.Meeting;
import org.granite.test.tide.data.User;
import org.granite.tide.data.DataEnabled;
import org.granite.tide.data.DataEnabled.PublishMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@DataEnabled(topic="testTopic", publish=PublishMode.ON_COMMIT, useInterceptor=true)
public class JPAJobService implements JobService {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	public void init() {
		Customer customer = entityManager.find(Customer.class, 1);
		if (customer == null) {
			customer = new Customer();
			customer.initIdUid(1, "C1");
			Job job = new Job();
			job.initIdUid(1, "J1");
			customer.setJobs(new HashSet<Job>());
			customer.getJobs().add(job);
			job.setCustomer(customer);
			job.setApplications(new HashSet<JobApplication>());
			User user = new User();
			user.initIdUid(1, "U1");
			user.setApplications(new HashSet<JobApplication>());
			entityManager.persist(customer);
			entityManager.persist(user);
		}
	}
    
	public Object[] apply(Integer jobId, Integer userId) {
		Job job = entityManager.find(Job.class, jobId);
		User user = entityManager.find(User.class, userId);
		
		JobApplication application = new JobApplication();
		application.initUid();
		application.setDate(new Date(new java.util.Date().getTime()));
		application.setJob(job);
		application.setUser(user);
		entityManager.persist(application);
		
		job.getApplications().add(application);
		user.getApplications().add(application);
		
		return new Object[] { job, user, application };
	}
    
	public Object[] createMeeting(Integer customerId, Integer userId) {
		Customer customer = entityManager.find(Customer.class, customerId);
		User user = entityManager.find(User.class, userId);
		
		Meeting meeting = new Meeting();
		meeting.initUid();
		meeting.setCustomer(customer);
		meeting.setUser(user);
		customer.getMeetings().add(meeting);
		user.getMeetings().add(meeting);
		
		return new Object[] { customer, user, meeting };
	}
    
	public Object[] closeMeeting(Integer meetingId) {
		Meeting meeting = entityManager.find(Meeting.class, meetingId);
		
		meeting.getUser().getMeetings().remove(meeting);
		meeting.getCustomer().getMeetings().remove(meeting);
		
		return new Object[] { meeting };
	}
}
