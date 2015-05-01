package org.wso2.sample.user.store.manager;

import org.wso2.carbon.user.core.UserStoreException;

public class CustomUserStoreManagerException extends UserStoreException {

    private int errorCode;

    public CustomUserStoreManagerException(){
        super();
    }

    public CustomUserStoreManagerException(int errorCode){
        super();
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
