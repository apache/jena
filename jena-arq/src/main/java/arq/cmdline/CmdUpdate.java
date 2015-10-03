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

import org.apache.jena.query.Syntax ;
import org.apache.jena.rdf.model.ModelFactory ;
import org.apache.jena.sparql.core.DatasetGraph ;

public abstract class CmdUpdate extends CmdARQ
{
    protected ModDataset modDataset = null ;
    protected Syntax updateSyntax = Syntax.defaultUpdateSyntax ;

    protected CmdUpdate(String[] argv)
    {
        super(argv) ;
        modDataset = setModeDataset() ;
        addModule(modDataset) ;
    }
    
    protected ModDataset setModeDataset() {
        return new ModDatasetGeneralAssembler() ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        if ( super.cmdStrictMode )
            updateSyntax = Syntax.syntaxSPARQL_11 ;
    }
    
    @Override
    protected final void exec() {
        DatasetGraph dataset = modDataset.getDatasetGraph() ;
        if ( dataset == null )
            dataset = dealWithNoDataset() ;
        
        if ( dataset.getDefaultGraph() == null )
            dataset.setDefaultGraph(ModelFactory.createDefaultModel().getGraph()) ;
        execUpdate(dataset) ;
    }

    protected abstract DatasetGraph dealWithNoDataset() ;

    protected abstract void execUpdate(DatasetGraph graphStore) ;
}
