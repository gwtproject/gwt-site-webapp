/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.gwt.site.webapp.server.guice;

import com.google.apphosting.utils.remoteapi.RemoteApiServlet;
import com.google.gwt.site.webapp.server.resources.ContentServlet;
import com.google.gwt.site.webapp.server.resources.HashServlet;
import com.google.inject.Singleton;
import com.google.inject.servlet.ServletModule;

public class MyServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    //docs
    bind(RemoteApiServlet.class).in(Singleton.class);
    bind(ContentServlet.class).in(Singleton.class);
    bind(HashServlet.class).in(Singleton.class);

    serve("/remote_api").with( RemoteApiServlet.class);
    serve("/hash").with( HashServlet.class);
    serveRegex("/(?!_ah).*").with( ContentServlet.class);
  }
}
