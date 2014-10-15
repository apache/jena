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


/** A Session is a set of operations that are either all read actions 
 * or a mixture of read and write (an update). Sessions are not necessarily long - 
 * they are just a grouping of operations. 
 * 
 * Most implementations of this interface do not enforce the policy - it
 * is up to the caller to preserve the invariant for the object called.
 * 
 * An implementation may allow policies such as transactional (ACID)
 * but, unless otherwise documented, an application can not 
 * assume that.
 */
public interface Session
{
    /** Signal the start of an update operation */
    public void startUpdate() ;
    
    /** Signal the completion of an update operation */
    public void finishUpdate();

    /** Signal the start of a read operation */
    public void startRead();
    
    /** Signal the completion of a read operation */
    public void finishRead();
}
