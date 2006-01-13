/*
 	(c) Copyright 2005 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: ContentAssembler.java,v 1.5 2006-01-13 10:56:19 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler.assemblers;

import java.io.StringReader;
import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.exceptions.UnknownEncodingException;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.*;
import com.hp.hpl.jena.vocabulary.*;

public class ContentAssembler extends AssemblerBase implements Assembler
    {
    public Object open( Assembler a, Resource root, Mode irrelevant )
        {
        checkType( root, JA.Content );
        return new Content( loadContent( new ArrayList(), this, root ) );
        }

    public static List loadContent( List contents, Assembler a, Resource root )
        {
        addLiteralContent( contents, root );
        addQuotedContent( contents, root );
        addExternalContents( contents, root );
        addIndirectContent( contents, a, root );
        return contents;
        }
    
    private static void addIndirectContent( List contents, Assembler a, Resource root )
        {
        StmtIterator it = root.listProperties( JA.content );
        while (it.hasNext()) contents.add( a.open( getResource( it.nextStatement() ) ) );
        }

    protected static void addExternalContents( List contents, Resource root )
        {
        StmtIterator it = root.listProperties( JA.externalContent );
        while (it.hasNext()) contents.add( objectAsContent( it.nextStatement() ) );
        }

    private static void addQuotedContent( List contents, Resource root )
        {
        StmtIterator it = root.listProperties( JA.quotedContent );
        while (it.hasNext())
            {
            Resource q = getResource( it.nextStatement() );
            Model m = ResourceUtils.reachableClosure( q );
            contents.add( newModelContent( m ) );
            }
        }

    protected static void addLiteralContent( List contents, Resource root )
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
            { public Model fill( Model x ) { x.setNsPrefixes( m ); return x.add( m ); } };
        }

    protected static Content objectAsContent( Statement s )
        {
        Resource external = getResource( s );
        final Model m = FileManager.get().loadModel( external.getURI() );
        return newModelContent( m );
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

    }


/*
 * (c) Copyright 2005 Hewlett-Packard Development Company, LP
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