/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

package reports.archive;

import com.hp.hpl.jena.ontology.Individual ;
import com.hp.hpl.jena.ontology.OntClass ;
import com.hp.hpl.jena.ontology.OntModel ;
import com.hp.hpl.jena.ontology.OntModelSpec ;
import com.hp.hpl.jena.ontology.OntProperty ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

public class ReportDuplicateBNodes
{
    public static void main(String...argv)
    {
        String queryString = "SELECT * { ?s ?p ?o }" ;
        
        
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
        
        String NS = "http://mc-model.owl#";
        OntClass typeA = model.createClass(NS+"TypeA");
        OntClass typeB = model.createClass(NS+"TypeB");
        Individual indiA = model.createIndividual(NS+"IndiA", typeA);
        Individual indiB = model.createIndividual(NS+"IndiB", typeB);
        OntProperty rel = model.createOntProperty(NS+"rel");
        model.createStatement(indiA, rel, indiB).createReifiedStatement();
        Query query = QueryFactory.create(queryString);

        Model model2 = model.getBaseModel() ;
        QueryExecution qe = QueryExecutionFactory.create(query, model2);
        ResultSet results = qe.execSelect();
        ResultSetFormatter.out(results, query);

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