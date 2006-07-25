/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.util;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.assembler.exceptions.NotUniqueException;
import com.hp.hpl.jena.assembler.exceptions.PropertyRequiredException;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.sdb.assembler.CommandAssemblerException;
import com.hp.hpl.jena.sdb.assembler.TypeNotUniqueException;
import com.hp.hpl.jena.vocabulary.RDF;

public class AssemblerUtils
{
    public static List<String> multiValueString(Resource r, Property p)
    {
        List<String> values = new ArrayList<String>() ;
        StmtIterator sIter = r.listProperties(p) ;
        while(sIter.hasNext())
        {
            Statement s = sIter.nextStatement() ;
            String v = s.getString() ;
            if ( v != null )
                values.add(v) ;
        }
        return values;
    }

    public static List<String> multiValueURI(Resource r, Property p)
    {
        List<String> values = new ArrayList<String>() ;
        StmtIterator sIter = r.listProperties(p) ;
        while(sIter.hasNext())
        {
            Statement s = sIter.nextStatement() ;
            String v = s.getResource().getURI() ;
            if ( v != null )
                values.add(v) ;
        }
        return values;
    }
    
    public static boolean exactlyOneProperty(Resource r, Property p)
    {
        StmtIterator sIter = r.listProperties(p) ;
        try {
            if ( ! sIter.hasNext() )
                throw new PropertyRequiredException(r, p) ;
            sIter.next() ;
            if ( sIter.hasNext() )
                throw new NotUniqueException(r, p) ;
        } finally { sIter.close() ; } 
        return true ;
    }

    public static boolean atmostOneProperty(Resource r, Property p)
    {
        StmtIterator sIter = r.listProperties(p) ;
        try {
            if ( ! sIter.hasNext() ) return true ;
            sIter.next() ;
            if ( sIter.hasNext() )
                throw new NotUniqueException(r, p) ;
        } finally { sIter.close() ; } 
        return true ;
    }

    public static String getStringValue(Resource r, Property p)
    {
        if ( ! atmostOneProperty(r, p) )
            throw new CommandAssemblerException(r, "More than one property '"+AssemblerUtils.fmtURI(p)+"'") ;
        Statement s = r.getProperty(p) ;
        if ( s == null )
            return null ;
        return s.getString() ; 
    }

    public static Resource getResourceValue(Resource r, Property p)
    {
        if ( ! atmostOneProperty(r, p) )
            throw new CommandAssemblerException(r, "More than one property '"+AssemblerUtils.fmtURI(p)+"'") ;
        Statement s = r.getProperty(p) ;
        if ( s == null )
            return null ;
        return s.getResource() ;
    }

    public static Resource getResourceByType(Model model, Resource type)
    {
        StmtIterator sIter = model.listStatements(null, RDF.type, type) ;
        if ( ! sIter.hasNext() )
            return null ;
        Resource r = sIter.nextStatement().getSubject() ;
        if ( sIter.hasNext() )
            throw new TypeNotUniqueException(r, "More than one resource with type "+AssemblerUtils.fmtURI(r)) ;
        return r ;
    }
    
    public static String fmtURI(Resource r)
    { return r.getModel().shortForm(r.getURI()) ;  }

}

/*
 * (c) Copyright 2006 Hewlett-Packard Development Company, LP
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