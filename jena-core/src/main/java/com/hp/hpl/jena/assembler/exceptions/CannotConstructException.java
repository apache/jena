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

/**
    Exception used to report a failure of a group assembler to construct an
    object because there is no component assembler associated with the
    object's most specific type.
*/
public class CannotConstructException extends AssemblerException
    {
    protected final Resource type;
    protected final Class<?> assemblerClass;
    
    public CannotConstructException( Class<?> assemblerClass, Resource root, Resource type )
        {
        super( root, constructMessage( assemblerClass, root, type ) );
        this.type = type; 
        this.assemblerClass = assemblerClass;
        }

    private static String constructMessage( Class<?>assemblerClass, Resource root, Resource type )
        {
        return 
            "the assembler " + getClassName( assemblerClass )
            + " cannot construct the object named " + nice( root )
            + " because it is not of rdf:type " + nice( type ) 
            ;
        }
    
    private static final String rootPrefix = getPackagePrefix( Assembler.class.getName() );
    
    private static String getClassName( Class<?> c )
        {
        String name = c.getName();
        return getPackagePrefix( name ).equals( rootPrefix ) ? getLeafName( name ) : name;
        }

    private static String getLeafName( String name )
        { return name.substring( name.lastIndexOf( '.' ) + 1 ); }

    private static String getPackagePrefix( String name )
        { return name.substring( 0, name.lastIndexOf( '.' ) ); }

    /**
        Answer the Assembler that cannot do the construction.
    */
    public Class<?> getAssemblerClass()
        { return assemblerClass; }

    /**
        Answer the (alleged most-specific) type of the object that could not be
        constructed.
    */
    public Resource getType()
        { return type; }
    }
