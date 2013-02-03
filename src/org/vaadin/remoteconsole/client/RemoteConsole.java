package org.vaadin.remoteconsole.client;

import java.util.Set;

import com.google.gwt.core.client.Duration;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.vaadin.client.ApplicationConfiguration;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.Console;
import com.vaadin.client.Util;
import com.vaadin.client.VConsole;
import com.vaadin.client.VDebugConsole;
import com.vaadin.client.ValueMap;
import com.vaadin.shared.ApplicationConstants;

public class RemoteConsole implements Console {

    private final VDebugConsole defaultConsole = new VDebugConsole();

    private JSONArray pendingMessages = new JSONArray();

    private boolean requestPending = false;

    private final ScheduledCommand purgeQueue = new ScheduledCommand() {
        @Override
        public void execute() {
            if (requestPending) {
                return;
            }

            // TODO Wait until previous request has finished to avoid messing up
            // the ordering.
            String data = pendingMessages.toString();
            pendingMessages = new JSONArray();

            ApplicationConnection applicationConnection = ApplicationConfiguration
                    .getRunningApplications().get(0);
            String url = applicationConnection
                    .translateVaadinUri(ApplicationConstants.APP_PROTOCOL_PREFIX
                            + "remoteConsole");
            RequestBuilder requestBuilder = new RequestBuilder(
                    RequestBuilder.POST, url);
            try {
                requestBuilder.sendRequest(data, new RequestCallback() {
                    @Override
                    public void onError(Request arg0, Throwable arg1) {
                        abortRemoteSending(arg1);
                        requestPending = false;
                    }

                    @Override
                    public void onResponseReceived(Request arg0, Response arg1) {
                        // All is fine, send a new batch if there's something to
                        // send
                        requestPending = false;
                        if (pendingMessages.size() != 0) {
                            execute();
                        }
                    }
                });
                requestPending = true;
            } catch (RequestException e) {
                abortRemoteSending(e);
            }
        }
    };

    private boolean remoteInitialized = false;

    @Override
    public void log(String msg) {
        defaultConsole.log(msg);
        queueMessage(msg, false);
    }

    private void queueMessage(String message, boolean isError) {
        JSONObject messageObj = new JSONObject();
        messageObj.put("message", new JSONString(message));
        if (isError) {
            messageObj.put("isError", JSONBoolean.getInstance(true));
        }
        messageObj.put("time", new JSONNumber(Duration.currentTimeMillis()));
        queueMessage(messageObj);
    }

    private void queueMessage(JSONObject message) {
        if (!remoteInitialized) {
            return;
        }
        if (pendingMessages.size() == 0) {
            Scheduler.get().scheduleDeferred(purgeQueue);
        }
        pendingMessages.set(pendingMessages.size(), message);
    }

    @Override
    public void log(Throwable e) {
        defaultConsole.log(e);
        if (e instanceof UmbrellaException) {
            UmbrellaException ue = (UmbrellaException) e;
            for (Throwable t : ue.getCauses()) {
                log(t);
            }
            return;
        }
        log(Util.getSimpleName(e) + ": " + e.getMessage());
    }

    @Override
    public void error(Throwable e) {
        defaultConsole.error(e);
        if (e instanceof UmbrellaException) {
            UmbrellaException ue = (UmbrellaException) e;
            for (Throwable t : ue.getCauses()) {
                error(t);
            }
            return;
        }
        error(Util.getSimpleName(e) + ": " + e.getMessage());
    }

    @Override
    public void error(String msg) {
        defaultConsole.error(msg);
        queueMessage(msg, true);
    }

    @Override
    public void printObject(Object msg) {
        defaultConsole.printObject(msg);
        String str;
        if (msg == null) {
            str = "null";
        } else {
            str = msg.toString();
        }
        queueMessage(str, false);
    }

    @Override
    public void dirUIDL(ValueMap u, ApplicationConnection client) {
        defaultConsole.dirUIDL(u, client);
    }

    @Override
    public void printLayoutProblems(ValueMap meta,
            ApplicationConnection applicationConnection,
            Set<ComponentConnector> zeroHeightComponents,
            Set<ComponentConnector> zeroWidthComponents) {
        defaultConsole.printLayoutProblems(meta, applicationConnection,
                zeroHeightComponents, zeroWidthComponents);
    }

    @Override
    public void setQuietMode(boolean quietDebugMode) {
        defaultConsole.setQuietMode(quietDebugMode);
    }

    @Override
    public void init() {
        defaultConsole.init();
        remoteInitialized = checkInitialized();
        if (!remoteInitialized) {
            defaultConsole
                    .log("Will not use remote debugging because window.remoteConsoleInitialized has not been set.");
        }
    }

    private native boolean checkInitialized()
    /*-{
        return !!$wnd.remoteConsoleInitialized;
    }-*/;

    private void abortRemoteSending(Throwable arg1) {
        remoteInitialized = false;
        VConsole.error("Aborting RemoteConsole because of an error");
        VConsole.error(arg1);
    }

}
