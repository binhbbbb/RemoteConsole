package org.vaadin.remoteconsole;


public interface MessageProcessor {
    public void processMessage(long time, String text, boolean error);
}
