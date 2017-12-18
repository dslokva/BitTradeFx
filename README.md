BitTradeFx
==============

Hello everybody!

This is a simple Vaadin application that show you Cryptocurrency/USD rates at 4 coin markets.


Workflow
========

Requires a Servlet 3.0 container to run.

To compile the entire project, run "mvn install".

To run the application, run "mvn jetty:run" and open http://localhost:8080/#!main .


Developing a theme using the runtime compiler
-------------------------

When developing the theme, Vaadin can be configured to compile the SASS based
theme at runtime in the server. This way you can just modify the scss files in
your IDE and reload the browser to see changes.

To use the runtime compilation, open pom.xml and comment out the compile-theme 
goal from vaadin-maven-plugin configuration. To remove a possibly existing 
pre-compiled theme, run "mvn clean package" once.

When using the runtime compiler, running the application in the "run" mode 
(rather than in "debug" mode) can speed up consecutive theme compilations
significantly.

It is highly recommended to disable runtime compilation for production WAR files.
