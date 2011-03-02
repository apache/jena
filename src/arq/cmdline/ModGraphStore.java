/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package arq.cmdline;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.sparql.core.assembler.AssemblerUtils ;
import com.hp.hpl.jena.update.GraphStore ;
import com.hp.hpl.jena.update.GraphStoreFactory ;

public class ModGraphStore extends ModDatasetGeneralAssembler
{
    GraphStore graphStore = null ;
    
    @Override
    public void registerWith(CmdGeneral cmdLine)
    {
        super.registerWith(cmdLine) ;
    }

    @Override
    public void processArgs(CmdArgModule cmdLine)
    {
        super.processArgs(cmdLine) ;
    }

    public ModGraphStore()
    {
        // Wire in assmebler implementations
        AssemblerUtils.init() ;
    }
    
    public GraphStore getGraphStore()
    {
        if ( graphStore == null )
            graphStore = createGraphStore() ;
        return graphStore ;
    }
    
    public GraphStore createGraphStore()
    {
        Dataset ds = createDataset() ;
        if ( ds == null )
            return GraphStoreFactory.create() ;
        return GraphStoreFactory.create(ds) ;
//        
//        // Default to a simple in-memory one.
//        if ( getAssemblerFile() == null )
//            return GraphStoreFactory.create() ;
//        
//        try {
//            // Try as graph store.
//            graphStore = (GraphStore)create(DatasetAssemblerVocab.tGraphStore) ;
//        } 
//        catch (AssemblerException ex) {}
//        catch (ARQException ex)
//        {
//            ex.printStackTrace(System.err) ;
//        }
//
//        // Try as dataset
//        if ( graphStore == null )
//        {
//            try {
//                Dataset ds = (Dataset)create(DatasetAssemblerVocab.tDataset) ;
//                if ( ds != null )
//                    graphStore = GraphStoreFactory.create(ds) ;
//            } catch (AssemblerException ex) { ex.printStackTrace(System.err) ;}
//            catch (ARQException ex)
//            {
//                ex.printStackTrace(System.err) ;
//            }
//        }
//        if ( graphStore == null )
//            throw new CmdException("Failed to find a dataset or graph store assembler description") ;
//        return graphStore ;
    }

}

/*
 * (c) Copyright 2008, 2009 Hewlett-Packard Development Company, LP
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