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

package com.hp.hpl.jena.sparql.core;

import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.util.Context ;
import com.hp.hpl.jena.sparql.util.Symbol ;

/** Allocate variables */

public class VarAlloc
{
    private String baseMarker ;
    private long counter = 0 ;

    // Globals
    // Try to avoid their use because of clashes/vry large allocated names.
    //private static VarAlloc varAnonAllocator  = new VarAlloc(ARQConstants.allocGlobalVarAnonMarker) ;
    //public static VarAlloc getVarAnonAllocator() { return bNodeAllocator ; }

    private static VarAlloc varAllocator    = new VarAlloc(ARQConstants.allocGlobalVarMarker) ;
    public static VarAlloc getVarAllocator() { return varAllocator ; }
    
    public static VarAlloc get(Context context, Symbol name)
    { 
        return (VarAlloc)context.get(name) ;
    }
    
    public VarAlloc(String baseMarker)
    {
        this.baseMarker = baseMarker ;
    }
    
    
    
    public Var allocVar()
    { return alloc(baseMarker, counter ++) ; }
    
    static private Var alloc(String base, long number)
    { return Var.alloc(base+number) ; }
    
}
