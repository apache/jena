/*
 	(c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: CannotConstructException.java,v 1.8 2008-01-02 12:07:38 andy_seaborne Exp $
*/

package com.hp.hpl.jena.assembler.exceptions;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.rdf.model.Resource;

/**
    Exception used to report a failure of a group assembler to construct an
    object because there is no component assembler associated with the
    object's most specific type.
    @author kers
*/
public class CannotConstructException extends AssemblerException
    {
    protected final Resource type;
    protected final Class assemblerClass;
    
    public CannotConstructException( Class assemblerClass, Resource root, Resource type )
        {
        super( root, constructMessage( assemblerClass, root, type ) );
        this.type = type; 
        this.assemblerClass = assemblerClass;
        }

    private static String constructMessage( Class assemblerClass, Resource root, Resource type )
        {
        return 
            "the assembler " + getClassName( assemblerClass )
            + " cannot construct the object named " + nice( root )
            + " because it is not of rdf:type " + nice( type ) 
            ;
        }
    
    private static final String rootPrefix = getPackagePrefix( Assembler.class.getName() );
    
    private static String getClassName( Class c )
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
    public Class getAssemblerClass()
        { return assemblerClass; }

    /**
        Answer the (alleged most-specific) type of the object that could not be
        constructed.
    */
    public Resource getType()
        { return type; }
    }


/*
 * (c) Copyright 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/