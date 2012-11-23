===============================================================================
Granite Data Services (http://www.graniteds.org)
===============================================================================


* What is in this Distribution:
  -----------------------------------------------------------------------------

  The "libraries" directory contains all GraniteDS jars and swcs. You'll need part of
  them in order to build and deploy GDS applications.
  The Java server libraries are in "libraries/server", and the client libraries for 
  Flex and Java/JavaFX are in "libraries/flex" and "libraries/java-client". 
  
  - The libs required on the server depend on the frameworks used. Only granite.jar
    is mandatory.
  - For Flex until 4.1, you will need granite.swc and granite-essentials.swc
  - For Flex 4.5+, you will need granite-flex45.swc and granite-essentials.swc
  - For a simple Java client, you will need granite-client.jar and granite-java-client.jar
  - For JavaFX, you will need granite-client.jar, granite-java-client.jar 
    and granite-javafx-client.jar
  - The Java client libraries have runtime dependencies on external libraries, see
    the documentation or examples.
  
  The "sources" directory contains the source jars for all GraniteDS libraries.
  You can attach them in your project classpath in Eclipse to be able to debug inside 
  the GraniteDS code. 
  
  The "docs" directory contains the JavaDoc / ASDoc API documentation for all 
  GDS server, and client Java and Flex libraries.
  The "docs/reference" contains the reference guides for Flex and Java/JavaFX clients.
  
  The "samples" directory contains a zip archive with some simple projects that will help
  you starting with GDS. You can import these projects in Eclipse with 
  Import... > General/Existing projects into workspace > Select archive file/Browse...
  The examples are meant to be deployed on JBoss 5. You will have to define the jboss.home
  and flex.home variables in build.properties and build the project with Ant.
  
  The "tools" directory contains granite-generator.jar which contains the GraniteDS
  Ant tasks. 
  
  The "maven" directory contains pom.xml files for each GDS library, as well as
  maven-deploy.xml script to upload GDS artifacts.
  
  NOTE: provided SWCs are compiled with Flex SDK Version 3.6.0 build 16995,
        except granite-flex45.swc which is built with Flex SDK version 4.5.1 build 21328.


* Build from sources:
  -----------------------------------------------------------------------------

  All projects are available on github :
  - Main project with server libraries and Flex client libraries : git://github.com/graniteds/graniteds.git
  - Java client libraries : git://github.com/graniteds/graniteds_java_client.git
  - Eclipse plugin : git://github.com/graniteds/graniteds_builder.git
  
  Open env.properties (WITH A SIMPLE TEXT EDITOR) and adjust at least the first
  variable (FLEX_HOME).

  Each project includes an Ant build.xml file that can build the project.
  