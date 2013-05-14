/*
 * Copyright 2013 Daniel Kurka
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
package com.google.gwt.site.webapp.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.user.client.Window;

import static com.google.gwt.query.client.GQuery.$;

public class GWTProjectEntryPoint implements EntryPoint {
	private String currentPage;

	@Override
	public void onModuleLoad() {	
		enhanceMenu();
    
    openMenu();

    if (supportsHtml5History()) {
      initOnPopState();
    }
	}

  private void openMenu() {
    String href = Window.Location.getPath();

    if (href != null && href.length() > 0) {
      $("#gwt-toc a[href$='" + href + "']").addClass("selected")
          .parentsUntil("#gwt-toc").filter("li.folder").addClass("open")
          .children("ul").slideDown(200);
    }
  }

  private void enhanceMenu() {
		// TODO : arrow should be clickable
		$("li.folder > a").click(new Function() {
			@Override
			public void f(Element e) {
				toggleMenu($(e).parent());
			}
		});
		
		$("#gwt-toc li.folder > ul").css("display", "none");

		$("#gwt-toc a").click(new Function() {
      @Override
      public void f(Element e) {
        loadPage(e.getAttribute("href"), true);

        selectItem(e);

        getEvent().preventDefault();
      }
    });
	}

	private void toggleMenu(GQuery menu) {
		if (menu.hasClass("open")) {
			menu.removeClass("open");
		} else {
			menu.addClass("open");
		}

		menu.children("ul").slideToggle(200);
	}

	private void loadPage(String pageUrl, boolean pushState) {
		if (pageUrl.equals(currentPage)) {
			return;
		}

		if (supportsHtml5History()) {
			currentPage = pageUrl;
			

			$("#gwt-content").load(pageUrl + " #gwt-content > div");

      if (pushState) {
			  pushState(pageUrl);
      }

    }
	}

  private void onPopState() {
    loadPage(Window.Location.getPath(), false);
  }

  private void selectItem(Element item) {
    $("#gwt-toc a.selected").removeClass("selected");
    item.addClassName("selected");
  }

	private native boolean pushState(String url) /*-{
		$wnd.history.pushState(null, null, url);
	}-*/;

	private native boolean supportsHtml5History()/*-{
		return $wnd.history && $wnd.history.pushState;
	}-*/;

  private native void initOnPopState() /*-{
      var that = this;
      $wnd.onpopstate = $entry(function(e) {
          that.@com.google.gwt.site.webapp.client.GWTProjectEntryPoint::onPopState()();
      });
  }-*/;
}
