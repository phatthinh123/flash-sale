package com.flashsale.flashsale.business.port;

public interface DistributedLockPort {
    boolean tryLock(String lockKey);

    void unlock(String lockKey);
}
