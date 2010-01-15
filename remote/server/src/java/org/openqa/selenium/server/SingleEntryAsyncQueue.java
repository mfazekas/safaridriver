/*
 * Copyright 2006 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.openqa.selenium.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import static java.lang.System.*;

import org.apache.commons.logging.Log;
import org.openqa.jetty.log.LogFactory;

/**
 * <p>Holds the command to be next run in the browser</p>
 * <p/>
 * This class uses reentrant locks in order to allow the same
 * thread to populate the queue as is waiting for it, which is
 * what currently happens on during browser startup.
 *
 * @author Jennifer Bevan
 * @version $Revision: 734 $
 */
public class SingleEntryAsyncQueue<T> {
    public static final long MILLISECONDS = 1000L;
    private static final Log logger = LogFactory.getLog(SingleEntryAsyncQueue.class);
    private final AtomicReference<T> poisonData;
    private final int timeoutInSeconds;
    private final ArrayBlockingQueue<T> holder;

    public SingleEntryAsyncQueue(int timeoutInSecs) {
        timeoutInSeconds = timeoutInSecs;
        holder = new ArrayBlockingQueue<T>(1);
        poisonData = new AtomicReference<T>();
    }

    public int getTimeoutInSeconds() {
        return timeoutInSeconds;
    }

    protected void setPoison(T poisonInstance) {
        poisonData.set(poisonInstance);
    }

    protected boolean isPoison(T poisonSample) {
        T poison = poisonData.get();
        return null != poison && poison.equals(poisonSample);
    }

    protected T pollToGetContentUntilTimeout() {
        T result = holder.poll(); // in case it's already there
        if (null != result) {
            logger.debug("data was waiting: " + result);
            return result;
        }

        if (timeoutInSeconds <= 0) {
            return null;
        }

        long deadline = currentTimeMillis() + (timeoutInSeconds * MILLISECONDS);
        for(long now = currentTimeMillis(); now < deadline; now = currentTimeMillis()) {
            try {
                logger.debug("waiting for data for at most " + timeoutInSeconds + " more s");
                result = holder.poll(deadline - now, TimeUnit.MILLISECONDS);
                logger.debug("data from polling: " + result);
                return result;
            } catch (InterruptedException ie) {
                logger.debug("was interrupted; resuming wait");
                continue;
            }
        }

        return null;
    }

    protected boolean putContent(T thing) {
        final boolean result;
        logger.debug("putting command: " + thing);
        result = holder.offer(thing);
        logger.debug("..command put?: " + result);
        return result;
    }

    protected boolean isEmpty() {
        return (0 == holder.size());
    }

    protected T peek() {
        return holder.peek();
    }

    /**
     * Clears the contents of the holder (if any) and also
     * feeds 'poison' data a pending listener (if any);
     *
     * @return true if poison was set and sent to any listeners.
     */
    protected boolean poisonPollers() {
        if (null == poisonData.get()) {
            holder.clear();
            return false;
        }
        // offer poison content.  If something is already there,
        // then the next listener will already have something to get.
        putContent(poisonData.get());
        return true;
    }
    
}
