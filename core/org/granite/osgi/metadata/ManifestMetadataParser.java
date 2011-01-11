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
package org.granite.osgi.metadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.granite.logging.Logger;
import org.granite.osgi.classloader.ServiceClassLoader;
import org.granite.osgi.constants.OSGIConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.SynchronousBundleListener;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
/**
 * Parse the Manifest of a bundle to load the GraniteDS-Service config file
 * i.e. GraniteDS-Service: /resources/granite-osgi.xml
 * <pre>
 * <graniteds>
 *	<services>
 *		<service packages="org.graniteds.services.security" />
 *	</services>
 * </graniteds>
 * </pre>
 * @see SynchronousBundleListener
 * @author <a href="mailto:gembin@gmail.com">gembin@gmail.com</a>
 * @since 1.1.0
 */
public class ManifestMetadataParser implements SynchronousBundleListener {

	private static final Logger log = Logger.getLogger(ManifestMetadataParser.class);

	/**
	 * the seperator of the packages which will be scanned
	 * i.e. org.granite.service.test,org.granite.service.testa.*
	 */
	private static final String SEPERATOR=",";
	/**
	 * Element in the metadata file which will contain any number of service Elements
	 */
	private static final String SERVICES="services";
	/**
	 * Element in the metadata file
	 * i.e. <service packages="org.granite.osgi.test,org.granite.osgi.test.*" />
	 */
	private static final String SERVICE="service";
	/**
	 * An attribute of 'service' Element in the metadata file
	 * packages of a bundle which contain some GraniteDS dataservice classes
	 * these packages will be scanned by this parser
	 * i.e. packages="org.granite.osgi.test,org.granite.osgi.test.*"
	 */
	private static final String PROP_PACKAGES="packages";
	/**
	 * the property key in the MANFEST.MF to specify the metadata 
	 * i.e. GraniteDS-Service: GraniteDS-INF/domain-config/granite-osgi.xml
	 * Parser only scans the bundles who have the this property key presented 
	 */
	private static final String GRANITEDS_SERVICE="GraniteDS-Service";
	/**
	 * the classloader which is used to load the classes in the bundles 
	 * which will provide the dataservices
	 */
	ServiceClassLoader classLoader;
	/**
	 * EventAdmin is used to send a event to the EventHandler 
	 * 'ServicesConfig' when a bundle is changed
	 */
	ServiceTracker eventAdminTracker;
	/**
	 * bundle context of Granite OSGi bundle
	 */
	BundleContext context;
	/**
	 * a set of qualifed Granite dataservice classes which will 
	 * be registered or unregistered during the runtime
	 */
	Set<Class<?>> classes; 
	/**
	 * the metadata xml file path
	 * i.e. GraniteDS-INF/granite-osgi.xml
	 */
	String granitedsMeta;
	/**
	 * metadata processing thread 
	 */
	static DocumentBuilder documentBuilder;
	private final MetadataProcessor processorThread = new MetadataProcessor();
	/**
	 * Constructor
	 * @param context
	 */
	public ManifestMetadataParser(BundleContext context) {
		this.context = context;
	}
	
	private void setGraniteMeta(String granitedsMeta) {
		this.granitedsMeta = granitedsMeta;
	}
	
	private EventAdmin getEventAdmin(){
		return (EventAdmin) eventAdminTracker.getService();
	}
	/**
	 * broadcast service change
	 * @param eventTopic
	 */
	private void broadcastServicesChanged(String eventTopic){
		if(classes!=null && classes.size()>0){
			Dictionary<String, Object> properties = new Hashtable<String, Object>();
			properties.put(OSGIConstants.SERVICE_CLASS_SET, classes);
			EventAdmin eadmin=getEventAdmin();
			if(eadmin!=null){
				eadmin.sendEvent(new Event(eventTopic,properties));
			}else{
				if(log.isErrorEnabled())
					log.error("EventAdmin is unavailable, cannot broadcast Event!!!");
			}
		}
	}
	
	/**
	 * parse the metadata
	 * 
	 * @param bundle
	 * @param eventTopic
	 */
    private void parseMetadata(Bundle bundle,String eventTopic){
    	if(log.isInfoEnabled())
    		log.info(GRANITEDS_SERVICE+":"+granitedsMeta);
    	classLoader.setBundle(bundle);
    	DocumentBuilder builder = getDocumentBuilder();
		try {
			if (builder != null) {
				if(granitedsMeta==null || "".equals(granitedsMeta))return;
				InputStream is=bundle.getEntry(granitedsMeta).openStream();
				if(is==null)return;
				Document doc=builder.parse(is);
				Element servicesNode=(Element) doc.getElementsByTagName(SERVICES).item(0);
				NodeList services=servicesNode.getElementsByTagName(SERVICE);
				for(int i=0;i<services.getLength();i++){
					Element service= (Element) services.item(i);
					String[] servicePackages=service.getAttribute(PROP_PACKAGES).split(SEPERATOR);
					if(servicePackages!=null){
						classes.addAll(classLoader.loadClasses(servicePackages));
					}else{
						throw new RuntimeException("Invalid Service at "+i);
					}
				}
			   broadcastServicesChanged(eventTopic);
    		}
		} catch (SAXException e) {
			log.error(e, "Could not parse metadata");
		} catch (IOException e) {
			log.error(e, "Could not parse metadata");
		}
    }
    /**
     * @return DocumentBuilder
     */
    private synchronized static DocumentBuilder getDocumentBuilder() {
		try {
			if(documentBuilder==null)
			 documentBuilder= DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			log.error(e, "Could not get document builder");
		}
		//DocumentBuilder is reset to the same state as when it was created
		documentBuilder.reset();
		return documentBuilder;
	}
    /**
     * start to parse Metadata
     */
    public void start() {
    	classLoader=new ServiceClassLoader();
		eventAdminTracker=new ServiceTracker(context,EventAdmin.class.getName(),null);
		eventAdminTracker.open();
    	new Thread(processorThread).start();
    	synchronized (this) {
    		context.addBundleListener(this);// listen to any changes in bundles.
    	}
    	if(classes==null){
    		classes=new HashSet<Class<?>>();
    	}else{
    		classes.clear();
    	}
    }
    /**
     * stop Metadata Parser
     */
    public void stop(){
    	eventAdminTracker.close();
    	processorThread.stop(); // Stop the thread processing bundles.
        context.removeBundleListener(this);
        classLoader=null;
        classes=null;
    }
    /**
     * @param bundle
     * @return true if the bundle has the property key 'GraniteDS-Service' presented and with value setted
     */
    private boolean hasDataService(Bundle bundle){
    	  if(bundle==null)return false;
    	  Object gsd=bundle.getHeaders().get(GRANITEDS_SERVICE);
    	  if(gsd!=null)
    		  setGraniteMeta(gsd.toString());
    	  return (gsd!=null || "".equals(gsd));
    }
    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.BundleEvent)
     */
	public void bundleChanged(BundleEvent event) {
		Bundle bundle=event.getBundle();
		// ignore own bundle GraniteDS OSGi bundle 
		// and GraniteDS unpowered bundle(property key 'GraniteDS-Service' is not presented)
		if(context.getBundle()==bundle || !hasDataService(bundle))return;
		switch (event.getType()) {
		case BundleEvent.STARTED:
			// Put the bundle in the queue to register dataservices
			processorThread.addBundle(bundle);
			break;
		case BundleEvent.STOPPING:
			// Put the bundle in the queue to unregister dataservices
			processorThread.removeBundle(bundle);
			break;
		default:
			break;
		}
	}
	/**
	 *
	 */
	private class MetadataProcessor implements Runnable{
		private boolean hasStarted=true;
		private List<Bundle> bundles = new ArrayList<Bundle>();
		private List<Bundle> removedBundles = new ArrayList<Bundle>();
		private synchronized void addBundle(Bundle bundle){
			bundles.add(bundle);
		    notifyAll(); // Notify the thread to force the process.
		}
		private synchronized void removeBundle(Bundle bundle){
			bundles.remove(bundle);
			removedBundles.add(bundle);
			notifyAll(); // Notify the thread to force the process.
		}
		/**
		 * Stops the processor thread.
		 */
		public synchronized void stop() {
			hasStarted = false;
			bundles.clear();
			notifyAll();
		}
		public void run() {
			 while (hasStarted) {
				 Bundle bundle=null;
				 Bundle removeBundle=null;
	             synchronized (this) {
                     while (hasStarted && bundles.isEmpty() && removedBundles.isEmpty()) {
                        try {
                        	//log.info("waiting...");
                            wait();
                        } catch (InterruptedException e) {
                            // Interruption, re-check the condition
                        }
                     }
                     if (!hasStarted)
                        return; // The thread must be stopped immediately.
                    
                     // The bundle list is not empty, get the bundle.
                     // The bundle object is collected inside the synchronized block to avoid
                     // concurrent modification. However the real process is made outside the
                     // mutual exclusion area
                	 if(bundles.size()>0)
                		bundle = bundles.remove(0);
                	 if(removedBundles.size()>0)
                		removeBundle = removedBundles.remove(0);
                 }
                 if(bundle!=null){
                	if(log.isInfoEnabled())
                		log.info("Processing AddService for bundle: %s", bundle.getSymbolicName());
                	parseMetadata(bundle,OSGIConstants.TOPIC_GDS_ADD_SERVICE);
                 }
                 if(removeBundle!=null){
                	if(log.isInfoEnabled())
	                	log.info("Processing RemoveService for bundle: %s", removeBundle.getSymbolicName());
                	parseMetadata(removeBundle,OSGIConstants.TOPIC_GDS_REMOVE_SERVICE);
                 }
			 }
		}
	}
	 
}
