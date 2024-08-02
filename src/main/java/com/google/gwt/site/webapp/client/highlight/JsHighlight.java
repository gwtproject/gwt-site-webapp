package com.google.gwt.site.webapp.client.highlight;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.TextResource;
import elemental2.dom.Element;
import elemental2.dom.HTMLElement;

import static elemental2.dom.DomGlobal.document;

public class JsHighlight {

  interface CssHighlight extends ClientBundle  {
    CssHighlight INSTANCE =  GWT.create(CssHighlight.class);

    @Source("idea.css")
    CssResource idea();

    @Source("highlight.pack.js")
    TextResource highlight();

  }

  private static boolean initialized;
  public static final JsHighlight INSTANCE = GWT.create(JsHighlight.class);


  public void initialize() {
    if (!initialized) {
      initialized = true;
      CssHighlight.INSTANCE.idea().ensureInjected();
      Element script = document.createElement("script");
      script.textContent = CssHighlight.INSTANCE.highlight().getText();
      document.head.appendChild(script);
    }
  }

  public native void highlightBlock(HTMLElement e) /*-{
    e && $wnd.hljs && $wnd.hljs.highlightBlock(e);
  }-*/;
}
