/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package tdb.examples;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.pgraph.assembler.PGraphAssemblerVocab;
import com.hp.hpl.jena.util.FileManager;

import com.hp.hpl.jena.sparql.core.assembler.DatasetAssemblerVocab;
import com.hp.hpl.jena.sparql.util.TypeNotUniqueException;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

/** Example of creating a TDB-back model.
 *  Once the Model (or Graph) is created, all the normal Jena APIs work
 *  (including SPARQL and SPARQL/Update)
 *   
 *  Calling TDBFactory is the only place TDB-specific code is needed.
 *  
 *  TDB-backed models can also be created via assemblers.
 *    
 * @author Andy Seaborne
 */

public class ExTDB1
{
    
    
    public static void main(String... argv)
    {
        String assemblerFile = "Store/tdb-assembler.ttl" ;
        
        // Use of TDBFactory will cause TDB to initialize and wore itself in the various Jena extension points. 
        {
            // Direct way: Make a TDB-back Jena model in the named directory.
            String directory = "MyDatabases/DB1" ;
            Model model = TDBFactory.createModel(directory) ;
        }

        {
            // Assembler way: Make a TDB-back Jena model in the named directory.
            // This way, you can change the model being used without changing the code.
            // The assembler file is a configuration file.
            // The same assembler description will work in Joseki. 
            Model model = TDBFactory.assembleModel(assemblerFile) ;
          
        }
        
        // Or even just assemble.  The TDBFactory operation is a convenience wrapper for looking
        // for the description by type. 
        {
            Model spec = FileManager.get().loadModel(assemblerFile) ;

            // Find the right starting point for the description in some way.
            Resource root = null ;
            
            if ( false )
                // If you know the Resource URI:
                root = spec.createResource("http://example/myChoiceOfURI" );
            else
            {
                // Alternatively, look for the a singel reource of the right type. 
                try {
                    // Find the required description - the file can contain descriptions of many different types.
                    root = GraphUtils.findRootByType(spec, PGraphAssemblerVocab.PGraphType) ;
                    if ( root == null )
                        throw new JenaException("Failed to find a suitable root") ;
                } catch (TypeNotUniqueException ex)
                { throw new JenaException("Multiple types for: "+DatasetAssemblerVocab.tDataset) ; }
            }
            
            Model model = (Model) Assembler.general.open(root) ;
        }
    }
    
    
}

/*
 * (c) Copyright 2008 Hewlett-Packard Development Company, LP
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