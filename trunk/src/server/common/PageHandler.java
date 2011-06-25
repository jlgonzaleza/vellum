/*
 * Copyright 2011, iPay (Pty) Ltd, Evan Summers
 * Apache Software License 2.0
 * Supported by BizSwitch.net
 */
package server.common;

import server.parameter.Parameters;
import common.logger.Logr;
import common.logger.LogrFactory;
import common.printer.PrintStreamAdapter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import server.parameter.Entry;
import server.parameter.ParameterMap;
import common.printer.Printer;
import common.util.Beans;
import common.util.Streams;
import common.util.Strings;
import common.util.Types;
import common.exception.Exceptions;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author evanx
 */
public abstract class PageHandler implements HttpHandler {

    protected HttpExchange httpExchange;
    protected String urlQuery;
    protected String path;
    protected String[] pathArgs;
    protected Printer out;
    protected ParameterMap parameterMap = new ParameterMap();
    protected Logr logger = LogrFactory.getLogger(getClass());
    protected boolean showMenu = true;
    protected boolean acceptGzip = false;
    protected boolean agentWget = false;
    protected ByteArrayOutputStream baos = null;

    public PageHandler() {
    }

    public PageHandler(boolean showMenu) {
        this.showMenu = showMenu;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        this.httpExchange = httpExchange;
        path = httpExchange.getRequestURI().getPath();
        parsePath();
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
        parseHeaders();
        if (agentWget) {
            baos = new ByteArrayOutputStream();
            out = new PrintStreamAdapter(baos);
        } else if (acceptGzip) {
            httpExchange.getResponseHeaders().set("Content-encoding", "gzip");
            out = new PrintStreamAdapter(new GZIPOutputStream(httpExchange.getResponseBody()));
        } else {
            out = new PrintStreamAdapter(httpExchange.getResponseBody());
        }
        try {
            parseParameterMap();
            printPageHeader();
            if (showMenu) {
                printMenu();
            }
            handle();
            printPageFooter();
        } catch (Exception e) {
            out.printf("<pre>\n");
            e.printStackTrace(System.err);
            out.println(Exceptions.printStackTrace(e));
            out.printf("</pre>\n");
            printPageFooter();
        } finally {
            close();
            if (baos != null) {
                logger.info(baos.size());
                httpExchange.getResponseHeaders().set("Content-length", Integer.toString(baos.size()));
                httpExchange.getResponseBody().write(baos.toByteArray());
                httpExchange.getRequestBody().close();
            }
        }
    }

    protected void parsePath() {
        pathArgs = path.substring(1).split("/");
    }

    protected void parseHeaders() {
        for (String key : httpExchange.getRequestHeaders().keySet()) {
            List<String> values = httpExchange.getRequestHeaders().get(key);
            logger.trace(key, values);
            if (key.equals("Accept-encoding")) {
                if (values.contains("gzip")) {
                    acceptGzip = true;
                }
            } else if (key.equals("User-agent")) {
                for (String value : values) {
                    if (value.toLowerCase().contains("wget")) {
                        agentWget = true;
                    }
                }
            }
        }
        logger.info(agentWget, acceptGzip);
    }

    protected void parseParameterMap() {
        urlQuery = httpExchange.getRequestURI().getQuery();
        if (urlQuery == null) {
            return;
        }
        int index = 0;
        while (true) {
            int endIndex = urlQuery.indexOf("&", index);
            if (endIndex > 0) {
                put(urlQuery.substring(index, endIndex));
                index = endIndex + 1;
            } else if (index < urlQuery.length()) {
                put(urlQuery.substring(index));
                return;
            }
        }
    }

    protected void setBean(Object bean) {
        for (PropertyDescriptor property : Beans.getPropertyMap(bean.getClass()).values()) {
            String stringValue = parameterMap.get(property.getName());
            if (stringValue != null) {
                Beans.parse(bean, property, stringValue);
            }
        }
    }

    protected void put(String string) {
        logger.info(string);
        Entry<String, String> entry = Parameters.parseEntry(string);
        if (entry != null) {
            parameterMap.put(entry.getKey(), Strings.decodeUrl(entry.getValue()));
        }
    }

    public String getParameter(String key) {
        return parameterMap.get(key);
    }

    protected abstract void handle() throws Exception;

    protected void printCss() {
        String resourceName = getClass().getSimpleName() + ".css";
        printCss(resourceName);
    }

    protected void printCss(String resourceName) {
        out.printf("<style>\n%s\n</style>\n", Streams.readString(getClass(), resourceName));
    }

    protected void printPageHeader() throws IOException {
        out.println("<html>");
        out.println("<head>");
        out.printf("<title>%s</title>", getClass().getSimpleName());
        printCss("/bizserver/common/style.css");
        out.println("</head>");
        out.println("<body>");
    }

    protected void printMenu() throws IOException {
        out.printf("<form method='get' action='/search'>\n");
        out.printf("<div class='menuBarDiv'>\n");
        out.printf("<span class='menuItem'><a href='/'><input type='button' value='home'></a></span>\n");
        out.printf("<span class='menuItem'><a href='http://evanx:8091'><input type='button' value='biz1'></a></span>\n");
        out.printf("<span class='menuItem'><a href='http://evanx:8092'><input type='button' value='biz2'></a></span>\n");
        out.printf("<span class='menuItem'><a href='http://evanx:8093'><input type='button' value='bizserver'></a></span>\n");
        out.printf("<span class='menuItem'><a href='http://evanx:8094'><input type='button' value='biz4'></a></span>\n");
        out.printf("<span class='menuItem'><a href='http://evanx:8095'><input type='button' value='biz5'></a></span>\n");
        out.printf("<span class='menuItem'><a href='http://evanx:8099'><input type='button' value='evanx'></a></span>\n");
        out.printf("<span class='hostName'>[<a href='/'>%s</a>]</span>\n", Parameters.getHostName());
        out.printf("</div>\n");
        if (false) {
            out.printf("<div class='actionBarDiv'>\n");
            for (PageHandlerInfo info : PageHandlerInfoManager.getInstance().getHandlerInfoList()) {
                if (info.getHandlerType() == PageHandlerType.COMMAND) {
                    out.printf("<span class='actionItem'><a href='/%s'><input type='button' value='%s'></a></span>\n",
                            info.getName(), info.getLabel());
                }
            }
        }
        out.printf("<span class='queryInput'><input name='query' value='%s'/></span>\n",
                Types.formatDisplay(parameterMap.get("query")));
        //out.printf("<input type='reset' value='Clear' onclick='this.form.reset();'/>\n");
        for (PageHandlerInfo info : PageHandlerInfoManager.getInstance().getHandlerInfoList()) {
            if (info.getHandlerType() == PageHandlerType.QUERY) {
                out.printf("<span class='actionItem'><input type='submit' value='%s'></span>\n",
                        info.getName(), info.getLabel());
            }
        }
        out.printf("</form>\n");
        out.printf("</div>\n");
    }

    public void pre(StringBuilder builder) {
        out.printf("<pre>");
        out.printf(Strings.escapeHtml(builder.toString()));
        out.printf("</pre>\n");
    }

    protected void printPageFooter() throws IOException {
        out.println("</body>");
        out.println("</html>");
    }

    protected void close() throws IOException {
        out.close();
    }
}
