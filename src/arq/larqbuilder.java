/**
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

package arq;

import java.util.Iterator ;

import arq.cmdline.ArgDecl ;
import arq.cmdline.CmdLARQ ;
import arq.cmdline.ModDatasetGeneralAssembler ;
import arq.cmdline.ModLARQindex ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.larq.IndexBuilderModel ;
import com.hp.hpl.jena.query.larq.IndexBuilderString ;
import com.hp.hpl.jena.query.larq.IndexBuilderSubject ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.StmtIterator ;

public class larqbuilder extends CmdLARQ
{
    ModDatasetGeneralAssembler modDataset = new ModDatasetGeneralAssembler() ;
    ModLARQindex modIndex = new ModLARQindex() ;
    
    ArgDecl argNodes = new ArgDecl(ArgDecl.NoValue, "nodes", "subjects")  ;
    private boolean indexSubjects ;
    
    // --larq filename 
    public static void main(String... argv)
    {
        new larqbuilder(argv).mainRun() ;
    }
    
    protected larqbuilder(String[] argv)
    {
        super(argv) ;
        super.addModule(modDataset) ;
        super.addModule(modIndex) ;
        super.add(argNodes, "--subjects", "Index literals to subject nodes") ;
    }

    @Override
    protected String getSummary()
    {
        return "larqbuilder --larq DIR [--subjects] --data RDF" ;
    }

    @Override
    protected void processModulesAndArgs()
    {
        super.processModulesAndArgs() ;
        indexSubjects = super.contains(argNodes) ;
    }
    
    @Override
    protected void exec()
    {
        // ---- Read and index all literal strings.
        IndexBuilderModel larqBuilder = 
            indexSubjects ? new IndexBuilderSubject(modIndex.getIndexWriter()) :
                            new IndexBuilderString(modIndex.getIndexWriter()) ; 
        Dataset ds = modDataset.getDataset() ;
        index(larqBuilder, ds.getDefaultModel()) ;
        for ( Iterator<String> iter = ds.listNames() ; iter.hasNext() ; )
        {
            String g = iter.next() ;
            index(larqBuilder, ds.getNamedModel(g)) ;
        }
        
        larqBuilder.closeWriter() ;
    }

    private void index(IndexBuilderModel larqBuilder, Model model)
    {
        StmtIterator sIter = model.listStatements() ;
        larqBuilder.indexStatements(sIter) ;
    }

}
