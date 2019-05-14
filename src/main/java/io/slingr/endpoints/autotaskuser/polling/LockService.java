package io.slingr.endpoints.autotaskuser.polling;

public interface LockService {
    boolean lock(String key);

    boolean unlock(String key);
}
