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

package com.hp.hpl.jena.sparql.util;

import org.apache.jena.atlas.lib.Callback ;

import com.hp.hpl.jena.query.ARQ ;


public class RefBoolean
{
    private volatile boolean value ;
    public final boolean getValue() { return value; }

    public RefBoolean(Symbol monitoredProperty)
    { this(ARQ.getContext(), monitoredProperty, ARQ.getContext().isTrue(monitoredProperty)) ; }
    
    public RefBoolean(Symbol monitoredProperty, boolean initialValue)
    { this(ARQ.getContext(), monitoredProperty, initialValue) ; }
    
    public RefBoolean(final Context context, final Symbol monitoredProperty, boolean initialValue)
    {
        value = initialValue ;
        context.addCallback(
            new Callback<Symbol>()
            {
                @Override
                public synchronized void proc(Symbol property)
                {
                    if ( property.equals(monitoredProperty) )
                        value = context.isTrue(monitoredProperty) ;
                }
            }) ;
    }
    
    public RefBoolean(Context context, Symbol monitoredProperty)
    {
        this(context, monitoredProperty, context.isTrue(monitoredProperty)) ;
    }
}
