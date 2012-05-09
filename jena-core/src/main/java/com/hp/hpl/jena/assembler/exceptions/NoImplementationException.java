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

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.rdf.model.Resource;

public class NoImplementationException extends AssemblerException
    {
    final Resource type;
    final Assembler assembler;
    
    public NoImplementationException( Assembler assembler, Resource root, Resource type )
        {
        super( root, messageFor( assembler, root, type ) );
        this.type = type;
        this.assembler = assembler;
        }

    private static String messageFor( Assembler a, Resource root, Resource type )
        {
        return
            "the (group) Assembler " + a 
            + " cannot construct the object " + nice( root )
            + " because it does not have an implementation for the objects's most specific type " + nice( type )
            ;
        }
    
    public Resource getType()
        { return type; }

    public Assembler getAssembler()
        { return assembler; }
    }
