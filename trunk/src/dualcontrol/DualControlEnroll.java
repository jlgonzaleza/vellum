/*
 * Apache Software License 2.0, (c) Copyright 2013, Evan Summers
 * 
 */
package dualcontrol;

import java.io.FileOutputStream;
import java.security.KeyStore;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.apache.log4j.Logger;

/**
 *
 * @author evans
 */
public class DualControlEnroll {

    final static Logger logger = Logger.getLogger(DualControlEnroll.class);
    private int submissionCount = SystemProperties.getInt("dualcontrol.submissions", 3);
    private String keyAlias = SystemProperties.getString("alias");
    private String keyStoreLocation = SystemProperties.getString("keystore");
    private String keyStoreType = SystemProperties.getString("storetype");
    private char[] keyStorePassword;
    private Map<String, char[]> dualMap;
    private KeyStore keyStore;
    private SecretKey secretKey;

    public static String getStringProperty(String propertyName) {
        String propertyValue = System.getProperty("alias");
        if (propertyValue == null) {
            throw new RuntimeException("Missing -D property: " + propertyName);
        }
        return propertyValue;
    } 

    public static void main(String[] args) throws Exception {
        new DualControlEnroll().start();
    }

    void start() throws Exception {
        dualMap = new DualControlReader().readDualMap(keyAlias, submissionCount);
        keyStorePassword = DualControlKeyStoreTools.getKeyStorePassword();
        keyStore = DualControlKeyStores.loadLocalKeyStore(keyStoreLocation, 
                keyStoreType, keyStorePassword);
        KeyStore.Entry entry = new KeyStore.SecretKeyEntry(secretKey);
        List<String> aliasList = Collections.list(keyStore.aliases());
        for (String alias : aliasList) {
            System.err.println("existing alias " + alias);
        }
        for (String dualAlias : dualMap.keySet()) {
            char[] dualPassword = dualMap.get(dualAlias);
            String alias = keyAlias + "-" + dualAlias;
            if (aliasList.contains(alias)) {
                secretKey = (SecretKey) keyStore.getKey(alias, dualPassword);
                break;
            }
        }
        for (String dualAlias : dualMap.keySet()) {
            char[] dualPassword = dualMap.get(dualAlias);
            String alias = keyAlias + "-" + dualAlias;
            if (!aliasList.contains(alias)) {
                KeyStore.ProtectionParameter prot = 
                        new KeyStore.PasswordProtection(dualPassword);
                keyStore.setEntry(alias, entry, prot);
            }
        }
        keyStore.store(new FileOutputStream(keyStoreLocation), keyStorePassword);
    }
}
