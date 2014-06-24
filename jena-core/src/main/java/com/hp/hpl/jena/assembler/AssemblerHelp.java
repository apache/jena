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

import java.lang.reflect.*;
import java.util.*;

import com.hp.hpl.jena.assembler.assemblers.AssemblerGroup;
import com.hp.hpl.jena.assembler.exceptions.*;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;
import com.hp.hpl.jena.vocabulary.*;

/**
    AssemblerHelp provides utility methods used by, and useful for working with,
    the Assembler code, including the methods that expand a model to include the
    required inferences and to find the most specific type of a root in an
    assembler specification.
*/
public class AssemblerHelp
    {
    /**
        A useful constant for <code>listStatements(S, P, O)</code>. 
    */
    protected static final Property ANY = null;
    
    /**
        Answer a Resource .equals() to <code>root</code>, but in the expanded
        model.
    */
    public static Resource withFullModel( Resource root )
        { return root.inModel( fullModel( root.getModel() ) ); }
    
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
        cannot be loaded. 
    <p>    
        Contrast with <code>loadClasses(AssemblerGroup,Model)</code>, 
        which loads classes and assumes that those classes are assemblers to 
        be added to the group.
     */
    public static void loadArbitraryClasses( AssemblerGroup g, Model m )
        {
        StmtIterator it = m.listStatements( null, JA.loadClass, ANY );
        while (it.hasNext()) loadArbitraryClass( g, it.nextStatement() );        
        }
    
    /**
         Load all the classes which are objects of any (t, ja:assembler, S) statements 
         in <code>m</code>. <code>group.implementWIth(t,c)</code> is called
         for each statement, where <code>c</code> is an instance of the class named
         by <code>S</code>. The order in which the classes are loaded is not
         specified, and loading stops immediately if any class cannot be loaded.
    */
    public static void loadAssemblerClasses( AssemblerGroup group, Model m )
        {
        StmtIterator it = m.listStatements( ANY, JA.assembler, ANY );
        while (it.hasNext()) loadAssemblerClass( group, it.nextStatement() );
        }
    
    /**
        Load the class named by the object of <code>s</code>, run its
        <code>whenRequiredByAssembler</code> method if any, and
        register an <code>implementWith</code> for the subject of
        <code>s</code> and an instance of the class.
    */
    private static void loadAssemblerClass( AssemblerGroup group, Statement s )
        {
        Class<?> c = loadArbitraryClass( group, s );
        runAnyAssemblerConstructor( group, s, c );
        }
    
    /**
        Load the class named by the object of <code>s</code> if necessary.
        If that class has a static method <code>whenRequiredByAssembler</code>
        with an <code>AssemblerGroup</code> argument, call that method
        passing it <code>ag</code>.
    */
    private static Class<?> loadArbitraryClass( AssemblerGroup ag, Statement s )
        {
        Class<?> loaded = loadClassNamedBy( s ); 
        try 
            { 
            Method m = loaded.getDeclaredMethod( "whenRequiredByAssembler", new Class[] {AssemblerGroup.class} );
            m.invoke( null, ag );
            }
        catch (NoSuchMethodException e)
            { /* that's OK */ }
        catch (Exception e) 
            { throw new JenaException( e ); }       
        return loaded;
        }
    
    private static Class<?> loadClassNamedBy( Statement s )
        {
        try { return Class.forName( getString( s ) ); }
        catch (Exception e) { throw new JenaException( e ); }
        }

    private static void runAnyAssemblerConstructor( AssemblerGroup group,  Statement s, Class<?> c )
        {
        try
            {
            Resource type = s.getSubject();
            Constructor<?> con = getResourcedConstructor( c );
            if (con == null)
                establish( group, type, c.newInstance() );
            else
                establish( group, type, con.newInstance( s.getSubject() ) );
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

    private static Constructor<?> getResourcedConstructor( Class<?> c )
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
    public static Resource findSpecificType( Resource root, Resource baseType )
        {
        Set<Resource> types = findSpecificTypes( root, baseType );
        if (types.size() == 1)
            return types.iterator().next();
        if (types.size() == 0)
            return baseType;
        throw new AmbiguousSpecificTypeException( root, new ArrayList<>( types ) );
        }

    /**
        Answer all the types of <code>root</code> which are subtypes of
        <code>baseType</code> and which do not have subtypes which are
        also types of <code>root</code>.
    */
    public static Set<Resource> findSpecificTypes( Resource root, Resource baseType )
        {
        List<RDFNode> types = root.listProperties( RDF.type ).mapWith( Statement.Util.getObject ).toList();
        Set<Resource> results = new HashSet<>();
        for (int i = 0; i < types.size(); i += 1)
            {
            Resource candidate = (Resource) types.get( i );
            if  (candidate.hasProperty( RDFS.subClassOf, baseType ))
                if (hasNoCompetingSubclass( types, candidate )) 
                    results.add( candidate );
            }
        return results;
        }

    private static boolean hasNoCompetingSubclass( List<RDFNode> types, Resource candidate )
        {
            for ( RDFNode type : types )
            {
                Resource other = (Resource) type;
                if ( other.hasProperty( RDFS.subClassOf, candidate ) && !candidate.equals( other ) )
                {
                    return false;
                }
            }
        return true;
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
    public static Set<Resource> findAssemblerRoots( Model model )
        { return findAssemblerRoots( model, JA.Object ); }

    /**
        Answer a Set of the objects in the full expansion of the assembler
        specification <code>model</code> which have rdf:type <code>type</code>,
        which <i>must</i> be a subtype of <code>ja:Object</code>.
    */
    public static Set<Resource> findAssemblerRoots( Model model, Resource type )
        { return fullModel( model ).listResourcesWithProperty( RDF.type, type ).toSet(); }

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
        Set<Resource> roots = findAssemblerRoots( singleRoot, type );
        if (roots.size() == 1) return roots.iterator().next();
        if (roots.size() == 0) throw new BadDescriptionNoRootException( singleRoot, type );
        throw new BadDescriptionMultipleRootsException( singleRoot, type );
        }
    }
