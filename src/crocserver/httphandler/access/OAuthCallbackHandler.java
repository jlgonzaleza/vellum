/*
 * (c) Copyright 2010, iPay (Pty) Ltd
 */
package crocserver.httphandler.access;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import crocserver.app.CrocApp;
import crocserver.httpserver.HttpExchangeInfo;
import java.io.IOException;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import vellum.html.HtmlPrinter;
import vellum.logr.Logr;
import vellum.logr.LogrFactory;
import vellum.util.Streams;

/**
 *
 * @author evans
 */
public class OAuthCallbackHandler implements HttpHandler {
    Logr logger = LogrFactory.getLogger(getClass());
    CrocApp app;
    HttpExchange httpExchange;
    HttpExchangeInfo httpExchangeInfo;
    PrintStream out;
    
    public OAuthCallbackHandler(CrocApp app) {
        super();
        this.app = app;
    }
        
    String state;
    String code;
    String error;
    
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        logger.info("handle", getClass().getSimpleName());
        this.httpExchange = httpExchange;
        httpExchangeInfo = new HttpExchangeInfo(httpExchange);
        logger.info("handle", httpExchangeInfo.getParameterMap());
        if (false) {
            String post = Streams.readString(httpExchange.getRequestBody());
            logger.info("post", post);
        }
        state = httpExchangeInfo.getParameter("state");
        code = httpExchangeInfo.getParameter("code");
        error = httpExchangeInfo.getParameter("error");
        try {
            if (error != null) {
                httpExchangeInfo.handleError(error);
            } else if (code != null) {
                handleCode();
            }
            httpExchangeInfo.setResponse("text/html", true);
            HtmlPrinter p = new HtmlPrinter(httpExchange.getResponseBody());
            p.div("menuBarDiv");
            p.a_("/", "Home");
            p._div();
            p.h(2, "Welcome");
            p.pre(httpExchangeInfo.getParameterMap().toString());
            httpExchange.close();
        } catch (Exception e) {
            httpExchangeInfo.handleException(e);
        }
    }
    
    void handleCode() throws Exception {
        app.getGoogleApi().setCode(code);
        app.getGoogleApi().sendTokenRequest();
    }
  
}
