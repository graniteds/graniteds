/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package test.granite.guice.listeners;

import javax.persistence.EntityManagerFactory;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import test.granite.guice.modules.GuiceModule;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.wideplay.warp.persist.PersistenceService;
import com.wideplay.warp.persist.UnitOfWork;

/**
 * @author Matt GIACOMI
 */
public class InitNDestroy implements ServletContextListener {

    private static final Logger log = Logger.getLogger(InitNDestroy.class);

    @Inject
    private EntityManagerFactory emf;

    private ServletContext context = null;

    public void contextInitialized(ServletContextEvent event) {
        context = event.getServletContext();

        // initialize Guice injector and WARP/Hibernate persistence, store in context.
        Injector injector = Guice.createInjector(new Module[] {
            new GuiceModule(),
            PersistenceService.usingJpa().across(UnitOfWork.REQUEST).buildModule()
        });
        log.info("Guice Initalized.");

        injector.getInstance(PersistenceService.class).start();
        context.setAttribute(Injector.class.getName(), injector);

        log.info("EntityManager Started.");
    }

    public void contextDestroyed(ServletContextEvent event) {
        context = event.getServletContext();
        try {
            // inject 'this' class into Guice to populate EntityManagerFactory,
            // so that we can shutdown JPA properly.
            Injector injector = (Injector)context.getAttribute(Injector.class.getName());
            if (injector != null)
                injector.injectMembers(this);
            if (emf != null)
                emf.close();
            log.info("EntityManagerFactory Shutdown.");

            context.removeAttribute(Injector.class.getName());
        } catch (Exception e) {
            log.error("Could not shutdown EntityManagerFactory", e);
        }
    }
}