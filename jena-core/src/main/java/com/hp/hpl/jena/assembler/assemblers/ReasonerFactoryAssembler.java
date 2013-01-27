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

package com.hp.hpl.jena.assembler.assemblers;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.exceptions.*;
import com.hp.hpl.jena.graph.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.reasoner.*;
import com.hp.hpl.jena.reasoner.rulesys.*;

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
*/
public class ReasonerFactoryAssembler extends AssemblerBase implements Assembler
    {
    @Override
    public Object open( Assembler a, Resource root, Mode irrelevant )
        { 
        checkType( root, JA.ReasonerFactory );
        return addSchema( root, a, addRules( root, a, getReasonerFactory( root ) ) );
        }

    private ReasonerFactory addSchema( Resource root, Assembler a, final ReasonerFactory rf )
        {
        if (root.hasProperty( JA.ja_schema ))
            {
            final Graph schema = loadSchema( root, a );
            return new ReasonerFactory()
                {
                @Override
                public Reasoner create( Resource configuration )
                    {
                    return rf.create( configuration ).bindSchema( schema );
                    }

                @Override
                public Model getCapabilities()
                    { return rf.getCapabilities(); }

                @Override
                public String getURI()
                    { return rf.getURI(); }
                };
            }
        else
            return rf;
        }

    private Graph loadSchema( Resource root, Assembler a )
        {
        Graph result = Factory.createDefaultGraph();
        for (StmtIterator models = root.listProperties( JA.ja_schema ); models.hasNext();)
            loadSchema( result, a, getResource( models.nextStatement() ) );
        return result;
        }

    private void loadSchema( Graph result, Assembler a, Resource root )
        {
        Model m = a.openModel( root );
        GraphUtil.addInto(result, m.getGraph()) ;
        }

    private ReasonerFactory addRules( Resource root, Assembler a, final ReasonerFactory r )
        {
        final List<Rule> rules = RuleSetAssembler.addRules( new ArrayList<Rule>(), a, root );
        if (rules.size() > 0)
            if (r instanceof GenericRuleReasonerFactory)
                {
                return new ReasonerFactory()
                    {
                    @Override
                    public Reasoner create( Resource configuration )
                        {
                        GenericRuleReasoner result = (GenericRuleReasoner) r.create( configuration );
                        result.addRules( rules );
                        return result;
                        }

                    @Override
                    public Model getCapabilities()
                        { return r.getCapabilities(); }

                    @Override
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
        Class<?> c = loadClass( root, className );
        mustBeReasonerFactory( root, c );
        ReasonerFactory theInstance = resultFromStatic( c, "theInstance" );
        return theInstance == null ? createInstance( root, c ) : theInstance;
        }

    private static ReasonerFactory createInstance( Resource root, Class<?> c )
        { 
        try
            { return (ReasonerFactory) c.newInstance(); }
        catch (Exception e)
            { throw new AssemblerException( root, "could not create instance of " + c.getName(), e ); }
        }

    private static ReasonerFactory resultFromStatic( Class<?> c, String methodName )
        {
        try
            { return (ReasonerFactory) c.getMethod( methodName, (Class[])null ).invoke( null, (Object[])null ); }
        catch (Exception e)
            { return null; }
        }

    /**
        Throw a <code>NotExpectedTypeException</code> if <code>c</code>
        isn't a subclass of <code>ReasonerFactory</code>.
    */
    private static void mustBeReasonerFactory( Resource root, Class<?> c )
        {
        if (!ReasonerFactory.class.isAssignableFrom( c ))
            throw new NotExpectedTypeException( root, ReasonerFactory.class, c );
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
        { return getOptionalClassName( root, JA.reasonerClass ); }

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
