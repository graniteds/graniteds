/**
 *   GRANITE DATA SERVICES
 *   Copyright (C) 2006-2013 GRANITE DATA SERVICES S.A.S.
 *
 *   This file is part of the Granite Data Services Platform.
 *
 *   Granite Data Services is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU Lesser General Public
 *   License as published by the Free Software Foundation; either
 *   version 2.1 of the License, or (at your option) any later version.
 *
 *   Granite Data Services is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 *   General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301,
 *   USA, or see <http://www.gnu.org/licenses/>.
 */
package org.granite.gravity;

import org.granite.jmx.MBean;
import org.granite.jmx.MBeanAttribute;
import org.granite.jmx.MBeanOperation;
import org.granite.jmx.MBeanParameter;
import org.granite.jmx.MBeanOperation.Impact;

/**
 * @author Franck WOLFF
 */
@MBean(description="MBean used for Gravity operations")
public interface DefaultGravityMBean {

	///////////////////////////////////////////////////////////////////////////
	// Attributes.
	
    @MBeanAttribute(description="Factory class name used for intantiating Gravity")
	public String getGravityFactoryName();

    @MBeanAttribute(description="Amount of time after which an idle channel may be removed")
	public long getChannelIdleTimeoutMillis();
	public void setChannelIdleTimeoutMillis(
		@MBeanParameter(name="channelIdleTimeoutMillis", description="New channel's idle timeout")
		long channelIdleTimeoutMillis
	);

    @MBeanAttribute(description="Long polling timeout in milliseconds (may not work with all containers)")
	public long getLongPollingTimeoutMillis();
	public void setLongPollingTimeoutMillis(
		@MBeanParameter(name="longPollingTimeoutMillis", description="New long polling timeout")
		long longPollingTimeoutMillis
	);

    @MBeanAttribute(description="Should unsent messages be kept in the queue on IOExceptions?")
	public boolean isRetryOnError();
	public void setRetryOnError(
		@MBeanParameter(name="retryOnError", description="New retry on error value")
		boolean retryOnError
	);

    @MBeanAttribute(description="Channel's queue maximum size")
	public int getMaxMessagesQueuedPerChannel();
	public void setMaxMessagesQueuedPerChannel(
		@MBeanParameter(name="maxMessagesQueuedPerChannel", description="New maximum messages queued value")
		int maxMessagesQueuedPerChannel
	);

    @MBeanAttribute(description="Client advice for reconnection interval")
	public long getReconnectIntervalMillis();

    @MBeanAttribute(description="Client advice for reconnection max attempts")
	public int getReconnectMaxAttempts();
	
	@MBeanAttribute(description="Maximum number of channels that may be queued in the Gravity pool")
    public int getQueueCapacity();
    
	@MBeanAttribute(description=
		"Number of channels that the Gravity pool queue can ideally (in the absence of " +
		"memory or resource constraints) accept without blocking")
    public int getQueueRemainingCapacity();
    
	@MBeanAttribute(description="Number of channels in the Gravity pool queue waiting for execution")
    public int getQueueSize();

	@MBeanAttribute(description="Number of threads to keep in the Gravity pool, even if they are idle")
    public int getCorePoolSize();
	public void setCorePoolSize(
		@MBeanParameter(name="corePoolSize", description="New core pool size")
		int corePoolSize
	);

	@MBeanAttribute(description="Maximum number of threads to allow in the Gravity pool")
	public int getMaximumPoolSize();
	public void setMaximumPoolSize(
		@MBeanParameter(name="maximumPoolSize", description="New maximum pool size")
		int maximumPoolSize
	);

    @MBeanAttribute(description=
    	"When the number of threads is greater than the core, this is the maximum " +
    	"time that excess idle threads will wait for new tasks before terminating")
	public long getKeepAliveTimeMillis();
    public void setKeepAliveTimeMillis(
    	@MBeanParameter(name="keepAliveTimeMillis", description="New keep alive time in milliseconds")
    	long keepAliveTimeMillis
    );

	@MBeanAttribute(description="Tell if this Gravity has been succefully started")
    public boolean isStarted();

	///////////////////////////////////////////////////////////////////////////
	// Operations.
    
	@MBeanOperation(description="Start Gravity", impact=Impact.ACTION)
    public void start() throws Exception;
    
	@MBeanOperation(description="Restart Gravity", impact=Impact.ACTION)
    public void restart() throws Exception;
    
	@MBeanOperation(
		description="Attempts to stop all actively executing channels and halts the processing of waiting channels",
		impact=Impact.ACTION
	)
    public void stop() throws Exception;
}
