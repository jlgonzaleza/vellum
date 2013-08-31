/*
 * Licensed to the Apache Software Foundation by Evan Summers, for ASL 2.0.
 * 
 */
package vellum.storage;

/**
 *
 * @author evan.summers
 */
public class StorageRuntimeException extends RuntimeException {
    StorageExceptionType storageExceptionType;

    public StorageRuntimeException(StorageExceptionType storageExceptionType, Throwable exception) {
        super(StorageExceptions.formatMessage(exception, storageExceptionType), exception);
        this.storageExceptionType = storageExceptionType;
    }
    
    public StorageRuntimeException(StorageExceptionType storageExceptionType) {
        super(StorageExceptions.formatMessage(storageExceptionType));
        this.storageExceptionType = storageExceptionType;
    }

    public StorageRuntimeException(StorageExceptionType storageExceptionType, Object ... args) {
        super(StorageExceptions.formatMessage(storageExceptionType, args));
        this.storageExceptionType = storageExceptionType;
    }
    
    public StorageExceptionType getStorageExceptionType() {
        return storageExceptionType;
    }
        
}
