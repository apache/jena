/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: ModelExpansion.java,v 1.3 2006-01-11 10:40:28 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.*;

/**
     The ModelExpansion code expands a model <code>M</code> against a 
     schema <code>S</code>, returning a new model which contains
     
     <ul>
     <li>the statements of M
     <li>any statements (A rdfs:subClassOf B) from S where neither A nor B
           is a bnode.
    <li>statements (A rdf:type T) if M contains (A P any) and 
        S contains (P rdfs:domain T).
    <li>statements (A rdf:type T) if M contains (any P A) and 
        S contains (P rdfs:range T).
    <li>statements (A rdf:type T) if (A rdf:type U) and (U rdfs:subClassOf T).
     </ul>
     
    This is sufficient to allow the subjects in <code>M</code> which have
    properties from <code>S</code> to have enough type information for
    AssemblerGroup dispatch.
    
    @author kers
 */
public class ModelExpansion
    {
    public static Model withSchema( Model model, Model schema )
        {
        Model result = ModelFactory.createDefaultModel().add( model );
        addSubclassesFrom( result, schema );        
        addDomainTypes( result, schema );   
        addRangeTypes( result, schema );
        addSupertypes( result );
        return result;
        }
    
    private static final Property ANY = null;
    
    protected static void addSubclassesFrom( Model result, Model schema )
        {
        for (StmtIterator it = schema.listStatements( ANY, RDFS.subClassOf, ANY ); it.hasNext();)
            { 
            Statement s = it.nextStatement();
            if (!s.getSubject().isAnon() && !s.getResource().isAnon()) result.add( s ); 
            }
        }
    
    protected static void addDomainTypes( Model result, Model schema )
        {
        for (StmtIterator it = schema.listStatements( ANY, RDFS.domain, ANY ); it.hasNext();)
            {
            Statement s = it.nextStatement();
            Property property = (Property) s.getSubject().as( Property.class );
            RDFNode type = s.getObject();
            for (StmtIterator x = result.listStatements( ANY, property, ANY ); x.hasNext();)
                {
                Statement t = x.nextStatement();
                result.add( t.getSubject(), RDF.type, type );
                }
            }
        }
    
    protected static void addRangeTypes( Model result, Model schema )
        {
        for (StmtIterator it = schema.listStatements( ANY, RDFS.range, ANY ); it.hasNext();)
            {
            Statement s = it.nextStatement();
            RDFNode type = s.getObject();
            Property property = (Property) s.getSubject().as( Property.class );
            for (StmtIterator x = result.listStatements( ANY, property, ANY ); x.hasNext();)
                {
                Statement t = x.nextStatement();
                result.add( t.getResource(), RDF.type, type );
                }
            }
        }
    
    protected static void addSupertypes( Model result )
        {
        Model temp = ModelFactory.createDefaultModel();
        for (StmtIterator it = result.listStatements( ANY, RDF.type, ANY ); it.hasNext();)
            {
            Statement s = it.nextStatement();
            for (StmtIterator subclasses = result.listStatements( s.getResource(), RDFS.subClassOf, ANY ); subclasses.hasNext();)
                {
                RDFNode type = subclasses.nextStatement().getObject();
                // System.err.println( ">> adding super type: subject " + s.getSubject() + ", type " + type );
                temp.add( s.getSubject(), RDF.type, type );
                }
            }
        result.add( temp );
        }
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