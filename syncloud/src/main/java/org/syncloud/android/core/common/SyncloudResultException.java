package org.syncloud.android.core.common;

public class SyncloudResultException extends SyncloudException {
    public BaseResult result;

    public SyncloudResultException(String message, BaseResult result) {
        super(message);
        this.result = result;
    }
}
