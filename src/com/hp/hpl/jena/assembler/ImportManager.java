/*
 	(c) Copyright 2006 Hewlett-Packard Development Company, LP
 	All rights reserved - see end of file.
 	$Id: ImportManager.java,v 1.1 2006-01-11 10:26:02 chris-dollin Exp $
*/

package com.hp.hpl.jena.assembler;

import java.util.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.compose.MultiUnion;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.util.FileManager;
import com.hp.hpl.jena.vocabulary.OWL;

public class ImportManager
    {

    /**
        Answer <code>model</code> if it has no imports, or a union model with
        <code>model</code> as its base and its imported models as the other
        components.
    */
    public static Model withImports( Model model )
        { return ImportManager.withImports( FileManager.get(), model ); }

    /**
        Answer <code>model</code> if it has no imports, or a union model with
        <code>model</code> as its base and its imported models as the other
        components. The file manager <code>fm</code> is used to load the
        imported models.
    */
    public static Model withImports( FileManager fm, Model model )
        {
        StmtIterator it = model.listStatements( null, OWL.imports, (RDFNode) null );
        if (it.hasNext())
            {
            MultiUnion g = new MultiUnion( new Graph[] { model.getGraph() } );
            Model result = ModelFactory.createModelForGraph( g );
            while (it.hasNext()) g.addGraph( ImportManager.graphFor( fm, it.nextStatement() ) );
            return result;
            }
        else
            return model;
        }

    private static Map cache = new HashMap();
    
    static Graph graphFor( FileManager fm, Statement s )
        {
        Resource url = s.getResource();
        Graph already = (Graph) cache.get( url );
        if (already == null)
            {
            Graph result = withImports( fm, fm.loadModel( url.getURI() ) ).getGraph();
            cache.put( url, result );
            return result;
            }
        else
            return already;
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