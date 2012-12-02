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

package com.hp.hpl.jena.shared;

/**
    Reification styles have two boolean components: whether the
    graph+reifier will intercept reification triples or not [if not, the only
    in-Jena reification is through the reifyAs operation], and whether or
    not reification triples will be visible in the graph.
*/
@Deprecated
public class ReificationStyle
    {
    public static final ReificationStyle Standard = new ReificationStyle( true, false );
    /** @deprecated Only "Standard" is supported -- use of this constant wil be ignored */
    @Deprecated
    public static final ReificationStyle Convenient = new ReificationStyle( true, true );
    /** @deprecated Only "Standard" is supported -- use of this constant wil be ignored */
    @Deprecated
    public static final ReificationStyle Minimal = new ReificationStyle( false, true );
    
    private boolean intercept;
    private boolean conceal;
    
    public ReificationStyle( boolean intercept, boolean conceal )
        {
        this.intercept = intercept;
        this.conceal = conceal;
        }
        
    public boolean intercepts()
        { return intercept; }
        
    public boolean conceals()
        { return conceal; }
        
    /**
        Answer a human-readable representation of this reification style. If it's
        one of the three standard constants, return their names; otherwise return
        a description of the fields. <i>code should not rely on these values</i>;
        they may be changed for debugging or convenience.
    */
    @Override
    public String toString()
        { 
        if (this == Minimal) return "Minimal";
        if (this == Standard) return "Standard";
        if (this == Convenient) return "Convenient";
        return "<style int=" + intercept + ", con=" + conceal + ">"; 
        }
    }
