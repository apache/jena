/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sparql.resultset;

import java.util.ArrayList ;

import com.hp.hpl.jena.rdf.model.Literal ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.Resource ;
import com.hp.hpl.jena.rdf.model.Statement ;
import com.hp.hpl.jena.rdf.model.StmtIterator ;
import com.hp.hpl.jena.shared.JenaException ;
import com.hp.hpl.jena.shared.PropertyNotFoundException ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.binding.Binding ;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory ;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap ;
import org.openjena.atlas.logging.Log ;
import com.hp.hpl.jena.sparql.vocabulary.ResultSetGraphVocab ;
import com.hp.hpl.jena.vocabulary.RDF ;


public class RDFInput extends ResultSetMem
{
    /** Process a result set encoded in RDF according to
     *  <code>@link{http://www.w3.org/2001/sw/DataAccess/tests/result-set}#</code>
     *
     * @param model
     */
    public RDFInput(Model model)
    {
        buildFromDumpFormat(model);
    }

    // Convert from RDF model to in-memory result set
    private void buildFromDumpFormat(Model resultsModel)
    {
        varNames = new ArrayList<String>() ;
        StmtIterator sIter = resultsModel.listStatements(null, RDF.type, ResultSetGraphVocab.ResultSet) ;
        for ( ; sIter.hasNext() ;)
        {
            // For each root
            Statement s = sIter.nextStatement() ;
            Resource root = s.getSubject() ;
            buildOneResource(root) ;
        }
        sIter.close() ;
        reset() ;
    }    
        
    private void buildOneResource(Resource root)
    {
        buildVariables(root) ;
        int count = buildPreprocess(root) ;
        if ( root.getModel().contains(null, ResultSetGraphVocab.index, (RDFNode)null) )
            buildRowsOrdered(root, count) ;
        else
            buildRows(root) ;
    }
        
    private void buildVariables(Resource root)
    {
        // Variables
        StmtIterator rVarsIter = root.listProperties(ResultSetGraphVocab.resultVariable) ;
        for ( ; rVarsIter.hasNext() ; )
        {
            String varName = rVarsIter.nextStatement().getString() ;
            varNames.add(varName) ;
        }
        rVarsIter.close() ;
    }

    private int buildPreprocess(Resource root)
    {
        StmtIterator solnIter = root.listProperties(ResultSetGraphVocab.solution) ;
        int rows = 0 ;
        int indexed = 0 ;
        for ( ; solnIter.hasNext() ; )
        {
            Resource soln = solnIter.nextStatement().getResource() ;
            rows++ ;
            if ( soln.hasProperty(ResultSetGraphVocab.index) )
                indexed++ ;
        }
        solnIter.close() ;
        if ( indexed > 0 && rows != indexed )
        {
            Log.warn(this, "Rows = "+rows+" but only "+indexed+" indexes" ) ;
            return rows ;
        }
        return rows ;
    }

    private void buildRowsOrdered(Resource root, int count)
    {
        Model m  = root.getModel() ;
        // Assume one result set per file.
        for ( int index = 1 ; ; index++ )
        {
            Literal ind = m.createTypedLiteral(index) ;
            StmtIterator sIter = m.listStatements(null, ResultSetGraphVocab.index, ind) ;
            if ( ! sIter.hasNext() )
                break ;
            Statement s = sIter.nextStatement() ;
            if ( sIter.hasNext() )
                Log.warn(this, "More than one solution: index = "+index) ;
            Resource soln = s.getSubject() ;

            Binding rb = buildBinding(soln) ;
            rows.add(rb) ;
            sIter.close() ;
        }
        if ( rows.size() != count )
            Log.warn(this, "Found "+rows.size()+": expected "+count) ;
    }
    
    private void buildRows(Resource root)
    {
        // Now the results themselves
        int count = 0 ;
        StmtIterator solnIter = root.listProperties(ResultSetGraphVocab.solution) ;
        for ( ; solnIter.hasNext() ; )
        {
            Resource soln = solnIter.nextStatement().getResource() ;
            count++ ;

            Binding rb = buildBinding(soln) ;
            rows.add(rb) ;
        }
        solnIter.close() ;
        
        if ( root.hasProperty(ResultSetGraphVocab.size))
        {
            try {
                int size = root.getRequiredProperty(ResultSetGraphVocab.size).getInt() ;
                if ( size != count )
                    Log.warn(this, "Warning: Declared size = "+size+" : Count = "+count) ;
            } catch (JenaException rdfEx) {}
        }
    }

    private Binding buildBinding(Resource soln)
    {
        // foreach row
        BindingMap rb = BindingFactory.create() ;
        
        StmtIterator bindingIter = soln.listProperties(ResultSetGraphVocab.binding) ;
        for ( ; bindingIter.hasNext() ; )
        {
            Resource binding = bindingIter.nextStatement().getResource() ;
            
            String var = binding.getRequiredProperty(ResultSetGraphVocab.variable).getString() ;
            try {
                RDFNode val = binding.getRequiredProperty(ResultSetGraphVocab.value).getObject() ;
                rb.add(Var.alloc(var), val.asNode()) ;
            } catch (PropertyNotFoundException ex)
            {
                Log.warn(this, "Failed to get value for ?"+var) ;
            }
            
            // We include the value even if it is the marker term "rs:undefined"
            //if ( val.equals(ResultSetVocab.undefined))
            //    continue ;
            // The ResultSetFormatter code equates null (not found) with
            // rs:undefined.  When Jena JUnit testing, it does not matter if the
            // recorded result has the term absent or explicitly undefined.
            
        }
        bindingIter.close() ;
        return rb ;
    }
}

/*
 * (c) Copyright 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
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