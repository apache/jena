/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
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

/*
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