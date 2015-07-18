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
package com.google.gwt.site.webapp.server.resources;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Text;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ContentServlet extends HttpServlet {

  /**
	 * 
	 */
  private static final long serialVersionUID = 458719890608890896L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {

    String fullPath = normalizePath(req.getRequestURI());

    Entity e = getResourceByKey(fullPath);

    if (e == null) {
      // temporary try to find the resource with .html appended
      // due to redirects from developers.google.com
      if (!fullPath.endsWith("/")) {
        fullPath = fullPath + ".html";
        e = getResourceByKey(fullPath);
        if (e != null) {
          // redirect so we use correct urls!
          resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
          resp.setHeader("Location", "/" + fullPath);
          return;
        }
      }
      resp.sendError(404);
      return;
    }
    String html = ((Text) e.getProperty("html")).getValue();

    setContentTypeByFileEnding(resp, fullPath);
    resp.setCharacterEncoding("utf-8");

    if (isBinaryFile(fullPath)) {
      byte[] decodeBase64 =
          org.apache.commons.codec.binary.Base64.decodeBase64(html.getBytes("UTF-8"));
      resp.getOutputStream().write(decodeBase64);

    } else {
      resp.getWriter().write(html);
    }
  }

  private Entity getResourceByKey(String key) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key keyInstance = KeyFactory.createKey("DocModel", key);
    Query query = new Query("DocModel", keyInstance);
    return datastore.prepare(query).asSingleEntity();
  }

  private void setContentTypeByFileEnding(HttpServletResponse resp, String fullPath) {

    if (fullPath.endsWith(".html")) {
      resp.setContentType("text/html");
      return;
    }

    if (fullPath.endsWith(".js")) {
      resp.setContentType("application/javascript");
      return;
    }
    if (fullPath.endsWith(".css")) {
      resp.setContentType("text/css");
      return;
    }

    if (fullPath.endsWith(".png")) {
      resp.setContentType("image/png");
      return;
    }

    if (fullPath.endsWith(".jpg") || fullPath.endsWith(".jpeg")) {
      resp.setContentType("image/jpg");
      return;
    }

    if (fullPath.endsWith(".gif")) {
      resp.setContentType("image/gif");
      return;
    }

    if (fullPath.endsWith("/package-list")) {
      resp.setContentType("text/plain");
      return;
    }

    if (fullPath.endsWith(".dtd")) {
      resp.setContentType("application/xml-dtd");
      return;
    }

    if (fullPath.endsWith(".woff") || fullPath.endsWith(".woff2")) {
      resp.setContentType("application/font-woff");
      return;
    }

    if (fullPath.endsWith(".eot")) {
      resp.setContentType("application/vnd.ms-fontobject");
      return;
    }

    if (fullPath.endsWith(".ttf")) {
      resp.setContentType("application/x-font-ttf");
      return;
    }

    if (fullPath.endsWith(".svg")) {
      resp.setContentType("image/svg+xml");
      return;
    }

    // TODO: what is a good default value?
  }

  private boolean isBinaryFile(String path) {
    return path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg")
        || path.endsWith(".gif") || path.endsWith(".woff") || path.endsWith(".eot")
        || path.endsWith(".ttf") || path.endsWith(".woff2");
  }

  private String normalizePath(String fullPath) {

    fullPath = fullPath.substring("/".length(), fullPath.length());

    if ("".equals(fullPath) || fullPath.endsWith("/")) {
      fullPath += "index.html";
    }

    return fullPath;
  }
}
