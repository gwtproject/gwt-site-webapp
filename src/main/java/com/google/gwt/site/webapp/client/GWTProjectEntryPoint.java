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

		//maybeLoadPage();
	}

	private void maybeLoadPage() {
		String currentHash = Window.Location.getHash();

		if (currentHash != null && currentHash.length() > 0) {
			if (currentHash.startsWith("#")) {
				currentHash = currentHash.substring(1);
			}

			loadPage(currentHash);
			// TODO open the menu to the page
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
				loadPage(e.getAttribute("href"));

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

	private void loadPage(String pageUrl) {
		if (pageUrl.equals(currentPage)) {
			return;
		}

		if (supportsHtml5Histroy()) {
			currentPage = pageUrl;
			

			$("#gwt-content").load(pageUrl + " #gwt-content > div");

			pushState(pageUrl);
		} else {
			Window.Location.replace(pageUrl);
		}

	}

	private native boolean pushState(String url) /*-{
		$wnd.history.pushState(null, null, url);
	}-*/;

	private native boolean supportsHtml5Histroy()/*-{
		return $wnd.history && $wnd.history.pushState;
	}-*/;

}
