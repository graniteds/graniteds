package org.granite.test.tide.spring.service;



public interface JobService {
	
	public void init();
    
	public Object[] apply(Integer jobId, Integer userId);
    
	public Object[] createMeeting(Integer customerId, Integer userId);

	public void newMeeting(Integer customerId, Integer userId);

	public Object[] closeMeeting(Integer meetingId);
}
