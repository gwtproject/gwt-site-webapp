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
import com.google.gwt.core.client.GWT;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Properties;
import com.google.gwt.query.client.js.JsUtils;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.impl.HyperlinkImpl;

import static com.google.gwt.query.client.GQuery.$;
import static com.google.gwt.query.client.GQuery.body;
import static com.google.gwt.query.client.GQuery.window;

public class GWTProjectEntryPoint implements EntryPoint {

  private static final RegExp isSameOriginRexp;
  private static final HyperlinkImpl clickHelper = GWT.create(HyperlinkImpl.class);
  private static Properties history = JsUtils.prop(window, "history");
  private static boolean isPushstateCapable = history.get("pushState") != null;

  static {
    // XHR must match all: protocol, host and port.
    // Note: in chrome this could be simpler since it has window.location.origin
    String origin = Window.Location.getProtocol() + "//" + Window.Location.getHostName();
    String port = Window.Location.getPort();
    if (port != null && port.length() > 0) {
      origin += ":" + port;
    }
    // We discard links with a different origin, hash links and protocol-agnostic urls.
    isSameOriginRexp = RegExp.compile("^" + origin + "|^(?!(https?|mailto|ftp|javascript):|#|//).+", "i");
  }

  private String currentPage;

  @Override
  public void onModuleLoad() {
    enhanceMenu();

    openMenu();

    maybeBindPopState();

    currentPage = Window.Location.getPath();
  }

  private void openMenu() {
    $("#gwt-toc a.selected").removeClass("selected");

    String path = Window.Location.getPath();

    $("#gwt-toc a[ahref$='" + path + "']")
        .addClass("selected")
        .parentsUntil("#gwt-toc")
        .filter("li.folder")
        .addClass("open")
        .children("ul")
        .slideDown(200);
  }

  private void enhanceMenu() {
    $("li.folder > a").click(new Function() {
      @Override
      public boolean f(Event e) {
        toggleMenu($(e).parent());
        return false;
      }
    });

    $("#gwt-toc li.folder > ul").hide();

    $("a", body).live(Event.ONCLICK, new Function() {
      @Override
      public boolean f(Event e) {
        String href = getHref($(e));
        if (shouldEnhanceClick(e) && isPushstateCapable && isSameOriginRexp.test(href)) {
          loadPage(href);
          return false;
        }
        return true;
      }
    });
  }

  private boolean shouldEnhanceClick(Event e) {
    GQuery link = $(e);

    // Is it a normal click (not ctrl/cmd/shift/right/middle click) ?
    if (!clickHelper.handleAsClick(e)) {
      return false;
    }

    // do not load links that are marked as full page reload
    if ("true".equals(link.attr("data-full-load"))) {
      return false;
    }

    // do not load javadoc async
    if (getHref(link).startsWith("/javadoc/")) {
      return false;
    }

    return true;
  }

  /**
   * Sometimes the link src comes in the 'ahref' attribute
   */
  private String getHref(GQuery link) {
    return  JsUtils.or(link.attr("ahref"), link.attr("href"));
  }

  private void toggleMenu(GQuery menu) {
    menu.toggleClass("open")
        .children("ul")
        .slideToggle(200);
  }

  private void loadPage(String pageUrl) {
    if (pageUrl != null) {
      JsUtils.runJavascriptFunction(history, "pushState", null, null, pageUrl);
    }

    String path = Window.Location.getPath();
    final String hash = Window.Location.getHash();

    if (!path.equals(currentPage)) {
      currentPage = path;

      $("#gwt-content").load(path + " #gwt-content > div", null,
          new Function() {
            @Override
            public void f() {
              scrollTo(hash);
              openMenu();
            }
          });

    } else {
      scrollTo(hash);
    }
  }

  private void scrollTo(String hash) {
    if (hash == null || hash.length() == 0) {
      Window.scrollTo(0, 0);
    } else {
      GQuery anchor = $(hash);
      if (anchor.isEmpty()) {
        anchor = $("[name='" + hash.substring(1) + "']");
      }

      anchor.scrollIntoView();
    }
  }

  private void maybeBindPopState() {
    if (!isPushstateCapable) {
      return;
    }

    // Note: gQuery will support $(window).on("popstate", function) in future releases.
    window.<Properties>cast().setFunction("onpopstate", new Function() {
      @Override
      public void f() {
        loadPage(null);
      }
    });
  }
}
