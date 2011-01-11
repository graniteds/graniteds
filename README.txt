===============================================================================
Granite Data Services (http://www.graniteds.org)
===============================================================================


* What is in this Distribution:
  -----------------------------------------------------------------------------

  The "build" directory contains all GraniteDS jar and swc. You'll need part of
  them in order to build and deploy GDS applications (granite.jar is the only
  one that's mandatory whatever technology you use).
  
  The "examples" directory contains various sample applications that will help
  you starting with GDS. Refer to examples/README.txt for detailed instructions.
  
  The "as3" directory contains GDS ActionScript3 code source, divided in
  "essentials" (classes that must be included in your swf by using the
  -compiler.include-libraries mxmlc option) and "framework" (all other classes,
  included in your swf if required).
  
  The "maven" directory contains pom.xml files for each GDS library, as well as
  maven-deploy.xml script to upload GDS artifacts.
  
  The "lib" directory contains all third-party jars required for GDS Java
  compilation.
  
  The "doc" directory contains API documentation for both GDS Java and
  ActionsScript3 code.
  
  All other directories contains GDS Java sources. 
  
  NOTE: provided SWCs are all compiled with Flex SDK Version 3.3.0 build 4852.


* General Requirements:
  -----------------------------------------------------------------------------

  JDK 5+, Flex 3+ SDK. Eclipse 3.2+ isn't required but GraniteDS distribution
  and examples are packaged, for convenience, as Eclipse projects.


* Eclipse Installation (GraniteDS project and all examples):
  -----------------------------------------------------------------------------

  Right click in your Eclipse "Package Explorer", choose "Import...", then
  "General" -> "Existing Projects into Workspace", choose "Select archive file",
  browse to graniteds-***.zip and click on "Finish". You should now see the
  "graniteds" main project as well as all sample projects in your package
  explorer view.


* Sources Build:
  -----------------------------------------------------------------------------

  Open env.properties (WITH A SIMPLE TEXT EDITOR) and adjust at least the first
  variable (FLEX_HOME).

  Open an Ant view ("Window" -> "Show View" -> "Ant"), drag an drop the main
  build.xml file from the "Package Explorer" view to the "Ant" view, expand
  "graniteds" node and double click on "build.jar.swc".

* Examples Build:
  -----------------------------------------------------------------------------

  Please refer to examples/README.txt file.
  