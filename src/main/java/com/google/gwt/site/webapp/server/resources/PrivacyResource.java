package com.google.gwt.site.webapp.server.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.sun.jersey.api.view.Viewable;
@Path("/privacy")
public class PrivacyResource {
	
	  @GET
	  @Produces("text/html")
	  public Viewable getHTML() {
	    return new Viewable("/privacy", null);
	  }
}
