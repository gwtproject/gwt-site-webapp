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
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;

import static com.google.gwt.query.client.GQuery.$;
import static com.google.gwt.query.client.GQuery.body;

public class GWTProjectEntryPoint implements EntryPoint {
	private static final RegExp INTERNAL_URL_REGEX;
	private static final RegExp NOT_RELATIVE_URL_REGEX;

	static {
		String domainUrl = Window.Location.getHostName();
		String port = Window.Location.getPort();

		if (port != null && port.length() > 0) {
			domainUrl += ":" + port;
		}

		INTERNAL_URL_REGEX = RegExp.compile("^(https?)\\:\\/\\/" + domainUrl
				+ "\\/.*$");
		NOT_RELATIVE_URL_REGEX = RegExp
				.compile("^(https?)|(mailto)|(ftp)|(javascript)\\:.*$");
	}

	private String currentPage;

	@Override
	public void onModuleLoad() {
		enhanceMenu();

		openMenu(Window.Location.getPath());

		if (supportsHtml5History()) {
			initOnPopState();
		}
	}

	private void openMenu(String path) {
		$("#gwt-toc a.selected").removeClass("selected");

		if (path != null && path.length() > 0) {
			$("#gwt-toc a[ahref$='" + path + "']").addClass("selected")
					.parentsUntil("#gwt-toc").filter("li.folder")
					.addClass("open").children("ul").slideDown(200);
		}
	}

	private void enhanceMenu() {
		// TODO : arrow should be clickable
		$("li.folder > a").click(new Function() {
			@Override
			public void f(Element e) {
				toggleMenu($(e).parent());

				getEvent().stopPropagation();
				getEvent().preventDefault();
			}
		});

		$("#gwt-toc li.folder > ul").css("display", "none");

		$("a", body).live(Event.ONCLICK, new Function() {
			@Override
			public boolean f(Event event) {
				String href = null;
				if (getElement().hasAttribute("ahref")) {
					href = getElement().getAttribute("ahref");
				} else {
					href = getElement().getAttribute("href");
				}

				if (isInternalNavigation(href)) {
					return loadPage(href, true);
				}

				return true;
			}
		});
	}

	private boolean isInternalNavigation(String href) {
		// TODO mix the three last conditions in one regex !!
		return href != null
				&& href.length() > 0
				&& (!href.startsWith("#") && !NOT_RELATIVE_URL_REGEX.test(href) || INTERNAL_URL_REGEX
						.test(href));
	}

	private void toggleMenu(GQuery menu) {
		if (menu.hasClass("open")) {
			menu.removeClass("open");
		} else {
			menu.addClass("open");
		}

		menu.children("ul").slideToggle(200);
	}

	private boolean loadPage(final String pageUrl, final boolean pushState) {
		int hashIndex = pageUrl.indexOf('#');
		final String path = hashIndex != -1 ? pageUrl.substring(0, hashIndex)
				: pageUrl;
		final String hash = hashIndex != -1 ? pageUrl.substring(hashIndex)
				: null;

		if (supportsHtml5History() && !path.equals(currentPage)) {
			currentPage = path;

            if (pushState) {
                pushState(pageUrl);
            }

			$("#gwt-content").load(pageUrl + " #gwt-content > div", null,
					new Function() {
						@Override
						public void f() {
							if (hash != null) {
								scrollTo(hash);
							}
							openMenu(path);
						}
					});

			return false;
		} else if (hash != null) {
			scrollTo(hash);
		}

		return !path.equals(currentPage);
	}

	private void scrollTo(String hash) {
		Window.scrollTo(Window.getScrollLeft(), $(hash).offset().top);
	}

	private void onPopState() {
		loadPage(Window.Location.getPath(), false);
	}

	private native void pushState(String url) /*-{
		$wnd.history.pushState(null, null, url);
	}-*/;

	private native boolean supportsHtml5History()/*-{
		return !!($wnd.history && $wnd.history.pushState);
	}-*/;

	private native void initOnPopState() /*-{
		var that = this;
		$wnd.onpopstate = $entry(function(e) {
			that.@com.google.gwt.site.webapp.client.GWTProjectEntryPoint::onPopState()();
		});
	}-*/;
}
