/*
 	(c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: ReasonerFactoryAssembler.java,v 1.8 2007-05-10 14:01:43 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.assemblers;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.exceptions.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;
import com.hp.hpl.jena.shared.JenaException;

/**
    The ReasonerFactoryAssembler constructs a ReasonerFactory from the
    description. The factory class may be specified by a URL (which looks
    the class up in the registry) or by class name; otherwise it defaults to
    the GenericRuleReasonerFactory.
<p>
    If the class is specified by class name then an instance of that class
    is acquired by calling its <code>theInstance</code> method if it
    has one. Otherwise a fresh instance is constructed by calling its 
    zero-argument constructor (and exploding if it hasn't got one).
<p>
    Thanks to Adam Cimarosti for provoking this code and providing an
    example implementation.

    @author kers
*/
public class ReasonerFactoryAssembler extends AssemblerBase implements Assembler
    {
    public Object open( Assembler a, Resource root, Mode irrelevant )
        { 
        checkType( root, JA.ReasonerFactory );
        return addRules( root, a, getReasonerFactory( root ) );
        }

    private ReasonerFactory addRules( Resource root, Assembler a, final ReasonerFactory r )
        {
        final List rules = RuleSetAssembler.addRules( new ArrayList(), a, root );
        if (rules.size() > 0)
            if (r instanceof GenericRuleReasonerFactory)
                {
                return new ReasonerFactory()
                    {
                    public Reasoner create( Resource configuration )
                        {
                        GenericRuleReasoner result = (GenericRuleReasoner) r.create( configuration );
                        result.addRules( rules );
                        return result;
                        }

                    public Model getCapabilities()
                        { return r.getCapabilities(); }

                    public String getURI()
                        { return r.getURI(); }
                    };
                }
            else
                throw new CannotHaveRulesException( root );
        return r;
        }

    protected Reasoner getReasoner( Resource root )
        { return getReasonerFactory( root ).create( root ); }
    
    protected static ReasonerFactory getReasonerFactory( Resource root )
        {
        Resource reasonerURL = getUniqueResource( root, JA.reasonerURL );
        String className = getOptionalClassName( root );
        return 
            className != null ? getReasonerFactoryByClassName( root, className )
            : reasonerURL == null ? GenericRuleReasonerFactory.theInstance()
            : getReasonerFactoryByURL( root, reasonerURL )
            ;
        }

    private static ReasonerFactory getReasonerFactoryByClassName
        ( Resource root, String className )
        {
        Class c = loadClass( root, className );
        mustBeReasonerFactory( root, c );
        ReasonerFactory theInstance = resultFromStatic( c, "theInstance" );
        return theInstance == null ? createInstance( root, c ) : theInstance;
        }

    private static ReasonerFactory createInstance( Resource root, Class c )
        { 
        try
            { return (ReasonerFactory) c.newInstance(); }
        catch (Exception e)
            { throw new AssemblerException( root, "could not create instance of " + c.getName(), e ); }
        }

    private static ReasonerFactory resultFromStatic( Class c, String methodName )
        {
        try
            { return (ReasonerFactory) c.getMethod( methodName, null ).invoke( null, null ); }
        catch (Exception e)
            { return null; }
        }

    /**
        Throw a <code>NotExpectedTypeException</code> if <code>c</code>
        isn't a subclass of <code>ReasonerFactory</code>.
    */
    private static void mustBeReasonerFactory( Resource root, Class c )
        {
        try 
            { c.asSubclass( ReasonerFactory.class ); }
        catch (ClassCastException e) 
            { throw new NotExpectedTypeException( root, ReasonerFactory.class, c ); }
        }

    /**
        Answer the string described by the value of the unique optional
        <code>JA.reasonerClass</code> property of <code>root</code>,
        or null if there's no such property. The value may be a URI, in which case
        it must be a <b>java:</b> URI with content the class name; or it may
        be a literal, in which case its lexical form is its class name; otherwise,
        BOOM.
    */
    private static String getOptionalClassName( Resource root )
        {
        RDFNode classNode = getUnique( root, JA.reasonerClass );
        return
            classNode == null ? null
            : classNode.isLiteral() ? classNode.asNode().getLiteralLexicalForm()
            : classNode.isResource() ? mustBeJava( classNode.asNode().getURI() )
            : null
            ;
        }

    /**
        Throw an exception if <code>uri</code> doesn't start with "java:",
        otherwise answer the string beyond the ":".
    */
    private static String mustBeJava( String uri )
        { // TODO replace JenaException
        if (uri.startsWith( "java:" )) return uri.substring( 5 );
        throw new JenaException( "class name URI must start with 'java:': " + uri );
        }

    /**
        Answer a ReasonerFactory which delivers reasoners with the given
        URL <code>reasonerURL</code>. If there is no such reasoner, throw
        an <code>UnknownreasonerException</code>.
    */
    public static ReasonerFactory getReasonerFactoryByURL( Resource root, Resource reasonerURL )
        {
        String url = reasonerURL.getURI();
        ReasonerFactory factory = ReasonerRegistry.theRegistry().getFactory( url );
        if (factory == null) throw new UnknownReasonerException( root, reasonerURL );
        return factory;
        }
    }


/*
 * (c) Copyright 2005, 2006, 2007 Hewlett-Packard Development Company, LP
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