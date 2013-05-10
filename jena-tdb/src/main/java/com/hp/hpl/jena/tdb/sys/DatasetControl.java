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

package com.hp.hpl.jena.tdb.sys;

import java.util.Iterator ;

/** A DatasetControl is an encapsulation of what to do on reads and writes.
 *  In addition, iterators returned can be tied back to the original request
 *  to check they are still valid.  
 */
public interface DatasetControl
{
    /** Fine grained, internal update - start */  
    public void startUpdate() ;
    
    /** Fine grained, internal update - finish */
    public void finishUpdate();

    /** Signal the start of an internal read operation */
    public void startRead();
    
    /** Signal the completion of an internal read operation */
    public void finishRead();
    
    /** Notify an iterator being created. */
    public <T> Iterator<T> iteratorControl(Iterator<T> iter) ;
}
