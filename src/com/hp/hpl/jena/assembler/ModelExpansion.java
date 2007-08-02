/*
 	(c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: ModelExpansion.java,v 1.12 2007-08-02 13:33:09 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.IteratorCollection;
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
    /**
        Answer a new model which is the aggregation of
    <ul>
        <li>the statements of <code>model</code>
        <li>the non-bnode subclass statements of <code>schema</code>
        <li>the subclass closure of those statements
        <li>the rdf:type statements implied by the rdfs:domain statements
            of <code>schema</code> and the <code>model</code>
            statements using that statements property
        <li>similarly for rdfs:range
        <li>the rdf:type statements implied by the subclass closure
    </ul>
    */
    public static Model withSchema( Model model, Model schema )
        {
        Model result = ModelFactory.createDefaultModel().add( model );
        addSubclassesFrom( result, schema );        
        addSubClassClosure( result );
        addDomainTypes( result, schema );   
        addRangeTypes( result, schema );
        addIntersections( result, schema );
        addSupertypes( result );
        return result;
        }
    
    private static final Property ANY = null;
    
    protected static void addSubclassesFrom( Model result, Model schema )
        {
        for (StmtIterator it = schema.listStatements( ANY, RDFS.subClassOf, ANY ); it.hasNext();)
            { 
            Statement s = it.nextStatement();
            if (s.getSubject().isURIResource() && s.getObject().isURIResource()) result.add( s ); 
            }
        }
    
    /**
        Do (limited) subclass closure on <code>m</code>.
    <p>    
        Those classes in <code>m</code> that appear in <code>subClassOf</code>
        statements are given as explicit superclasses all their indirect superclasses.
    */
    public static void addSubClassClosure( Model m )
        {
        Set roots = selectRootClasses( m, findClassesBySubClassOf( m ) );
        for (Iterator it = roots.iterator(); it.hasNext();)
            addSuperClasses( m, (Resource) it.next() );
        }
    
    /**
        To each subclass X of <code>type</code> add as superclass all the
        classes between X and <code>type</code>.
    */
    private static void addSuperClasses( Model m, Resource type )
        { addSuperClasses( m, new LinkedSeq( type ) ); }

    /**
        To each subclass X of <code>parents.item</code> add as superclass
        all the classes between X and that item and all the items in the 
        rest of <code>parents</code>.
    */
    private static void addSuperClasses( Model m, LinkedSeq parents )
        {
        Model toAdd = ModelFactory.createDefaultModel();
        addSuperClasses( m, parents, toAdd );
        m.add(  toAdd );
        }
    
    /**
        Add to <code>toAdd</code> all the superclass statements needed 
        to note that any indirect subclass of <code>X = parents.item</code> has
        as superclass all the classes between it and X and all the remaining
        elements of <code>parents</code>.
    */
    private static void addSuperClasses( Model m, LinkedSeq parents, Model toAdd )
        {
        Resource type = parents.item;
        for (StmtIterator it = m.listStatements( null, RDFS.subClassOf, type ); it.hasNext();)
            {
            Resource t = it.nextStatement().getSubject();
            for (LinkedSeq scan = parents.rest; scan != null; scan = scan.rest)
                toAdd.add( t, RDFS.subClassOf, scan.item );
            addSuperClasses( m, parents.push( t ), toAdd );
            }
        }

    /**
         Answer the subset of <code>classes</code> which have no
         superclass in <code>m</code>.
    */
    private static Set selectRootClasses( Model m, Set classes )
        {
        Set roots = new HashSet();
        for (Iterator it = classes.iterator(); it.hasNext();)
            {
            Resource type = (Resource) it.next();
            if (!m.contains( type, RDFS.subClassOf, (RDFNode) null ) ) roots.add( type ); 
            }
        return roots;
        }

    /**
        Answer the set of all classes which appear in <code>m</code> as the
        subject or object of a <code>rdfs:subClassOf</code> statement.
    */
    private static Set findClassesBySubClassOf( Model m )
        {
        Set classes = new HashSet();
        StmtIterator it = m.listStatements( null, RDFS.subClassOf, (RDFNode) null );
        while (it.hasNext()) addClasses( classes, it.nextStatement() );
        return classes;
        }

    /**
        Add to <code>classes</code> the subject and object of the statement
        <code>xSubClassOfY</code>.
    */
    private static void addClasses( Set classes, Statement xSubClassOfY )
        {
        classes.add( xSubClassOfY.getSubject() );
        classes.add( xSubClassOfY.getObject() );
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
        Model toAdd = ModelFactory.createDefaultModel();
        for (StmtIterator it = schema.listStatements( ANY, RDFS.range, ANY ); it.hasNext();)
            {
            Statement s = it.nextStatement();
            RDFNode type = s.getObject();
            Property property = (Property) s.getSubject().as( Property.class );
            for (StmtIterator x = result.listStatements( ANY, property, ANY ); x.hasNext();)
                {
                RDFNode ob = x.nextStatement().getObject();
                if (ob.isResource()) toAdd.add( (Resource) ob, RDF.type, type );
                }
            }
        result.add( toAdd );
        }
    
    protected static void addSupertypes( Model result )
        {
        Model temp = ModelFactory.createDefaultModel();
        for (StmtIterator it = result.listStatements( ANY, RDF.type, ANY ); it.hasNext();)
            {
            Statement s = it.nextStatement();
            Resource c = AssemblerHelp.getResource( s );
            for (StmtIterator subclasses = result.listStatements( c, RDFS.subClassOf, ANY ); subclasses.hasNext();)
                {
                RDFNode type = subclasses.nextStatement().getObject();
                // System.err.println( ">> adding super type: subject " + s.getSubject() + ", type " + type );
                temp.add( s.getSubject(), RDF.type, type );
                }
            }
        result.add( temp );
        }
    
    private static void addIntersections( Model result, Model schema )
        {
        StmtIterator it = schema.listStatements( ANY, OWL.intersectionOf, ANY );
        while (it.hasNext()) addIntersections( result, schema, it.nextStatement() );
        }

    private static void addIntersections( Model result, Model schema, Statement s )
        {
        Resource type = s.getSubject();
        List types = asJavaList( AssemblerHelp.getResource( s ) );
        Set candidates = subjectSet( result, ANY, RDF.type, (Resource) types.get(0) );
        for (int i = 1; i < types.size(); i += 1)
            removeElementsWithoutType( candidates, (Resource) types.get(i) );
        addTypeToAll( type, candidates );
        }

    private static void addTypeToAll( Resource type, Set candidates )
        {
        List types = equivalentTypes( type );
        for (Iterator it = candidates.iterator(); it.hasNext();)
            {
            Resource resource = ((Resource) it.next());
            for (int i = 0; i < types.size(); i += 1)
                resource.addProperty( RDF.type, (Resource) types.get(i) );
            }
        }

    private static List equivalentTypes( Resource type )
        {
        List types = new ArrayList();
        types.add( type );
        for (StmtIterator it = type.getModel().listStatements( ANY, OWL.equivalentClass, type ); it.hasNext();)
            types.add( it.nextStatement().getSubject() );
        return types;
        }

    private static void removeElementsWithoutType( Set candidates, Resource type )
        {
        for (Iterator it = candidates.iterator(); it.hasNext();)
            {
            Resource candidate = (Resource) it.next();
            if (!candidate.hasProperty( RDF.type, type )) it.remove();
            }
        }

    private static Set subjectSet( Model result, Resource S, Property P, RDFNode O )
        {
        return IteratorCollection.iteratorToSet
            ( result.listStatements( S, P, O ) .mapWith( Statement.Util.getSubject ) );
        }

    private static List asJavaList( Resource resource )
        {
        return ((RDFList) resource.as( RDFList.class )).asJavaList();
        }
    
    /**
        A Lisp-style linked list. Used because we want non-updating cons
        operations.
    */
    protected static class LinkedSeq
        {
        final Resource item;
        final LinkedSeq rest;
        
        LinkedSeq( Resource item ) 
            { this( item, null ); }

        LinkedSeq( Resource item, LinkedSeq rest ) 
            { this.item = item; this.rest = rest; }
        
        LinkedSeq push( Resource item ) 
            { return new LinkedSeq( item, this ); }
        
        public String toString()
            {
            StringBuffer result = new StringBuffer( "[" );
            LinkedSeq scan = this;
            while (scan != null) { result.append( scan.item ); scan = scan.rest; result.append( " " ); }
            return result.append( "]" ).toString();
            }
        }
    }


/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
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