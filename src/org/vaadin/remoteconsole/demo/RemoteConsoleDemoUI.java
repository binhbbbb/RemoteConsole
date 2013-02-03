package org.vaadin.remoteconsole.demo;

import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.UI;

@Widgetset("org.vaadin.remoteconsole.demo.RemoteConsoleDemoWidgetset")
public class RemoteConsoleDemoUI extends UI {

    @Override
    protected void init(VaadinRequest request) {
        getPage().setTitle("RemoteConsole");
        setContent(new Button("Press me to generate more output to the console"));
    }

}
