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

import java.io.StringReader;
import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.exceptions.UnknownEncodingException;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.vocabulary.*;

public class ContentAssembler extends AssemblerBase implements Assembler
    {
    protected final FileManager defaultFileManager;
    
    public ContentAssembler()
        { this( null ); }
    
    public ContentAssembler( FileManager fm )
        { this.defaultFileManager = fm; }

    @Override public Object open( Assembler a, Resource root, Mode irrelevant )
        {
        checkType( root, JA.Content );
        return new Content( loadContent( new ArrayList<Content>(), a, root ) );
        }
    
    public final static Set<Property> contentProperties = new HashSetWith<Property>()
        .with( JA.content )
        .with( JA.literalContent )
        .with( JA.externalContent )
        .with( JA.quotedContent )
        ;
    
    static class HashSetWith<T> extends HashSet<T>
        {
        public HashSetWith<T> with( T x )
            {
            this.add( x );
            return this;
            }
        }

    public List<Content> loadContent( List<Content> contents, Assembler a, Resource root )
        {
        FileManager fm = getFileManager( a, root );
        addLiteralContent( contents, root );
        addQuotedContent( contents, root );
        addExternalContents( contents, fm, root );
        addIndirectContent( contents, a, root );
        return contents;
        }
    
    private static void addIndirectContent( List<Content> contents, Assembler a, Resource root )
        {
        StmtIterator it = root.listProperties( JA.content );
        while (it.hasNext()) contents.add( (Content) a.open( getResource( it.nextStatement() ) ) );
        }

    protected void addExternalContents( List<Content> contents, FileManager fm, Resource root )
        {
        StmtIterator it = root.listProperties( JA.externalContent );
        while (it.hasNext()) contents.add( objectAsContent( fm, it.nextStatement() ) );
        }

    private static void addQuotedContent( List<Content> contents, Resource root )
        {
        StmtIterator it = root.listProperties( JA.quotedContent );
        while (it.hasNext())
            {
            Resource q = getResource( it.nextStatement() );
            Model m = ResourceUtils.reachableClosure( q );
            contents.add( newModelContent( m ) );
            }
        }

    protected static void addLiteralContent( List<Content> contents, Resource root )
        {
        String encoding = getEncoding( root );
        StmtIterator it = root.listProperties( JA.literalContent );
        while (it.hasNext())
            {
            String s = getString( it.nextStatement() );
            Model model = parseAs( root, encoding, s );
            contents.add( newModelContent( model ) );
            }
        }

    private static Model parseAs( Resource root, String encoding, String lexicalForm )
        {
        String enc = encoding == null ? guessFrom( lexicalForm ) : encoding;
        if (enc.equals( "N3" )) return parseAsN3( lexicalForm );
        if (enc.equals( "RDF/XML" )) return parseAsXML( lexicalForm );
        throw new UnknownEncodingException( root, encoding );
        }

    private static Model parseAsXML( String lexicalForm )
        {
        String pre = 
            "<?xml version='1.0'?>"
            + "<rdf:RDF"
            + " xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"
            + " xmlns:rdfs='http://www.w3.org/2000/01/rdf-schema#'"
            + " xmlns:xsd='http://www.w3.org/2001/XMLSchema#'"
            + " xmlns:owl='http://www.w3.org/2002/07/owl#'"
            + " xmlns:dc='http://purl.org/dc/elements/1.1/'"
            + ">"
            ;
        String post = "</rdf:RDF>";
        StringReader r = new StringReader( pre + lexicalForm + post );
        return ModelFactory.createDefaultModel().read( r, "", "RDF/XML" );
        }

    private static String guessFrom( String lexicalForm )
        { return "N3"; }

    private static String getEncoding( Resource root )
        {
        Literal L = getUniqueLiteral( root, JA.contentEncoding );
        return L == null ? null : L.getLexicalForm();
        }

    protected static Content newModelContent( final Model m )
        {
        return new Content() 
            { 
            @Override public Model fill( Model x ) { x.setNsPrefixes( m ); return x.add( m ); } 
            
            @Override public boolean isEmpty() { return m.isEmpty(); }
            };
        }

    protected Content objectAsContent( FileManager fm, Statement s )
        {
        final Model m = fm.loadModel( getModelName( s ) );
        return newModelContent( m );
        }
    
    private String getModelName( Statement s )
        {
        Node o = s.getObject().asNode();
        return o.isLiteral() ? o.getLiteralLexicalForm(): o.getURI();
        }

    private FileManager getFileManager( Assembler a, Resource root )
        {
        Resource fm = getUniqueResource( root, JA.fileManager );
        return 
            fm != null ? (FileManager) a.open( fm )
            : defaultFileManager == null ? FileManager.get() 
            : defaultFileManager
            ;
        }

    static final String preamble =
        "@prefix rdf: <" + RDF.getURI() + "> ."
        + "\n@prefix rdfs: <" + RDFS.getURI() + "> ."
        + "\n@prefix owl: <" + OWL.getURI() + "> ."
        + "\n@prefix xsd: <http://www.w3.org/2001/XMLSchema#> ."
        + "\n@prefix dc: <" + DC_11.getURI() + "> ."
        ;
    
    protected static Model parseAsN3( String value )
        {
        Model result = ModelFactory.createDefaultModel();
        StringReader r = new StringReader( preamble +"\n" +  value );
        result.read( r, "", "N3" );
        return result;
        }

    public Object getFileManager()
        { return defaultFileManager; }
    }
