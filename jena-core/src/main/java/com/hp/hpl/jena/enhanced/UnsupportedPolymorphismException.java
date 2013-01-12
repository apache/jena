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

package com.hp.hpl.jena.enhanced;

import com.hp.hpl.jena.shared.JenaException ;

/**
    Exception to throw if an enhanced graph does not support polymorphism
    to a specific class. The exception records the "bad" class and node for
    later reporting.
*/
public class UnsupportedPolymorphismException extends JenaException
    {
    private final Class<?> type;
    private final Object node;
        
    /**
        Initialise this exception with the node that couldn't be polymorphed and
        the class it couldn't be polymorphed to.
    */
    public UnsupportedPolymorphismException( Object node, boolean hasModel, Class<?> type )
        {
        super( constructMessage( node, hasModel, type ) );
        this.node = node;
        this.type = type;
        }

    private static String constructMessage( Object node, boolean hasModel, Class<?> type )
        {
        String mainMessage = "cannot convert " + node + " to " + type;
        return hasModel ? mainMessage : mainMessage + " -- it has no model";
        }

    
    /** 
        Answer the class that the node couldn't be polymorphed to
    */
    public Class<?> getBadClass() 
        { return type; }

    /**
        Answer the node that couldn't be polymorphed.
    */
    public Object getBadNode()
        { return node; }
    }
