package org.granite.test.tide.spring.service;



public interface JobService {
	
	public void init();
    
	public Object[] apply(Long jobId, Long userId);
    
	public Object[] createMeeting(Long customerId, Long userId);

	public void newMeeting(Long customerId, Long userId);

	public Object[] closeMeeting(Long meetingId);
}
