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

import static com.google.gwt.query.client.GQuery.*;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Properties;
import com.google.gwt.query.client.js.JsUtils;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;

public class GWTProjectEntryPoint implements EntryPoint {

  private static final RegExp isSameOriginRexp;
  static {
    // XHR must match all: protocol, host and port.
    // Note: in chrome this could be simpler since it has window.location.origin
    String origin = Window.Location.getProtocol() + "//" +  Window.Location.getHostName();
    String port = Window.Location.getPort();
    if (port != null && port.length() > 0) {
      origin += ":" + port;
    }
    // We discard links with a different origin, and hash links.
    isSameOriginRexp = RegExp.compile("^" + origin + "|^(?!(https?|mailto|ftp|javascript):|#).+", "i");
  }

  private String currentPage;
  private static Properties history = JsUtils.prop(window, "history");
  private static boolean isPushstateCapable = $(history).prop("pushState") != null;

  @Override
  public void onModuleLoad() {
    enhanceMenu();

    openMenu(Window.Location.getPath());

    bindPopState();
  }

  private void openMenu(String path) {
    $("#gwt-toc a.selected").removeClass("selected");

    if (path != null && path.length() > 0) {
      $("#gwt-toc a[ahref$='" + path + "']")
        .addClass("selected")
        .parentsUntil("#gwt-toc")
        .filter("li.folder")
        .addClass("open")
        .children("ul")
        .slideDown(200);
    }
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
        // Sometimes the link src comes in the 'ahref' attribute
        String href = JsUtils.or($(e).attr("ahref"), $(e).attr("href"));

        // if loadPage returns true we don't stop propagation nor default
        return loadPage(href, true);
      }
    });
  }

  private void toggleMenu(GQuery menu) {
    menu.toggleClass("open")
        .children("ul")
        .slideToggle(200);
  }

  private boolean loadPage(final String pageUrl, final boolean pushState) {
    if (!isSameOriginRexp.test(pageUrl)) {
      return true;
    }

    int hashIndex = pageUrl.indexOf('#');
    final String path = hashIndex != -1 ? pageUrl.substring(0, hashIndex)
        : pageUrl;
    final String hash = hashIndex != -1 ? pageUrl.substring(hashIndex)
        : null;

    if (isPushstateCapable && !path.equals(currentPage)) {
      currentPage = path;

      if (pushState) {
        JsUtils.runJavascriptFunction(history, "pushState", null, null, pageUrl);
      }

      $("#gwt-content").load(pageUrl + " #gwt-content > div", null,
          new Function() {
            @Override
            public void f() {
              if (hash != null) {
                $(hash).scrollIntoView();
              }
              openMenu(path);
            }
          });

    } else if (hash != null) {
      $(hash).scrollIntoView();
    }

    return !path.equals(currentPage);
  }

  private void bindPopState() {
    if (isPushstateCapable) {
      // Note: gQuery will support $(window).on("popstate", function) in future releases.
      window.<Properties>cast().setFunction("onpopstate", new Function() {
        public void f() {
          loadPage(Window.Location.getPath(), false);
        };
      });
    }
  }
}
