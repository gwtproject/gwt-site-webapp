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

import static com.google.gwt.query.client.GQuery.$;
import static com.google.gwt.query.client.GQuery.body;
import static com.google.gwt.query.client.GQuery.window;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Properties;
import com.google.gwt.query.client.js.JsUtils;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.impl.HyperlinkImpl;

public class GWTProjectEntryPoint implements EntryPoint {

  private static final int ANIMATION_TIME = 200;

  private static final HyperlinkImpl clickHelper = GWT.create(HyperlinkImpl.class);

  private static Properties history = JsUtils.prop(window, "history");

  // Visible for testing
  // The absolute path to the url root (http://gwtproject.com)
  static String origin = GWT.getModuleBaseForStaticFiles()
      .replaceFirst("^(\\w+://.+?)/.*", "$1").toLowerCase();

  // Visible for testing
  // We discard links with different origin, hashes, starting with protocol, javadocs, and media links.
  static final RegExp isSameOriginRexp = RegExp.compile("^" + origin
      + "|^(?!(#|[a-z#]+:))(?!.*(|/)javadoc/)(?!.*\\.(jpe?g|png|mpe?g|mp[34]|avi)$)", "i");

  private static boolean isPushstateCapable = history.get("pushState") != null;
  private static boolean ajaxEnabled = isPushstateCapable && origin.startsWith("http");
  private static String currentPage = Window.Location.getPath();

  @Override
  public void onModuleLoad() {
    enhancePage();
    $("#gwt-toc li ul").hide();
    openMenu();
  }

  /*
   * Open the branch and select the item corresponding to the current url.
   */
  private void openMenu() {
    GQuery item = $("#gwt-toc a[href='" + Window.Location.getPath() + "']").eq(0);

    // Only collapse unrelated entries in mobile
    if ($("#nav-mobile").isVisible()) {
       hideUnrelatedBranches(item);
    }

    showBranch(item);

    $("#gwt-toc a.selected").removeClass("selected");
    item.addClass("selected");

    // Change the page title for easy bookmarking
    $("title").text("[GWT] " + item.text());
  }

  /*
   * Enhance the page adding handlers and replacing relative by absolute urls
   */
  private void enhancePage() {

    // We add a span with the +/- icon so as the click area is well defined
    // this span is not rendered in server side because it is only needed
    // for enhancing the page.
    GQuery parentItems = $("#gwt-toc li").has("ul").prepend("<span/>");

    // Toggle the branch when clicking on the arrow or anchor without content
    $(parentItems).children("span, a[href='#']").on("click", new Function() {
      @Override
      public boolean f(Event e) {
        toggleMenu($(e).parent());
        return false;
      }
    });

    // Replace relative paths in anchors by absolute ones
    // exclude all anchors in the content area.
    $("a").not($("#gwt-content a")).each(new Function() {
      @Override
      public void f(Element e) {
        GQuery link = $(e);
        if (shouldEnhanceLink(link)) {
          // No need to make complicated things for computing
          // the absolute path: anchor.pathname is the way
          link.attr("href", link.prop("pathname"));
        }
      }
    });

    // In mobile have a link for opening/closing the menu
    $("#nav-mobile").on("click", new Function() {
      @Override
      public void f() {
        $("#gwt-toc").toggleClass("show");
      }
    });

    // Do not continue enhancing if Ajax is disabled
    if (!ajaxEnabled) {
      // Select current item from the URL info
      loadPage(null);
      return;
    }

    // Use Ajax instead of default behaviour
    $(body).on("click", "a", new Function() {
      @Override
      public boolean f(Event e) {
        if (shouldEnhanceLink($(e)) &&
            // Is it a normal click (not ctrl/cmd/shift/right/middle click) ?
            clickHelper.handleAsClick(e)) {

          // In mobile, if menu is visible, close it
          $("#gwt-toc.show").removeClass("show");

          // Load the page using Ajax
          loadPage($(e));
          return false;
        }
        return true;
      }
    });

    // Select the TOC item when URL changes
    $(window).on("popstate", new Function() {
      @Override
      public void f() {
        loadPage(null);
      }
    });
  }

  private boolean shouldEnhanceLink(GQuery link) {
    return
      // Enhance only local links
      isSameOriginRexp.test(link.attr("href")) &&
      // Do not load links that are marked as full page reload
      !Boolean.parseBoolean(link.attr("data-full-load"));
  }

  private void toggleMenu(GQuery menu) {
    menu.toggleClass("open")
      .children("ul")
      .slideToggle(ANIMATION_TIME);
  }

  private void hideUnrelatedBranches(GQuery item) {
    $("#gwt-toc li.open")
      .not(item).not(item.parents())
      .removeClass("open")
      .children("ul")
      .slideUp(ANIMATION_TIME);
  }

  private void showBranch(GQuery item) {
    item.parents()
      .filter("li")
      .addClass("open")
      .children("ul")
      .slideDown(ajaxEnabled ? ANIMATION_TIME : 0);
  }

  /*
   * Change URL via pushState and load the page via Ajax.
   */
  private void loadPage(GQuery link) {
    String pageUrl = link == null ? null : link.<String>prop("pathname");

    if (!currentPage.equals(pageUrl)) {
      if (pageUrl != null) {
        // Preserve QueryString, useful for the gwt.codesvr parameter in dev-mode.
        pageUrl = pageUrl.replaceFirst("(#.*|)$", Window.Location.getQueryString() + "$1");
        // Set the page to load in the URL
        JsUtils.runJavascriptFunction(history, "pushState", null, null, pageUrl);
      }

      pageUrl = Window.Location.getPath();
      if (!currentPage.equals(pageUrl)) {
        $("#spinner").show();
        $("#gwt-content").load(pageUrl + " #gwt-content > div", null, new Function() {
          @Override
          public void f() {
            openMenu();
            scrollToHash();
            $("#spinner").hide();
          }
        });
      } else {
        scrollToHash();
      }
      currentPage = pageUrl;
    }
  }

  /*
   * Move the scroll to the hash fragment in the URL
   */
  private void scrollToHash() {
    String hash = Window.Location.getHash();
    GQuery anchor = hash.length() > 1 ? $(hash + ", [name='" + hash.substring(1) + "']") : $();
    if (anchor.isEmpty()) {
      Window.scrollTo(0, 0);
    } else {
      anchor.scrollIntoView();
    }
  }
}
