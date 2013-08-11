package com.google.gwt.site.webapp.server.resources;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HashServlet extends HttpServlet {

  private static final long serialVersionUID = -8648249883829261848L;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {

    int count;
    try {
      count = Integer.parseInt(req.getParameter("count"));
    } catch (NumberFormatException e) {
      throw new ServletException(e);
    }

    if (count < 0) {
      throw new ServletException("invalid value count " + count);
    }

    JSONObject root = new JSONObject();

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();

    PreparedQuery pq = ds.prepare(new Query("DocHash"));

    List<Entity> asList = pq.asList(FetchOptions.Builder.withOffset(count).limit(1000));
    JSONArray array = new JSONArray();

    try {
      root.put("hashes", array);
      for (Entity entity : asList) {
        count++;
        String key = entity.getKey().getName();
        String hash = (String) entity.getProperty("hash");

        JSONObject docHash = new JSONObject();
        docHash.put("key", key);
        docHash.put("hash", hash);
        array.put(docHash);
      }

      resp.getWriter().write(root.toString());
    } catch (JSONException e) {
      throw new ServletException(e);
    }
  }
}
