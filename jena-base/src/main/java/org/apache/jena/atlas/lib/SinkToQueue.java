/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.jena.atlas.lib;

import java.util.concurrent.BlockingQueue ;
import java.util.concurrent.CancellationException ;

/** Send items to a blocking queue */
public class SinkToQueue<T> implements Sink<T>
{
    private final BlockingQueue<T> queue ;

    public SinkToQueue(BlockingQueue<T> queue) { this.queue = queue ; }

    @Override
    public void send(T item)
    {
        try
        {
            if (Thread.interrupted()) throw new InterruptedException();
            // Hopefully we'll never get passed null... but just in case
            if (null == item) return;
            queue.put(item);
        }
        catch (InterruptedException e)
        {
            throw new CancellationException();
        }
    }

    @Override
    public void flush() {}

    @Override
    public void close() {}
}
