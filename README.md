
- **gwt-site-webapp**.
 This project includes the server an client code of the GWT site
 It does not include the site content which is in the gwt-site project
 nor the utilities for uploading the documentation to GAE Servers which
 are in the gwt-site-uploader project.

- **Requirements**
 [Apache Maven](http://maven.apache.org) 3.0 or greater, and JDK 7+ in order to run.

- Before building the project you may need **gwt-site** packaged file.
  Go to the `gwt-site` folder and run:

        $ cd [...]/gwt-site
        $ mvn install

- To **build** and check the app, run

        $ cd [...]/gwt-site-webapp
        $ mvn package

  Now you can point your browser to the `target/www` folder or you can serve
  its content with any web-server (i.e [serve](https://www.npmjs.com/package/serve)).
  Notice that the second way is needed if you want to test site navigation via Ajax.

- Building will run the **tests**, but to explicitly run them you can use the test target

        $ mvn test

- **Develop and Deploy**. This app is thought to be run in GAE servers, so it comes with all dependencies for it.

  * To start the app using the [App Engine Maven Plugin](http://code.google.com/p/appengine-maven-plugin/) included in this project, just run the command:

            $ mvn appengine:devserver

  * You might upload the gwt stuff to the local developer server, just open another terminal,
  go to the `gwt-site-uploader` folder project and run:

            $ cd [...]/gwt-site-uploader
            $ sh upload.sh localhost

  * If you wanted to deploy to production, you might change the `<application>` section in the
  `src/main/webapp/WEB-INF/appengine-web.xml` to point to the appropriate GAE instance and
  then deploy to Google servers running:

            $ mvn appengine:update -DgaeAccount=your_google_account

  * For further information about GAE, consult the [Java App Engine](https://developers.google.com/appengine/docs/java/overview) documentation.

      To see all the available goals for the App Engine plugin, run

            $ mvn help:describe -Dplugin=appengine
