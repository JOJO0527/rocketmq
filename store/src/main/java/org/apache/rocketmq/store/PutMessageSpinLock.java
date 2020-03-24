/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.store;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Spin lock Implementation to put message, suggest using this with low race conditions
 * 自旋锁
 */
public class PutMessageSpinLock implements PutMessageLock {
    //true: Can lock, false : in lock.
    private AtomicBoolean putMessageSpinLock = new AtomicBoolean(true);

    /**
     * 不断进行CAS上锁，如果上锁成功则顺利返回，
     * 否则一直阻塞，锁竞争激烈的情况下会发生"busy-waiting"，所以此时应该避免使用自旋锁
     */
    @Override
    public void lock() {
        boolean flag;
        do {
            // if (true == putMessageSpinLock.get()) putMessageLock.set(false)
            // 如果上述操作成功  返回true 则flag = true
            // 此时其他线程try to lock的话，会因为expect ！= putMessageSpinLock().get() 导致上锁失败 进入循环CAS。
            // 此时如果多个线程一起CAS的话，可能会导致CPU资源耗尽 this is busy-waiting
            flag = this.putMessageSpinLock.compareAndSet(true, false);
        }
        while (!flag);
    }
    @Override
    public void unlock() {
        this.putMessageSpinLock.compareAndSet(false, true);
    }
}
