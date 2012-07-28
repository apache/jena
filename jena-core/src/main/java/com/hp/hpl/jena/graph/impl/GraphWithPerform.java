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

package com.hp.hpl.jena.graph.impl;

import com.hp.hpl.jena.graph.*;

/**
     GraphWithPerform is an implementation interface that extends Graph
     with the performAdd and performDelete methods used by GraphBase
     to invoke non-notifying versions of add and delete.
     
*/
public interface GraphWithPerform extends Graph
    {
    /** 
        add <code>t</code> to this graph, but do not generate any event 
    */
    public void performAdd( Triple t );
   
    /** 
    	remove <code>t</code> from this graph, but do not generate any event 
    */
    public void performDelete( Triple t );
    }
