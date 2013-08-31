/*
 * Vellum by Evan Summers under Apache Software License 2.0 from ASF.
 * 
 */
package vellum.test;


/**
 *
 * @author evan.summers
 */
public class Woohoo {

    public static void assertTrue(String message, boolean condition) {
        if (!condition) {
            System.err.printf("D'oh! %s\n", message);
        }
    }
    
    public static void assertEquals(String message, Object expected, Object actual) {
        if (actual.equals(expected)) {
            System.out.printf("%s: Woohoo! %s == %s\n", message, expected, actual);
        } else {
            System.out.printf("%s: D'oh! %s != %s\n", message, expected, actual);
        }
    }
    
}
