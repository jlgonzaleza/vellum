/*
 * Apache Software License 2.0
 * Supported by BizSwitch.net   
 */
package vellum.exception;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import vellum.util.Strings;
import vellum.util.formatter.ArgFormatter;

/**
 * Utility methods related to exceptions.
 *
 * @author evan
 */
public class Exceptions {

    public static Throwable getThrowable(Object[] args) {
        if (args.length > 0 && args[0] instanceof Throwable) {
            return (Throwable) args[0];
        }
        return null;
    }

    public static String getMessage(Object[] args) {
        return ArgFormatter.formatter.format(args);
    }

    public static RuntimeException newRuntimeException(Object ... args) {
        if (args.length == 1) {
            Throwable e = getThrowable(args);
            if (e instanceof RuntimeException) {
                return (RuntimeException) e;
            }
        }
        return new ArgsRuntimeException(args);
    }

    public static String printStackTrace(Exception exception) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        try {
            return baos.toString(Strings.ENCODING);
        } catch (UnsupportedEncodingException e) {
            throw Exceptions.newRuntimeException(e);
        }
    }
    
    public static void warn(Exception e) {
        e.printStackTrace(System.err);
    }
    
}
