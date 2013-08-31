/*
 * Vellum by Evan Summers under Apache Software License 2.0 from ASF.
 * 
 */
package vellum.security;

import java.io.File;
import sun.security.tools.KeyTool;
import vellum.logr.Logr;
import vellum.logr.LogrFactory;
import vellum.util.Streams;
import vellum.util.Strings;

/**
 *
 * @author evan.summers
 */
public class VKeyTool {

    Logr logger = LogrFactory.getLogger(getClass());
    String keyStoreType = "JKS";
    String providerName = null;
    String keyAlgName = "RSA";
    String dname = Certificates.LOCAL_DNAME;
    String keyStoreLocation;
    String trustStoreLocation;
    String keyStorePassword;
    String trustStorePassword;

    private void main() throws Exception {
        keyStoreLocation = getProperty("javax.net.ssl.keyStore");
        trustStoreLocation = getProperty("javax.net.ssl.trustStore");
        keyStorePassword = getProperty("javax.net.ssl.keyStorePassword");
        trustStorePassword = getProperty("javax.net.ssl.trustStorePassword");
        dname = getProperty("dname");
        File keyStoreFile = new File(keyStoreLocation);
        File trustStoreFile = new File(trustStoreLocation);
        String keyAlias = Streams.removeFileNameExtension(keyStoreFile);
        String certFilePath = keyStoreFile.getParent() + File.separator + keyAlias + ".pem";
        File certFile = new File(certFilePath);
        logger.info("certFile", certFilePath);
        if (keyStoreFile.exists() || trustStoreFile.exists() || certFile.exists()) {
            if (dname == null) {
                throw new Exception("require dname");
            }
            if (!keyStoreFile.delete()) {
                throw new Exception("unable to delete " + keyStoreFile.getPath());
            }
            trustStoreFile.delete();
            certFile.delete();
        }
        logger.info("keyStoreFile", keyStoreFile.getPath(), keyStoreFile.exists());
        genKeyPair(keyStoreLocation, keyAlias);
        exportCert(keyStoreLocation, keyAlias, certFilePath);
        if (false) {
            importCert(trustStoreLocation, keyAlias, certFilePath);
            list(trustStoreLocation);
        }
        list(keyStoreLocation);
    }

    String getProperty(String name) {
        String value = System.getProperty(name);
        logger.info("getProperty", name, value);
        return value;
    }
    
    void genKeyPair(String keyStoreFile, String keyAlias) throws Exception {
        keyTool(new String[]{
                    "-genkeypair",
                    "-keyalg", keyAlgName,
                    "-keystore", keyStoreFile,
                    "-storetype", keyStoreType,
                    "-storepass", keyStorePassword,
                    "-alias", keyAlias,
                    "-keypass", keyStorePassword,
                    "-dname", dname
                });
    }

    void exportCert(String keyStore, String alias, String certFile) throws Exception {
        keyTool(new String[]{
                    "-export",
                    "-keystore", keyStore,
                    "-storetype", keyStoreType,
                    "-storepass", keyStorePassword,
                    "-alias", alias,
                    "-keypass", keyStorePassword,
                    "-file", certFile,
                    "-rfc"
                });
    }

    void importCert(String trustStore, String trustAlias, String certFile) throws Exception {
        keyTool(new String[]{
                    "-import",
                    "-noprompt",
                    "-keystore", trustStore,
                    "-storetype", keyStoreType,
                    "-storepass", trustStorePassword,
                    "-alias", trustAlias,
                    "-file", certFile
                });
    }

    void list(String keyStore) throws Exception {
        keyTool(new String[]{
                    "-list",
                    "-keystore", keyStore,
                    "-storetype", keyStoreType,
                    "-storepass", keyStorePassword
                });
    }

    void keyTool(String[] args) throws Exception {
        System.out.println(Strings.joinArray(" ", args));
        KeyTool.main(args);
    }

    public static void main(String[] args) throws Exception {
        try {
            new VKeyTool().main();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
