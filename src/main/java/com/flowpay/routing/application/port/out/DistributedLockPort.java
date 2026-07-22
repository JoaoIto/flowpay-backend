package com.flowpay.routing.application.port.out;

public interface DistributedLockPort {
    boolean tryAcquire(String lockKey, long waitTimeMs, long leaseTimeMs);
    void release(String lockKey);
}
