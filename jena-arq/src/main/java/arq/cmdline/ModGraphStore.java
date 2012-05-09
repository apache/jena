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
