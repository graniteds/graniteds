===============================================================================
Granite Data Services Examples (http://www.graniteds.org)
===============================================================================


* Requirements:
  -----------------------------------------------------------------------------

  JDK 5+, Flex 3+ SDK and JBoss-4.2.2+. Eclipse 3.2+ isn't required but all
  examples are packaged, for convenience, as Eclipse projects.

  IMPORTANT: for the Chat sample, you MUST install APR (Apache Portable Runtime):
  download and install it from http://tomcat.apache.org/tomcat-6.0-doc/apr.html.
  This is required for both a standalone Tomcat or a JBoss/Tomcat server!
  
 
* Build and Deployment Configuration:
  -----------------------------------------------------------------------------

  #1. Edit the examples/env.properties file (WITH A SIMPLE TEXT EDITOR) and adjust
  the first 2 variables (FLEX_HOME and SERVER_HOME).
  
  #2. Follow these steps in order to make authentication work:
  
  Copy users.properties and roles.properties into the
  <JBOSS_HOME>/server/default/conf/props directory (backup first existing
  files).

  Edit <JBOSS_HOME>/server/default/conf/login-config.xml and replace (at the
  end of the file):

  <application-policy name="other">
    <authentication>
      <login-module code="org.jboss.security.auth.spi.UsersRolesLoginModule"
        flag="required" />
    </authentication>
  </application-policy>

  by:

  <application-policy name="other">
    <authentication>
      <login-module code="org.jboss.security.auth.spi.UsersRolesLoginModule"
        flag="required">
        <module-option name="usersProperties">props/users.properties</module-option>
        <module-option name="rolesProperties">props/roles.properties</module-option>
      </login-module>
    </authentication>
  </application-policy>
  
  #3. For the Chat sample, you MUST COMMENT OUT the "CommonHeadersFilter" section in
  jboss-4.2.2+.GA/server/default/deploy/jboss-web.deployer/conf/web.xml:

   <!--
   <filter>
      <filter-name>CommonHeadersFilter</filter-name>
      <filter-class>org.jboss.web.tomcat.filters.ReplyHeaderFilter</filter-class>
      <init-param>
         <param-name>X-Powered-By</param-name>
         <param-value>Servlet 2.4; JBoss-4.2.3.GA [...]</param-value>
      </init-param>
   </filter>

   <filter-mapping>
      <filter-name>CommonHeadersFilter</filter-name>
      <url-pattern>/*</url-pattern>
   </filter-mapping>
   -->


* Running the Examples in JBoss 5
  -----------------------------------------------------------------------------

  All examples that do not use the Gravity push functionality should work without
  modification in JBoss 5+.
  For all examples using Gravity (chat and all Tide examples), it's necessary to change
  the Gravity servlet implementation in web.xml because the JBossWeb version included in JBoss 5 
  does not support the Tomcat CometProcessor interface :
  
  <servlet>
      <servlet-name>GravityServlet</servlet-name>
      <servlet-class>org.granite.gravity.jbossweb.GravityJBossWebServlet</servlet-class>
      <load-on-startup>1</load-on-startup>
  </servlet>  
  

* Eclipse Examples Installation:
  -----------------------------------------------------------------------------

  If you followed the instructions in the main README.txt file (import GDS
  archive file), you should have already all example projects installed under
  Eclipse.

  If not, right click in your Eclipse Package Explorer, choose "Import...", then
  "General" -> "Existing Projects into Workspace", choose "Select root directory",
  browse to the examples directory and select the samples you are interested in.


* Build & Deployment:
  -----------------------------------------------------------------------------

  From Eclipse, open an Ant view ("Window" -> "Show View" -> "Ant"), drag an
  drop the build.xml file from each project to the Ant view and select the
  "deploy" target.
  
  Alternatively, you may build and deploy the samples from the command line (refer
  to the Ant command line manual).
  
  WARNING: the main examples/build.xml file isn't intended to be used standalone! Use
  only the examples/<example>/build.xml files.

  Start JBoss and point your browser to one of these urls:

    http://localhost:8080/graniteds-chat
    http://localhost:8080/graniteds-ejb3
    http://localhost:8080/graniteds-guice
    http://localhost:8080/graniteds-pojo
    http://localhost:8080/graniteds-spring
    http://localhost:8080/graniteds-tide-ejb3
    http://localhost:8080/graniteds-tide-seam
    http://localhost:8080/graniteds-tide-spring
    http://localhost:8080/seam-flexbooking
    
    
* Alternative configurations:
  -----------------------------------------------------------------------------
  
  Some of the example projects provide example configurations for other combinations 
  of application servers and persistence providers. In this case, just set the SERVER_HOME
  variable in env.properties to the deployment directory of the target server and comment out 
  the corresponding section of the project build.xml.
  