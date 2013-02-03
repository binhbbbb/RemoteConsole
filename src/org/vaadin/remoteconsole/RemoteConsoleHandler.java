package org.vaadin.remoteconsole;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.parser.Tag;

import com.vaadin.server.BootstrapFragmentResponse;
import com.vaadin.server.BootstrapListener;
import com.vaadin.server.BootstrapPageResponse;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.ServiceException;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.SessionInitListener;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;

public class RemoteConsoleHandler implements RequestHandler {

    private final MessageProcessor messageProcessor;

    public RemoteConsoleHandler() {
        this(new DefaultMessageProcessor());
    }

    public RemoteConsoleHandler(MessageProcessor messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    @Override
    public boolean handleRequest(VaadinSession session, VaadinRequest request,
            VaadinResponse response) throws IOException {
        if (!"/remoteConsole".equals(request.getPathInfo())) {
            // Not for us
            return false;
        }

        int contentLength = request.getContentLength();
        if (contentLength <= 0) {
            throw new IOException("Content lenght required");
        }

        InputStream inputStream = request.getInputStream();
        byte[] buffer = new byte[contentLength];
        int totalRead = 0;
        while (totalRead < contentLength) {
            int read = inputStream.read(buffer, totalRead, contentLength
                    - totalRead);
            if (read == -1) {
                // More data was expected
                throw new EOFException();
            }
            totalRead += read;
        }

        try {
            JSONArray messages = new JSONArray(new String(buffer,
                    Charset.forName("UTF-8")));
            for (int i = 0; i < messages.length(); i++) {
                JSONObject message = messages.getJSONObject(i);
                boolean error = message.optBoolean("isError");
                String text = message.getString("message");
                long time = message.getLong("time");
                messageProcessor.processMessage(time, text, error);
            }
        } catch (JSONException e) {
            throw new IOException(e);
        }

        response.setStatus(200);
        response.getWriter().close();
        return true;
    }

    public static void initialize(VaadinService service) {
        initialize(service, new DefaultMessageProcessor());
    }

    public static void initialize(VaadinService service,
            final MessageProcessor messageProcessor) {
        service.addSessionInitListener(new SessionInitListener() {
            @Override
            public void sessionInit(SessionInitEvent event)
                    throws ServiceException {
                VaadinSession session = event.getSession();
                session.addRequestHandler(new RemoteConsoleHandler(
                        messageProcessor));
                session.addBootstrapListener(new BootstrapListener() {
                    @Override
                    public void modifyBootstrapPage(
                            BootstrapPageResponse response) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void modifyBootstrapFragment(
                            BootstrapFragmentResponse response) {
                        List<Node> fragmentNodes = response.getFragmentNodes();

                        Element scriptNode = new Element(Tag.valueOf("script"),
                                fragmentNodes.get(0).baseUri());
                        scriptNode
                                .appendText("window.remoteConsoleInitialized = true;");
                        scriptNode.attr("type", "text/javascript");
                        fragmentNodes.add(0, scriptNode);
                    }
                });
            }
        });
    }
}