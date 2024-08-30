# gwt-site-webapp
 This project includes the server an client code of the GWT site
 It does not include the site content which is in the gwt-site project
 nor the utilities for uploading the documentation to GAE Servers which
 are in the gwt-site-uploader project.

## Requirements
 [Apache Maven](http://maven.apache.org) 3.0 or greater, and JDK 7+ in order to run.

- Before building the project you may need **gwt-site** packaged file.
  Go to the `gwt-site` folder and run:

        $ cd [...]/gwt-site
        $ mvn install

- To **build** and check the app, run

        $ cd [...]/gwt-site-webapp
        $ mvn verify -P full-site

  Now you can point your browser to the `target/www` folder, or you can serve
  its content with any web-server (i.e [serve](https://www.npmjs.com/package/serve)).
  Notice that the second way is needed if you want to test site navigation via Ajax.

- Building will run the **tests**, but to explicitly run them you can use the test target

        $ mvn test

# Develop and Deploy

Note that as configured, sourcemaps will not work locally - they must be deployed to gwtproject.org
because the generated JS will attempt to load them from an absolute URL.

  * The `target/gwt-site-webapp-<version>.war` file (and the `target/gwt-site-webapp-<version>`
  directory) contains the generated JavaScript and sourcemaps, and can be deployed along with the
  generated site content.
  * The `target/www` directory contains the generated JavaScript, sourcemaps (and Java sources),
  and the generated HTML from gwt-site itself, and is suitable for deployment directly to a
  server.
