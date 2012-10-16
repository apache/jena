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

package com.hp.hpl.jena.sdb.store;

import com.hp.hpl.jena.graph.Node;

public interface TupleLoader
{
//    public Store getStore() ;
    
    /** Set table description */
    public void setTableDesc(TableDesc tableDesc) ;
    
    /** Get the table description */
    public TableDesc getTableDesc() ;
    

    /** Notify the start of a sequence of rows to load */
    public void start() ;
    
    /** Load a row - may not take place immediately
     *  but row object is free for reuse after calling this method.
     * @param row
     */
    public void load(Node... row) ;
    
    /** Remove a row - may not take place immediately
     *  but row object is free for reuse after calling this method.
     * @param row
     */
    public void unload(Node... row) ;

    /** Notify the finish of a sequence of rows to load.  
     * All data will have been loaded by the time this returns */ 
    public void finish() ;
    
    /** This TupleLoader is done with.
     * Do not use a TupleLoader after calling close().
     */
    public void close() ;
    // Copied from StoreLoader but not called there currently.
    // If one only type needs these, put on an implementation.  
//    public void setChunkSize(int chunks) ;
//    public int getChunkSize() ;
    
//    public void setUseThreading(boolean useThreading);
//    public boolean getUseThreading();

}
