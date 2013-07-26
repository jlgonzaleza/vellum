/*
 */
package mobi.servlets;

import vellum.logr.Logr;
import vellum.logr.LogrFactory;
import vellum.util.Files;
import java.io.IOException;
import java.io.Writer;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author evan
 */
public class Page {

    Logr logr = LogrFactory.getLogger(getClass());
    HttpServletRequest req;
    HttpServletResponse res;
    HtmlBuilder builder;

    public Page(String htmlFileName) {
        builder = new HtmlBuilder(Files.readString(getClass(), htmlFileName));
    }
                   
    protected void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
        this.req = req;
        this.res = res;
        res.setContentType("text/html");
        Writer w = res.getWriter();
        w.write(builder.toString());
        w.close();
    }
}