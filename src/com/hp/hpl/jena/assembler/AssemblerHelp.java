/*
 (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 All rights reserved - see end of file.
 $Id: AssemblerHelp.java,v 1.17 2007-07-19 14:23:31 chris-dollin Exp $
 */

package com.hp.hpl.jena.assembler;

import java.lang.reflect.*;
import java.util.*;

import com.hp.hpl.jena.assembler.assemblers.AssemblerGroup;
import com.hp.hpl.jena.assembler.exceptions.NoSpecificTypeException;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.vocabulary.*;

/**
    AssemblerHelp provides utility methods used by, and useful for working with,
    the Assembler code, including the methods that expand a model to include the
    required inferences and to find the most specific type of a root in an
    assembler specification.
    
    @author kers
*/
public class AssemblerHelp
    {
    /**
        Answer a Resource .equals() to <code>root</code>, but in the expanded
        model.
    */
    public static Resource withFullModel( Resource root )
        { return (Resource) root.inModel( fullModel( root.getModel() ) ); }
    
    /**
        Answer the full model of <code>m</code>, with all its imports included and
        with the necessary properties added from the JA schema. However, if
        the magic footprint triple (ja:this, rdf:type, ja:Expanded) is present in the
        model, it is returned unchanged. Imports are managed by the shared
        <code>ImportManager.instance</code>.
    */
    public static Model fullModel( Model m )
        { return fullModel( ImportManager.instance, m ); }    
    
    /**
        Answer the full model of <code>m</code>, with all its imports included and
        with the necessary properties added from the JA schema. However, if
        the magic footprint triple (ja:this, rdf:type, ja:Expanded) is present in the
        model, it is returned unchanged. Imports are managed by <code>im</code>.
    */
    public static Model fullModel( ImportManager im, Model m )
        {
        return m.contains( JA.This, RDF.type, JA.Expanded )
            ? m
            : (Model) ModelExpansion.withSchema( im.withImports( m ), JA.getSchema() )
                .add( JA.This, RDF.type, JA.Expanded )
                .setNsPrefixes( PrefixMapping.Extended )
                .setNsPrefixes( m )
            ;
        }
    
    /**
        Load all the classes which are objects of any (t, ja:loadClass, S) 
        statements in <code>m</code>. The order in which the classes are 
        loaded is not specified, and loading stops immediately if any class 
        cannot be loaded
    <p>    
        Contrast with <code>loadClasses(AssemblerGroup,Model)</code>, 
        which loads classes and assumes that those classes are assemblers to 
        be added to the group.
     */
    public static void loadClasses( Model m )
        {
        Property ANY = null;
        StmtIterator it = m.listStatements( null, JA.loadClass, ANY );
        while (it.hasNext()) loadClass( it.nextStatement() );
        }
    
    /**
         Load all the classes which are objects of any (t, ja:assembler, S) statements 
         in <code>m</code>. <code>group.implementWIth(t,c)</code> is called
         for each statement, where <code>c</code> is an instance of the class named
         by <code>S</code>. The order in which the classes are loaded is not
         specified, and loading stops immediately if any class cannot be loaded.
    */
    public static void loadClasses( AssemblerGroup group, Model m )
        {
        Property ANY = null;
        StmtIterator it = m.listStatements( null, JA.assembler, ANY );
        while (it.hasNext()) loadClass( group, it.nextStatement() );
        }

    private static void loadClass( Statement statement )
        {
        try { Class.forName( getString( statement ) ); }
        catch (Exception e) { throw new JenaException( e ); }        
        }
    
    private static void loadClass( AssemblerGroup group, Statement statement )
        {
        try
            {
            Resource type = statement.getSubject();
            Class c = Class.forName( getString( statement ) );
            Constructor con = getResourcedConstructor( c );
            if (con == null)
                establish( group, type, c.newInstance() );
            else
                establish( group, type, con.newInstance( new Object[] { statement.getSubject() } ) );
            }
        catch (Exception e)
            { throw new JenaException( e ); }
        }

    private static void establish( AssemblerGroup group, Resource type, Object x )
        {
        if (x instanceof Assembler)
            group.implementWith( type, (Assembler) x );
        else
            throw new JenaException( "constructed entity is not an Assembler: " + x );
        }

    private static Constructor getResourcedConstructor( Class c )
        {
        try { return c.getConstructor( new Class[] { Resource.class } ); }
        catch (SecurityException e) { return null; }
        catch (NoSuchMethodException e) { return null; }
        }

    /**
         Answer the most specific type of <code>root</code> that is a subclass of
         ja:Object. If there are no candidate types, answer <code>givenType</code>. 
         If there is more than one type, throw a NoSpecificTypeException.
    */
    public static Resource findSpecificType( Resource root )
        { return findSpecificType( root, JA.Object ); }

    /**
     	 Answer the most specific type of <code>root</code> that is a subclass of
         <code>givenType</code>. If there are no candidate types, answer 
         <code>givenType</code>. If there is more than one type, throw a 
         NoSpecificTypeException.
    */
    public static Resource findSpecificType( Resource root, Resource givenType )
        {
        Model desc = root.getModel();
        StmtIterator types = root.listProperties( RDF.type );
        List results = new ArrayList();
        while (types.hasNext())
            {
            Resource type = getResource( types.nextStatement() );
            if (desc.contains( type, RDFS.subClassOf, givenType ))
                {
                boolean allowed = true;
                for (StmtIterator subs = desc.listStatements( null, RDFS.subClassOf, type ); subs.hasNext(); )
                    {
                    Resource sub = subs.nextStatement().getSubject();
                    if (!sub.equals( type ) && root.hasProperty( RDF.type, sub )) allowed = false;
                    }
                if (allowed) results.add( type );
                }
            }
        if (results.size() == 1)
            return (Resource) results.get(0);
        if (results.size() == 0)
            return givenType;
        throw new NoSpecificTypeException( root, results );
        }

    /**
        Answer the resource that is the object of the statement <code>s</code>. If
        the object is not a resource, throw a BadObjectException with that statement.
    */
    public static Resource getResource( Statement s )
        {
        RDFNode ob = s.getObject();
        if (ob.isLiteral()) throw new BadObjectException( s );
        return (Resource) ob;
        }

    /**
        Answer the plain string object of the statement <code>s</code>. If the
        object is not a string literal, throw a BadObjectException with that statement.
    */
    public static String getString( Statement s )
        {
        RDFNode ob = s.getObject();
        if (ob.isResource()) throw new BadObjectException( s );
        Literal L = (Literal) ob;
        if (!L.getLanguage().equals( "" )) throw new BadObjectException( s );
        if (L.getDatatype() == null) return L.getLexicalForm();
        if (L.getDatatype() == XSDDatatype.XSDstring) return L.getLexicalForm();
        throw new BadObjectException( s );
        }

    /**
        Answer the String value of the literal <code>L</code>, which is the
        object of the Statement <code>s</code>. If the literal is not an
        XSD String or a plain string without a language code, throw a
        BadObjectException.
    */
    public static String getString( Statement s, Literal L )
        {
        if (!L.getLanguage().equals( "" )) throw new BadObjectException( s );
        if (L.getDatatype() == null) return L.getLexicalForm();
        if (L.getDatatype() == XSDDatatype.XSDstring) return L.getLexicalForm();
        throw new BadObjectException( s );
        }

    /**
        Answer a Set of the ja:Object resources in the full expansion of
        the assembler specification model <code>model</code>.
    */
    public static Set findAssemblerRoots( Model model )
        { return findAssemblerRoots( model, JA.Object ); }

    /**
        Answer a Set of the objects in the full expansion of the assembler
        specification <code>model</code> which have rdf:type <code>type</code>,
        which <i>must</i> be a subtype of <code>ja:Object</code>.
    */
    public static Set findAssemblerRoots( Model model, Resource type )
        { return fullModel( model ).listSubjectsWithProperty( RDF.type, type ).toSet(); }

    /**
         Answer the single resource in <code>singleRoot</code> of type
         <code>ja:Model</code>. Otherwise throw an exception.
    */
    public static Resource singleModelRoot( Model singleRoot )
        { return singleRoot( singleRoot, JA.Model ); }

    /**
     	Answer the single resource in <code>singleRoot</code> of type
         <code>type</code>. Otherwise throw an exception.
    */
    public static Resource singleRoot( Model singleRoot, Resource type )
        {
        Set roots = findAssemblerRoots( singleRoot, type );
        if (roots.size() == 1) return (Resource) roots.iterator().next();
        if (roots.size() == 0) throw new BadDescriptionNoRootException( singleRoot, type );
        throw new BadDescriptionMultipleRootsException( singleRoot, type );
        }
    }

/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */