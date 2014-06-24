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

package com.hp.hpl.jena.assembler.exceptions;

import java.util.*;

import com.hp.hpl.jena.rdf.model.Resource;

/**
    Exception to throw when an AssemblerGroup has a choice of types
    from which to try and find an implementation.
*/
public class AmbiguousSpecificTypeException extends AssemblerException
    {
    protected final List<Resource> types;
    
    public AmbiguousSpecificTypeException( Resource root, ArrayList<Resource> types )
        {
        super( root, makeMessage( root, types ) );
        this.types = types;
        }

    private static String makeMessage( Resource root, List<Resource> types )
        { return 
            "cannot find a most specific type for " + nice( root )
            + ", which has as possibilities:" + nice( types )
            + "."; 
        }

    private static String nice( List<Resource> types )
        {
        StringBuilder result = new StringBuilder();
            for ( Resource type : types )
            {
                result.append( " " ).append( nice( type ) );
            }
        return result.toString();
        }

    public List<Resource> getTypes()
        { return types; }
    }
