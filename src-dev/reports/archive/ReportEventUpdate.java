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

package reports.archive;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.rdf.listeners.StatementListener ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelChangedListener ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;
import com.hp.hpl.jena.rdf.model.Property ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.sparql.core.DSG_Mem ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.update.UpdateAction ;
import com.hp.hpl.jena.update.UpdateFactory ;
import com.hp.hpl.jena.update.UpdateRequest ;


public class ReportEventUpdate
{
    public static void main(String ...args)
    {
        DatasetGraph dsg = new DSG_Mem() ;
        
        //Dataset ds = DatasetFactory.create() ;
        //Dataset ds = DatasetFactory.create(dsg) ;
        Dataset ds = DatasetFactory.create(ModelFactory.createModelForGraph(dsg.getDefaultGraph())) ;
        
        
        
        exec(ds) ;
        exec(DatasetFactory.create()) ;
        
    }
    
    
    static void exec(Dataset ds)
    {
        Model m = ds.getDefaultModel() ;
        
        // Works for graph backed dataset.
        ModelChangedListener listener = new StatementListener()
        {
            @Override
            public void addedStatement( Statement s )
            {
             System.out.println("Add: "+s) ;
            }
            @Override
            public void removedStatement( Statement s )
            {
                System.out.println("Remove: "+s) ;
            }
        } ;
        
        m.register(listener) ;
        
        Resource s = m.createResource("http://example/s") ;
        Property p = m.createProperty("http://example/o") ;
        m.add(s, p, "123") ;
        
        UpdateRequest request = UpdateFactory.create("BASE <http://example> INSERT DATA { <s> <p> <o> }") ;
        UpdateAction.execute(request, ds) ;
        
        System.out.print(ds.asDatasetGraph()) ;
        System.out.println("DONE") ;
        
        
        
    }
}
