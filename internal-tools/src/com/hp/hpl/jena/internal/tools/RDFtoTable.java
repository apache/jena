/*
    (c) Copyright 2005 Hewlett-Packard Development Company, LP
    All rights reserved.
    [See end of file]
    $Id: RDFtoTable.java,v 1.2 2005-04-06 15:28:14 chris-dollin Exp $
*/
package com.hp.hpl.jena.internal.tools;

import java.util.*;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.*;

/**
    Convert a file of RDF instance data into an HTML table according to
    the description in a config file.
    
    @author kers
 */
public class RDFtoTable
    {    
    public static void main( String [] args )
        {
        Model specModel = FileManager.get().loadModel( args[0] );
        Resource spec = findRoot( specModel );
        Model data = FileManager.get().loadModel( args[1] );
        System.out.println( new RDFtoTable().tableFrom( spec, data ) );
        }
    
    public static final String uri = "http://jena.hpl.hp.com/rdftotable#";
    
    public static final Property tableColumns = property( "columns" );
    public static final Property tableClass = property( "class" );
    public static final Property tableId = property( "id" );
    public static final Property tableDisplays = property( "displays" );
    public static final Property tableProperty = property( "property" );
    public static final Property tableLiteral = property( "literal" );
    
    protected static Property property( String leafname )
        { return ResourceFactory.createProperty( uri + leafname ); }
    
    public String tableFrom( Resource root, Model data )
        {
        List L = toList( root.getRequiredProperty( tableColumns ).getResource() );
        String attClass = optionalString( root, tableClass, "framed" );
        String attId = optionalString( root, tableId, "TABLE" );
        String heads = "";
        for (int i = 0; i < L.size(); i += 1)
            heads += "<th>" + label( (Resource) L.get(i) ) + "</th>";
        String body = "";
        int count = 0;
        for (ResIterator it = data.listSubjects(); it.hasNext();)
            body += rowFrom( count++, L, it.nextResource() );
        return 
            "<table class='" + attClass + "' ID='" + attId + "'><thead><tr>" + heads + "</tr></thead>"
            + (body.equals( "" ) ? "" : "<tbody>" + body + "</tbody>")
            + "</table>";
        }
    
    protected String rowFrom( int count, List l, Resource resource )
        {
        String elements = "";
        for (int i = 0; i < l.size(); i += 1)
            {
            Resource td = (Resource) l.get(i);
            Statement displays = td.getProperty( tableDisplays );
            if (displays != null)
                {
                List L = toList( displays.getResource() );          
                String items = "";
                for (int j = 0; j < L.size(); j += 1)
                    {
                    Resource b = (Resource) L.get(j);
                    Statement sProperty = b.getProperty( tableProperty );
                    Statement sLiteral = b.getProperty( tableLiteral );
                    if (sProperty != null)
                        {
                        Property p = (Property) sProperty.getResource().as( Property.class );
                        items += label( resource.getRequiredProperty( p ).getObject() );
                        }
                    else if (sLiteral != null)
                        {
                        String literal = sLiteral.getString();
                        items += literal;
                        }
                    }
                elements += "<td>" + items + "</td>";
                }
            }
        String tr = count % 2 == 0 ? "<tr class='even'>" : "<tr class='odd'>";
        return "\n" + tr + elements + "</tr>";
        }
    
    
    protected String label( RDFNode n )
        {
        if (n instanceof Literal)
            return ((Literal) n).getLexicalForm();
        else
            {
            Resource r = (Resource) n;
            Statement x = r.getProperty( RDFS.label );
            if (x == null) return r.getLocalName();
            return x.getString();
            }
        }
    
    public static List toList( Resource resource )
        {
        List result = new ArrayList();
        while (!resource.equals( RDF.nil ))
            {
            result.add( resource.getRequiredProperty( RDF.first ).getResource() );
            resource = resource.getRequiredProperty( RDF.rest ).getResource();
            }
        return result;
        }
    
    public static Resource findRoot( Model specModel )
        {
        return specModel.listStatements( null, tableColumns, (RDFNode) null ).nextStatement().getSubject();
        }
    
    public static String optionalString( Resource root, Property property, String ifAbsent )
        { Statement s = root.getProperty( property );
        return s == null ? ifAbsent : s.getString(); }
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