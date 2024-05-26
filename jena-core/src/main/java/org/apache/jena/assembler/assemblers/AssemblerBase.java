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

package org.apache.jena.assembler.assemblers;

import java.util.List;
import org.apache.jena.assembler.* ;
import org.apache.jena.assembler.exceptions.* ;
import org.apache.jena.rdf.model.* ;
import org.apache.jena.shared.JenaException ;
import org.apache.jena.sys.JenaSystem ;
import org.apache.jena.vocabulary.RDF ;

public abstract class AssemblerBase implements Assembler
{
    static { JenaSystem.init(); }

    @Override
    public final Object open( Resource root )
        { return open( this, root ); }

    @Override
    public final Object open( Assembler a, Resource root )
        { return open( a, root, Mode.DEFAULT ); }

    @Override
    public abstract Object open( Assembler a, Resource root, Mode mode );

    protected static Resource getUniqueResource( Resource root, Property property )
        { return (Resource) getUnique( root, property ); }

    protected static Literal getUniqueLiteral( Resource root, Property property )
        { return (Literal) getUnique( root, property ); }

    protected static Statement getUniqueStatement( Resource root, Property property )
        {
        List<Statement> statements = root.listProperties( property ).toList();
        if (statements.size() == 0) return null;
        if (statements.size() == 1) return statements.get(0);
        throw new NotUniqueException( root, property );
        }

    protected static RDFNode getUnique( Resource root, Property property )
        {
        List<RDFNode> nodes = root.listProperties( property ) .mapWith( s -> s.getObject() ).toList();
        if (nodes.size() == 0) return null;
        if (nodes.size() == 1) return nodes.get(0);
        throw new NotUniqueException( root, property );
        }

    protected void checkType( Resource root, Resource type )
        {
        if (!root.hasProperty( RDF.type, type ))
            throw new CannotConstructException( this.getClass(), root, type );
        }

    @Override
    public Model openModel( Resource root, Mode mode )
        { return (Model) open( this, root, mode ); }

    @Override
    public Model openModel( Resource root )
        { return openModel( root, Mode.DEFAULT ); }

    public static Resource getRequiredResource( Resource root, Property p )
        {
        Resource R = getUniqueResource( root, p );
        if (R == null) throw new PropertyRequiredException( root, p );
        return R;
        }

    protected Literal getRequiredLiteral( Resource root, Property p )
        {
        Literal L = getUniqueLiteral( root, p );
        if (L == null) throw new PropertyRequiredException( root, p );
        return L;
        }

    protected static Resource getResource( Statement s )
        { return AssemblerHelp.getResource( s ); }

    protected static String getString( Statement s )
        { return AssemblerHelp.getString( s ); }

    protected static String getUniqueString( Resource root, Property property )
        {
        Statement s = getUniqueStatement( root, property );
        return s == null ? null : AssemblerHelp.getString( s );
        }

    protected static Class<?> loadClass( Resource root, String className )
        {
        try
            { return Class.forName( className ); }
        catch (ClassNotFoundException e)
            { throw new CannotLoadClassException( root, className, e ); }
        }

    /**
        Answer the string described by the value of the unique optional
        <code>classProperty</code> property of <code>root</code>,
        or null if there's no such property. The value may be a URI, in which case
        it must be a <b>java:</b> URI with content the class name; or it may
        be a literal, in which case its lexical form is its class name; otherwise,
        BOOM.
    */
    public static String getOptionalClassName( Resource root, Property classProperty )
        {
        RDFNode classNode = getUnique( root, classProperty );
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
    }
