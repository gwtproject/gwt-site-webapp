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
package com.google.gwt.site.webapp.client;

import static com.google.gwt.query.client.GQuery.$;
import static com.google.gwt.query.client.GQuery.body;
import static com.google.gwt.query.client.GQuery.window;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Predicate;
import com.google.gwt.query.client.Properties;
import com.google.gwt.query.client.js.JsUtils;
import com.google.gwt.query.client.plugins.ajax.Ajax;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.site.webapp.client.highlight.JsHighlight;
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
  // We discard links with different origin, hashes, starting with protocol, javadocs,
  // and media links.
  static final RegExp isSameOriginRexp = RegExp.compile("^" + origin
                                                        + "|^(?!(#|[a-z#]+:))(?!.*(|/)javadoc/)(?!.*\\.(jpe?g|png|mpe?g|mp[34]|avi)$)",
                                                        "i");

  private static boolean isPushstateCapable = history.get("pushState") != null;
  private static boolean ajaxEnabled = isPushstateCapable && origin.startsWith("http");
  private static String currentPage = Window.Location.getPath();
  private HandlerRegistration resizeHandler;

  @Override
  public void onModuleLoad() {
    bindSearch();

    enhancePage();
    enhanceMenu();

    onPageLoaded(false);
    $(".holder").show();
  }

  private void highLight() {
    JsHighlight.INSTANCE.initialize();
    $("pre > code").each(new Function(){
      public void f(Element e) {
        JsHighlight.INSTANCE.highlightBlock(e);
      }
    });
  }

  private void bindSearch() {
    $("#search form").submit(new Function() {
      @Override
      public boolean f(Event e) {
        InputElement input = $(this).children("input").get(0).cast();

        doSearch(input.getValue());

        return false;
      }
    });
  }

  private void enhanceMenu() {
    $("#nav").not(".alwaysOpen")
        .hover(
            new Function() {
              @Override
              public void f(Element e) {
                $(e).removeClass("closed");
              }
            },
            new Function() {
              @Override
              public void f(Element e) {
                $(e).addClass("closed");
              }
            }
        );
  }

  /*
   * Open the branch and select the item corresponding to the current url.
   */
  private void openMenu() {
    // close all submenus
    // todo hide first anchor with css
    $("#submenu > nav > ul > li").hide().children("a").hide();

    String path = Window.Location.getPath();
    GQuery selectedItem = $("#submenu a[href='" + path + "']")
        .filter(new Predicate() {
          @Override
          public boolean f(Element e, int index) {
            return !Style.Display.NONE.getCssName().equals(e.getStyle().getDisplay());
          }
        })
        .eq(0);

    showBranch(selectedItem);

    GQuery liParents = selectedItem
        .parentsUntil("#submenu")
        .filter("li");
    GQuery subMenuItem = liParents.last();

    subMenuItem.show();
    String mainNavigationHref = subMenuItem.children("a").attr("href");

    $("#nav a.active").removeClass("active");
    $("#nav a[href='" + mainNavigationHref + "']").addClass("active");
    $("#submenu .active").not(liParents).removeClass("active");
    liParents.add(selectedItem).not(selectedItem.parent()).addClass("active");

    // Change the page title for easy bookmarking
    $("title").text("[GWT] " + subMenuItem.children("a").text() + " - " + selectedItem.text());

    boolean homePage = isHomePage(path);
    boolean overviewPage = isOverviewPage(path);
    $("#nav").toggleClass("alwaysOpen", homePage);
    $("#content").toggleClass("home", homePage);
    if (homePage || overviewPage) {
      $("#submenu").hide();
    } else {
      $("#submenu").show();
    }

    maybeStyleHomepage();
  }

  /*
   * Enhance the page adding handlers and replacing relative by absolute urls
   */
  private void enhancePage() {
    $("#nav")
        .mouseenter(new Function() {
          @Override
          public void f() {
            $(this).removeClass("closed");
          }
        })
        .mouseleave(new Function() {
          @Override
          public void f() {
            if (!$(this).hasClass("alwaysOpen")) {
              $(this).addClass("closed");
            }
          }
        });

    enhanceLinks();

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
          $("#submenu.show").removeClass("show");

          // Load the page using Ajax
          loadPage($(e));
          return false;
        }
        return true;
      }
    });

    // Select the TOC item when URL changes
    $(window)
        .on("popstate", new Function() {
              @Override
              public void f() {
                loadPage(null);
              }
            }
        );
  }

  private boolean shouldEnhanceLink(GQuery link) {
    return
        // Enhance only local links
        isSameOriginRexp.test(link.attr("href")) &&
        // Do not load links that are marked as full page reload
        !Boolean.parseBoolean(link.attr("data-full-load"));
  }

  private void enhanceLinks() {
    // Replace relative paths in anchors by absolute ones
    // exclude all anchors in the conHighlight and collapse menutent area.
    // TODO could be done on server side
    $("a").not($("#content a")).each(new Function() {
      @Override
      public void f(Element e) {
        GQuery link = $(e);
        if (shouldEnhanceLink(link)) {
          // No need to make complicated things for computing
          // the absolute path: anchor.pathname is the way
          link.attr("href", link.<Object>prop("pathname"));
        }
      }
    });

    // We add a span with the +/- icon so as the click area is well defined
    // this span is not rendered in server side because it is only needed
    // for enhancing the page.
    GQuery parentItems = $("#submenu ul > li li").has("ul").prepend("<span/>");

    $("#submenu").children("span, a[href=\"#\"]").unbind("click");

    // Toggle the branch when clicking on the arrow or anchor without content
    $(parentItems).children("span, a[href=\"#\"]").on("click", new Function() {
      @Override
      public boolean f(Event e) {
        toggleMenu($(e).parent());
        return false;
      }
    });

    parentItems
        .addClass("folder")
        .not(".open")
        .children("ul")
        .slideUp(0);
  }

  private void toggleMenu(GQuery menu) {
    menu.toggleClass("open")
        .children("ul")
        .slideToggle(ANIMATION_TIME);
  }

  private void showBranch(final GQuery item) {
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      @Override
      public void execute() {
        item.parents("li")
            .addClass("open")
            .children("ul")
            .slideDown(ajaxEnabled ? ANIMATION_TIME : 0);
      }
    });
  }

  /*
   * Change URL via pushState and load the page via Ajax.
   */
  private void loadPage(GQuery link) {
    String pageUrl = link == null ? null : link.<String>prop("pathname");

    boolean shouldReplaceMenu = shouldReplaceMenu(pageUrl);
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
        ajaxLoad(pageUrl, shouldReplaceMenu);
      } else {
        scrollToHash();
      }
      currentPage = pageUrl;
    }
  }

  private void ajaxLoad(String pageUrl, final boolean shouldReplaceMenu) {
    Ajax.Settings settings = Ajax.createSettings();
    settings.setUrl(pageUrl);
    settings.setDataType("html");
    settings.setType("get");
    settings.setSuccess(new Function() {
      @Override
      public void f() {
        GQuery content = $("<div>" + getArgument(0) + "</div>");

        if (shouldReplaceMenu) {
          $("#submenu").replaceWith(content.find("#submenu"));
        }

        $("#content").replaceWith(content.find("#content"));

        onPageLoaded(shouldReplaceMenu);
      }
    });

    Ajax.ajax(settings);
  }

  private boolean shouldReplaceMenu(String pageUrl) {
    String path = Window.Location.getPath();

    boolean isHomeOrOverview = isHomePage(path) || isOverviewPage(path);

    return isHomeOrOverview && (!isHomePage(pageUrl) || !isOverviewPage(pageUrl));
  }

  private void onPageLoaded(boolean menuReplaced) {
    if (menuReplaced) {
      enhanceLinks();
    }
    openMenu();
    scrollToHash();
    $("#spinner").hide();
    $("#editLink").appendTo("#content h1");
    // highlight loaded page
    highLight();
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

  private void maybeStyleHomepage() {
    if ($("#content").hasClass("home")) {
      styleHomepage();
      resizeHandler = Window.addResizeHandler(new ResizeHandler() {
        @Override
        public void onResize(ResizeEvent event) {
          styleHomepage();
        }
      });

      // Pager
      $(".next, .pager a").click(new Function() {
        @Override
        public boolean f(final Event event) {
          event.preventDefault();
          $("html, body")
              .each(new Function() {
                @Override
                public void f(Element e) {
                  new ScrollTopAnimation(e, getElementOffset($($(event).attr("href")))).run(600);
                }
              });
          return true;
        }
      });

      $(".pager a").click(new Function() {
        @Override
        public boolean f(Event e) {
          e.preventDefault();
          $(".pager a").removeClass("active");
          $(this).addClass("active");

          return true;
        }
      });

      $(window).scroll(new Function() {
        @Override
        public void f() {
          $(".pager a").removeClass("active");
          if ($(window).scrollTop() + 100 > getElementOffset($("#letsbegin"))) {
            $(".pager a:nth-child(3)").addClass("active");
          } else if ($(window).scrollTop() + 100 > getElementOffset($("#gwt"))) {
            $(".pager a:nth-child(2)").addClass("active");
          } else {
            $(".pager a:nth-child(1)").addClass("active");
          }
        }
      });
    } else {
      $(window).unbind(Event.ONSCROLL);
      if (resizeHandler != null) {
        resizeHandler.removeHandler();
        resizeHandler = null;
      }
    }
  }

  private int getElementOffset(GQuery element) {
    return element.offset().top + Window.getScrollTop();
  }

  private boolean isOverviewPage(String path) {
    return "/overview.html".compareToIgnoreCase(path) == 0;
  }

  private boolean isHomePage(String path) {
    return "/".equals(path);
  }

  private void styleHomepage() {
    final int windowHeight = $(window).height();
    int sectionHeight = $("#letsbegin").height();

    if (windowHeight > sectionHeight) {
      $(".home section").each(new Function() {
        @Override
        public void f() {
          $(this)
              .css("height", windowHeight + "px")
              .css("padding", "0");
          GQuery container = $(this).find(".container");
          container.css("padding-top", (windowHeight - container.height()) / 2 + "px");
        }
      });
    }
  }

  private native void doSearch(String value) /*-{
      var element = $wnd.google.search.cse.element.getElement('searchresults');
      element.execute(value);
  }-*/;
}
