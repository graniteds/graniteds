package org.granite.tide.data;

import javax.transaction.Synchronization;

import org.granite.tide.data.DataEnabled.PublishMode;


public class TideDataPublishingSynchronization implements Synchronization {
	
	private final boolean removeContext;
	
	public TideDataPublishingSynchronization(boolean removeContext) {
		this.removeContext = removeContext;
	}

	public void beforeCompletion() {
		DataContext.publish(PublishMode.ON_COMMIT);
		if (removeContext)
			DataContext.remove();
	}

	public void afterCompletion(int status) {
		if (removeContext)
			DataContext.remove();
	}
	
}