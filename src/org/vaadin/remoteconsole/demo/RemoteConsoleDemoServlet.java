package org.vaadin.remoteconsole.demo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.servlet.ServletException;

import org.vaadin.remoteconsole.MessageProcessor;
import org.vaadin.remoteconsole.RemoteConsoleHandler;

import com.vaadin.server.VaadinServlet;

public class RemoteConsoleDemoServlet extends VaadinServlet {
    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();
        RemoteConsoleHandler.initialize(getService(), new MessageProcessor() {
            private final DateFormat dateFormat = new SimpleDateFormat(
                    "hh:mm:ss:SSS", new Locale("fi"));

            @Override
            public void processMessage(long time, String text, boolean error) {
                String message = dateFormat.format(new Date(time)) + " " + text;
                if (error) {
                    System.err.println(message);
                } else {
                    System.out.println(message);
                }
            }
        });
    }
}
