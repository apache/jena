/*
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

/** Split a sink stream and duplicate the operations onto two sinks 
 *  See also: {@link SinkWrapper}
 */
public class SinkSplit<T> implements Sink<T>
{
    private final Sink<T> sink1 ;
    private final Sink<T> sink2 ;

    public SinkSplit(Sink<T> sink1, Sink<T> sink2)
    {
        this.sink1 = sink1 ;
        this.sink2 = sink2 ;
    }
    
    @Override
    public void flush()
    { 
        sink1.flush();
        sink2.flush();
    }
        
    @Override
    public void send(T item)
    { 
        sink1.send(item) ;
        sink2.send(item) ;
    }

    @Override
    public void close()
    {
        sink1.close(); 
        sink2.close();
    }
}
