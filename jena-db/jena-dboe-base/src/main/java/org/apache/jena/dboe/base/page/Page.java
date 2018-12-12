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

package org.apache.jena.dboe.base.page;

import org.apache.jena.atlas.io.Printable ;
import org.apache.jena.dboe.base.block.Block;

public interface Page extends Printable
{
    public static final int NO_ID   = -1 ;
    
    /** Pages are addressed ints (a page ref does in on-disk blocks)
     * although block are address in longs
     */  
    public int getId() ;
    
    /** Return a string for display that identifies this Page */
    public String getRefStr() ;
    
    /** Return the block associated with this page */ 
    public Block getBackingBlock() ;
    
    /**
     * The underlying block for this page has changed (e.g. it's been
     * promoted and the promotion may have caused the block to change).
     */
    public void reset(Block block) ;

}
