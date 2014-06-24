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

package com.hp.hpl.jena.assembler;

import java.util.*;

import com.hp.hpl.jena.rdf.model.Model;

/**
    A Content object records content to be used to fill models. This Content
    class contains other Content objects. 
*/
public class Content
    {
    /**
        An empty Content object for your convenience.
    */
    public static final Content empty = new Content();
    
    /**
        The list of component Content objects. 
    */
    protected final List<Content> contents;
    
    /**
        Initialise a content object that includes the contents of each (Content) item
        in the list <code>contents</code>.
    */
    public Content( List<Content> contents )
        { this.contents = contents; }
    
    /**
        Initialise an empty Content object.
    */
    public Content()
        { this( new ArrayList<Content>() ); }

    /**
        Answer the model <code>m</code> after filling it with the contents
        described by this object.
    */
    public Model fill( Model m )
        {
            for ( Content content : contents )
            {
                content.fill( m );
            }
        return m; 
        }

    public boolean isEmpty()
        {
            for ( Content content : contents )
            {
                if ( !content.isEmpty() )
                {
                    return false;
                }
            }
        return true;
        }
    }
