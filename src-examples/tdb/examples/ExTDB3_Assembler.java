/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package tdb.examples;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.shared.JenaException;

import com.hp.hpl.jena.sparql.core.assembler.DatasetAssemblerVocab;
import com.hp.hpl.jena.sparql.util.TypeNotUniqueException;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

import com.hp.hpl.jena.tdb.assembler.VocabTDB;

/** 
 * Examples of finding an assembler for a TDB model in a larger collection
 * of descriptions in a single file.
 */
public class ExTDB3_Assembler
{
    public static void main(String... argv)
    {
        String assemblerFile = "Store/tdb-assembler.ttl" ;
        
        // Find a particular description in the file where there are several: 
        Model spec = FileManager.get().loadModel(assemblerFile) ;

        // Find the right starting point for the description in some way.
        Resource root = null ;

        if ( false )
            // If you know the Resource URI:
            root = spec.createResource("http://example/myChoiceOfURI" );
        else
        {
            // Alternatively, look for the a single resource of the right type. 
            try {
                // Find the required description - the file can contain descriptions of many different types.
                root = GraphUtils.findRootByType(spec, VocabTDB.tDatasetTDB) ;
                if ( root == null )
                    throw new JenaException("Failed to find a suitable root") ;
            } catch (TypeNotUniqueException ex)
            { throw new JenaException("Multiple types for: "+DatasetAssemblerVocab.tDataset) ; }
        }

        Dataset ds = (Dataset)Assembler.general.open(root) ;
    }
}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * (c) Copyright 2010 Epimorphics Ltd.
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