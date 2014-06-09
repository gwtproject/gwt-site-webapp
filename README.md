
- gwt-site-webapp
 This project includes the server an client code of the GWT site
 It does not include the site content which is in the gwt-site project
 nor the utilities for uploading the documentation to GAE Servers which
 are in the gwt-site-uploader project.

- Requirements
 [Apache Maven](http://maven.apache.org) 3.0 or greater, and JDK 6+ in order to run.

- Before building the project you may need gwt-site packaged file.
  Go to the gwt-site folder and run:
  $ mvn install

- To build and check the app, run
  $ mvn package

  Now you can point your browser to the target/www folder or you can deploy
  its content in any web-server.

- Building will run the tests, but to explicitly run tests you can use the test target
  $ mvn test

- Develop and Deploy in GAE
  This app is thought to be run in GAE servers, so it comes with all dependencies for it.

  To start the app, use the [App Engine Maven Plugin](http://code.google.com/p/appengine-maven-plugin/)
  that is already included in this project. Just run the command:
  $ mvn appengine:devserver

  You might upload the gwt stuff to the dev server, just open another terminal, 
  go to the gwt-site-uploader folder project and run:
  $ sh upload.sh localhost

  For further information about GAE, consult the [Java App Engine](https://developers.google.com/appengine/docs/java/overview) documentation.
  To see all the available goals for the App Engine plugin, run
  $  mvn help:describe -Dplugin=appengine
