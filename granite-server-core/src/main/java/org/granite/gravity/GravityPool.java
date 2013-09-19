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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.granite.logging.Logger;

/**
 * @author Franck WOLFF
 */
public class GravityPool {

    private static final Logger log = Logger.getLogger(GravityPool.class);

    public static final int DEFAULT_CORE_POOL_SIZE = 5;
    public static final int DEFAULT_MAXIMUM_POOL_SIZE = 20;
    public static final long DEFAULT_KEEP_ALIVE_TIME = 10000L;
    public static final int DEFAULT_QUEUE_CAPACITY = Integer.MAX_VALUE;

    private final ThreadPoolExecutor pool;
    private final int queueCapacity;

    public GravityPool() {
    	this(DEFAULT_CORE_POOL_SIZE, DEFAULT_MAXIMUM_POOL_SIZE, DEFAULT_KEEP_ALIVE_TIME, DEFAULT_QUEUE_CAPACITY);
    }

    public GravityPool(GravityConfig config) {
    	this(config.getCorePoolSize(), config.getMaximumPoolSize(), config.getKeepAliveTimeMillis(), config.getQueueCapacity());
    }

    public GravityPool(int corePoolSize, int maximumPoolSize, long keepAliveTimeMillis, int queueCapacity) {
        log.info(
            "Starting Gravity Pool (corePoolSize=%d, maximumPoolSize=%d, keepAliveTimeMillis=%d, queueCapacity=%d)...",
            corePoolSize, maximumPoolSize, keepAliveTimeMillis, queueCapacity
        );

        this.queueCapacity = queueCapacity;
        this.pool = new ThreadPoolExecutor(
            corePoolSize,
            maximumPoolSize,
            keepAliveTimeMillis,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(queueCapacity),
            new ThreadPoolExecutor.AbortPolicy()
        );
    }
    
    public int getQueueCapacity() {
    	return queueCapacity;
    }
    
    public int getQueueRemainingCapacity() {
    	return pool.getQueue().remainingCapacity();
    }
    
    public int getQueueSize() {
    	return pool.getQueue().size();
    }

    public int getCorePoolSize() {
		return pool.getCorePoolSize();
	}
    public void setCorePoolSize(int corePoolSize) {
    	pool.setCorePoolSize(corePoolSize);
    }

	public int getMaximumPoolSize() {
		return pool.getMaximumPoolSize();
	}
	public void setMaximumPoolSize(int maximumPoolSize) {
		pool.setMaximumPoolSize(maximumPoolSize);
	}

	public long getKeepAliveTimeMillis() {
		return pool.getKeepAliveTime(TimeUnit.MILLISECONDS);
	}
	public void setKeepAliveTimeMillis(long keepAliveTimeMillis) {
		pool.setKeepAliveTime(keepAliveTimeMillis, TimeUnit.MILLISECONDS);
	}
	
	public void reconfigure(GravityConfig config) {
		pool.setCorePoolSize(config.getCorePoolSize());
		pool.setKeepAliveTime(config.getKeepAliveTimeMillis(), TimeUnit.MILLISECONDS);
		pool.setMaximumPoolSize(config.getMaximumPoolSize());
	}

	public void execute(AsyncChannelRunner runner) {
		if (runner == null)
			throw new NullPointerException("runner cannot be null");

		if (!pool.isShutdown()) {
			try {
				pool.execute(runner);
			}
			catch (RejectedExecutionException e) {
				runner.reset();
				throw e;
			}
		}
		else
			runner.reset();
    }

    public boolean contains(AsyncChannelRunner runner) {
        return pool.getQueue().contains(runner);
    }

    public boolean remove(AsyncChannelRunner runner) {
    	if (pool.getQueue().remove(runner)) {
    		runner.reset();
    		return true;
    	}
    	return false;
    }
    
    public void clear() {
    	List<Runnable> runnables = new ArrayList<Runnable>(pool.getQueue().size());
    	pool.getQueue().drainTo(runnables);
        for (Runnable runnable : runnables)
        	((AsyncChannelRunner)runnable).reset();
    }

    public boolean isShutdown() {
    	return pool.isShutdown();
    }

    public boolean isTerminated() {
    	return pool.isTerminated();
    }

    public void shutdown() {
        log.info("Stopping Gravity Pool...");
        pool.shutdown();
    }
    
    public List<AsyncChannelRunner> shutdownNow() {
        log.info("Stopping Gravity Pool Now...");
        List<Runnable> runnables = pool.shutdownNow();
        List<AsyncChannelRunner> runners = new ArrayList<AsyncChannelRunner>(runnables.size());
        for (Runnable runnable : runnables) {
        	AsyncChannelRunner runner = (AsyncChannelRunner)runnable;
        	runner.reset();
        	runners.add(runner);
        }
        return runners;
    }
}
