/*
 * (c) Copyright 2002, Hewlett-Packard Company, all rights reserved.
 * [See end of file]
 */

/** Internal class - public so the external code that understands the internals
 * of RDQL can access it.
 * 
 * @author   Andy Seaborne
 * @version  $Id: Slot.java,v 1.1.1.1 2002-12-19 19:18:57 bwm Exp $
 */

package com.hp.hpl.jena.rdql;

// A slot holds one of a value (A typed RDF value), a resource, a property, or a variable.
import com.hp.hpl.jena.rdf.model.* ;

public class Slot
{
    Value value ;
    Var variable ;
    // Can we just use an RDFNode here?
    Resource resource ;
    Property property ;
    Literal literal ;


    private void unset()
    {
        value = null ;
        variable = null ;
        resource = null ;
        property = null ;
        literal = null ;
    }

    public Slot()
    {
        unset() ;
    }

    public Slot(Slot slot)
    {
        value = slot.value ;
        variable = slot.variable ;
        property = slot.property ;
        resource = slot.resource ;
    }

    public void set(Value v)
    {
        unset() ;
        value = v ;
    }

    public void set(Var v)
    {
        unset() ;
        variable = v ;
    }

    public void set(Resource r)
    {
        unset() ;
        resource = r ;
    }

    public void set(Property p)
    {
        unset() ;
        property = p;
    }

    public boolean isValue() { return value != null ; }
    public boolean isVar()   { return variable != null ; }
    public boolean isResource() { return resource != null ; }
    public boolean isProperty() { return property != null ; }

    public Value getValue() { return value ; }
    public Var getVar() { return variable ; }
    public Resource getResource() { return resource ; }
    public Property getProperty() { return property ; }

    public String getVarName()
    {
        if ( variable == null )
            return null ;
        return variable.getVarName() ;
    }

    // This is a printable/parseable value - it is not called
    // during filter evaluation so we can quote things.
    public String toString()
    {
        if ( value != null )
            return value.asInfixString() ;
        if ( variable != null )
            return variable.toString() ;
        if ( property != null )
            return "<"+property.toString()+">" ;
        if ( resource != null )
            return "<"+resource.toString()+">" ;
        return "slot:unset" ;
    }
}

/*
 *  (c) Copyright Hewlett-Packard Company 2001
 *  All rights reserved.
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
 *
 * This product includes software developed by the
 * Apache Software Foundation (http://www.apache.org/).
 *
 */
