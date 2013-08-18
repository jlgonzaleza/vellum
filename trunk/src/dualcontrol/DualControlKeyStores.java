
package dualcontrol;

import java.io.FileInputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.security.SecureRandom;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import org.apache.log4j.Logger;

/**
 *
 * @author evans
 */
public class DualControlKeyStores {    
    static final Logger logger = Logger.getLogger(DualControlKeyStores.class);
    static final String keyStorePath = System.getProperty("dualcontrol.ssl.keyStore");
    static final char[] keyStorePassword = System.getProperty("dualcontrol.ssl.keyStorePassword").toCharArray();
    static final char[] keyPassword = System.getProperty("dualcontrol.ssl.keyStorePassword").toCharArray();
    static final String trustStorePath = System.getProperty("dualcontrol.ssl.trustStore");
    static final char[] trustStorePassword = System.getProperty("dualcontrol.ssl.trustStorePassword").toCharArray();    
    
    public static SSLContext createSSLContext() throws Exception {
        return createSSLContext(keyStorePath, keyStorePassword, keyPassword,
                trustStorePath, trustStorePassword);
    }

    public static SSLContext createSSLContext(String keyStorePath, 
            char[] keyStorePassword, char[] keyPassword,
            String trustStorePath, char[] trustStorePassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(keyStorePath), keyStorePassword);
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(trustStorePath), trustStorePassword);
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, keyPassword);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(trustStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), 
                trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }
    
    public static KeyStore loadKeyStore(String keyStorePath, char[] storePass) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JCEKS");
        if (keyStorePath.contains(":")) {
            String[] array = keyStorePath.split(":");
            Socket socket = DualControlKeyStores.createSSLContext().getSocketFactory().
                createSocket(array[0], Integer.parseInt(array[1]));
            keyStore.load(socket.getInputStream(), storePass);
            socket.close();
        } else {
            keyStore.load(new FileInputStream(keyStorePath), storePass);
        }
        return keyStore;
    }
}
