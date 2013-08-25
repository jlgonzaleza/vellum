package dualcontrol;

import java.security.KeyStore;
import java.util.Arrays;
import java.util.Map;
import javax.crypto.SecretKey;
import org.apache.log4j.Logger;

/**
 *
 * @author evans
 */
public class DualControlSession {
    private final static Logger logger = Logger.getLogger(DualControlSession.class);

    private KeyStore dualKeyStore;
    private char[] dualPass;
    private String dualAlias;

    public void configure(String keyStoreLocation, char[] storePass, String prompt) 
            throws Exception {
        logger.debug("keyStore " + keyStoreLocation);
        this.dualKeyStore = DualControlKeyStores.loadKeyStore(keyStoreLocation, storePass);
        Map.Entry<String, char[]> entry = DualControlReader.readDualEntry(prompt);
        this.dualAlias = entry.getKey();
        this.dualPass = entry.getValue();
        if (!keyStoreLocation.contains(":")) {
            new JCEKSBruteForceTimer(1000*1000, keyStoreLocation, storePass, dualAlias, dualPass).start(4);
        }
        logger.debug("alias " + dualAlias);
    }
    
    public void clear() {
        Arrays.fill(dualPass, (char) 0);
    }

    public SecretKey loadKey(String alias) throws Exception {
        alias += "-" + dualAlias;
        logger.debug("loadKey " + alias);
        return (SecretKey) dualKeyStore.getKey(alias, dualPass);
    }
}
