/*
 * Vellum by Evan Summers under Apache Software License 2.0 from ASF.
 * 
 */
package crocserver.storage.common;

import crocserver.app.CrocApp;
import crocserver.storage.adminuser.AdminUserStorage;
import crocserver.storage.org.OrgStorage;
import crocserver.storage.orgrole.OrgRoleStorage;

/**
 *
 * @author evan.summers
 */
public class CrocStorageHandler {
    protected CrocApp app; 
    protected CrocStorage storage;
    protected AdminUserStorage userStorage;
    protected OrgStorage orgStorage;
    protected OrgRoleStorage orgRoleStorage;
    
    public CrocStorageHandler(CrocApp app) {
        this.app = app;
        storage = app.getStorage();
        userStorage = storage.getUserStorage();
        orgRoleStorage = storage.getOrgRoleStorage();
    }    
}
