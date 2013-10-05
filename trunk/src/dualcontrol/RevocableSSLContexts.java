/*
 * Source https://code.google.com/p/vellum by @evanxsummers

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements. See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.  
 */
package dualcontrol;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.apache.log4j.Logger;

/**
 *
 * @author evan.summers
 */
public class RevocableSSLContexts {

    static Logger logger = Logger.getLogger(RevocableSSLContexts.class);
    static Set<String> revokedNames; 
    
    public static SSLContext create(String sslPrefix, Properties properties,
            MockableConsole console) throws Exception {
        ExtendedProperties props = new ExtendedProperties(properties);
        sslPrefix = props.getString(sslPrefix, sslPrefix);
        String keyStoreLocation = props.getString(sslPrefix + ".keyStore");
        if (keyStoreLocation == null) {
            throw new Exception("Missing -D property: " + sslPrefix + ".keyStore");
        }
        char[] pass = props.getPassword(sslPrefix + ".pass", null);
        if (pass == null) {
            pass = console.readPassword("Enter passphrase for %s: ", sslPrefix);
        }
        String trustStoreLocation = props.getString(sslPrefix + ".trustStore",
                keyStoreLocation);
        SSLContext sslContext = create(keyStoreLocation, pass, trustStoreLocation);
        String crlFile = props.getString(sslPrefix + ".crlFile", null);
        if (crlFile != null) {
            revokedNames = Collections.synchronizedSet(readRevokedCertNames(crlFile));
            sslContext = create(keyStoreLocation, pass, trustStoreLocation,
                    revokedNames);
        }
        Arrays.fill(pass, (char) 0);
        return sslContext;
    }

    public static SSLContext create(String keyStoreLocation, char[] pass,
            String trustStoreLocation) throws Exception {
        return create(keyStoreLocation, pass, pass, trustStoreLocation, pass);
    }

    public static SSLContext create(String keyStoreLocation,
            char[] keyStorePassword, char[] keyPassword,
            String trustStoreLocation, char[] trustStorePassword) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(keyStoreLocation), keyStorePassword);
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(trustStoreLocation), trustStorePassword);
        return create(keyStore, keyPassword, trustStore);
    }

    public static SSLContext create(KeyStore keyStore, char[] keyPassword,
            KeyStore trustStore) throws Exception {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, keyPassword);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(trustStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(),
                trustManagerFactory.getTrustManagers(), new SecureRandom());
        return sslContext;
    }

    public static SSLContext create(String keyStoreLocation, char[] pass,
            String trustStoreLocation,
            Set<String> revokedNames) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        keyStore.load(new FileInputStream(keyStoreLocation), pass);
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(new FileInputStream(trustStoreLocation), pass);
        return createRevokedNames(keyStore, pass, trustStore, revokedNames);
    }

    public static SSLContext create(KeyStore keyStore, char[] keyPass,
            Set<String> revokedNames) throws Exception {
        return createRevokedNames(keyStore, keyPass, keyStore, revokedNames);
    }

    public static SSLContext createRevokedNames(KeyStore keyStore, char[] keyPass,
            KeyStore trustStore, Set<String> revokedNames) throws Exception {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, keyPass);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManager revocableTrustManager = new RevocableClientTrustManager(
                getPrivateKeyCertificate(keyStore),
                getX509TrustManager(trustStore),                
                revokedNames, new TreeSet());
        sslContext.init(keyManagerFactory.getKeyManagers(),
                new TrustManager[]{revocableTrustManager},
                new SecureRandom());
        return sslContext;
    }

    public static SSLContext createRevokedSerialNumbers(KeyStore keyStore, char[] keyPass,
            KeyStore trustStore, Set<BigInteger> revokedSerialNumbers) 
            throws GeneralSecurityException {
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, keyPass);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManager revocableTrustManager = new RevocableClientTrustManager(
                getPrivateKeyCertificate(keyStore),
                getX509TrustManager(trustStore),
                new TreeSet(),
                revokedSerialNumbers);
        sslContext.init(keyManagerFactory.getKeyManagers(),
                new TrustManager[]{revocableTrustManager},
                new SecureRandom());
        return sslContext;
    }
    
    public static X509Certificate getPrivateKeyCertificate(KeyStore keyStore, 
            String keyAlias) throws KeyStoreException {
        if (!keyStore.entryInstanceOf(keyAlias, KeyStore.PrivateKeyEntry.class)) {
            throw new KeyStoreException("Not private key entry: " + keyAlias);
        }
        return (X509Certificate) keyStore.getCertificate(keyAlias);
    }

    public static X509Certificate getPrivateKeyCertificate(KeyStore keyStore) 
            throws KeyStoreException {
        if (countKeys(keyStore) == 1) {
            for (String alias : Collections.list(keyStore.aliases())) {
                if (keyStore.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class)) {
                    return (X509Certificate) keyStore.getCertificate(alias);
                }
            }
        }
        throw new KeyStoreException("No sole private key found in keystore");
    }
            
    public static int countKeys(KeyStore keyStore) throws KeyStoreException {
        int count = 0;
        for (String alias : Collections.list(keyStore.aliases())) {
            if (keyStore.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class)) {
                count++;
            }
        }
        return count;
    }

    private static Set<String> readRevokedCertNames(String crlFile)
            throws FileNotFoundException, IOException {
        Set<String> revocationList = new TreeSet();
        BufferedReader reader = new BufferedReader(new FileReader(crlFile));
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                return revocationList;
            }
            revocationList.add(line.trim());
        }        
    }

    private static X509TrustManager getX509TrustManager(KeyStore trustStore)
            throws GeneralSecurityException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(trustStore);
        if (trustManagerFactory.getTrustManagers().length != 1) {
            throw new GeneralSecurityException("Multiple default trust managers");
        }
        if (trustManagerFactory.getTrustManagers()[0] instanceof X509TrustManager) {
            return (X509TrustManager) trustManagerFactory.getTrustManagers()[0];
        }
        throw new GeneralSecurityException("Default X509TrustManager not found");
    }
}