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
package org.granite.client.test.server.feed;

import flex.messaging.messages.AsyncMessage;
import org.granite.gravity.Gravity;
import org.granite.gravity.GravityManager;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Created by william on 30/09/13.
 */
@WebListener
public class FeedListener implements ServletContextListener {

    private Thread feedThread;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Feed feed = new Feed(servletContextEvent.getServletContext());
        feedThread = new Thread(feed);
        feedThread.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        feedThread.interrupt();
    }

    private static class Feed implements Runnable {

        private final ServletContext servletContext;

        public Feed(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        @Override
        public void run() {
            int count = 1;

            while (true) {
                try {
                    Thread.sleep(250L);
                }
                catch (InterruptedException e) {
                    return;
                }

                Gravity gravity = GravityManager.getGravity(servletContext);
                if (gravity == null)
                    continue;

                Info info = new Info();
                info.setName("INFO" + (count++));
                info.setValue(Math.random() * 100.0);

                AsyncMessage message = new AsyncMessage();
                message.setHeader(AsyncMessage.SUBTOPIC_HEADER, "feed");
                message.setDestination("feed");
                message.setBody(info);
                gravity.publishMessage(message);
            }
        }
    }
}
