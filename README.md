[![Published on Vaadin  Directory](https://img.shields.io/badge/Vaadin%20Directory-published-00b4f0.svg)](https://vaadin.com/directory/component/remoteconsole)
[![Stars on Vaadin Directory](https://img.shields.io/vaadin-directory/star/remoteconsole.svg)](https://vaadin.com/directory/component/remoteconsole)

RemoteConsole
=============

Enhanced Vaadin debug console sending log messages to the server.

RemoteConsole replaces the default implementation of Vaadin's debug console with a version that also sends the log messages to the server. This can be used e.g. for debugging on mobile platforms where the small screen size makes it difficult to use the default debug console.

To enable this add-on, inherit org.vaadin.remoteconsole.RemoteConsoleWidgetset in your widgetset and add RemoteConsoleHandler.initialize(getService()) to your servlet's servletInitialized method. See the included demo for details.

The debug console must be enabled to make the log messages go to the server, but it doesn't have to be open. To achieve this add ?debug=quiet to the URL.
