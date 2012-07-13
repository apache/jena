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

package com.hp.hpl.jena.sparql.util;


import java.util.List ;

import com.hp.hpl.jena.query.Dataset ;
import com.hp.hpl.jena.query.DatasetFactory ;
import com.hp.hpl.jena.query.Query ;
import com.hp.hpl.jena.query.QueryExecution ;
import com.hp.hpl.jena.query.QueryExecutionFactory ;
import com.hp.hpl.jena.query.QueryFactory ;
import com.hp.hpl.jena.query.QuerySolution ;
import com.hp.hpl.jena.query.ResultSet ;
import com.hp.hpl.jena.query.ResultSetFactory ;
import com.hp.hpl.jena.query.ResultSetFormatter ;
import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.RDFNode ;
import com.hp.hpl.jena.rdf.model.RDFWriter ;
import com.hp.hpl.jena.shared.PrefixMapping ;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl ;
import com.hp.hpl.jena.sparql.ARQConstants ;
import com.hp.hpl.jena.sparql.ARQException ;
import com.hp.hpl.jena.sparql.algebra.Algebra ;
import com.hp.hpl.jena.sparql.algebra.Op ;
import com.hp.hpl.jena.sparql.algebra.OpVars ;
import com.hp.hpl.jena.sparql.algebra.op.OpProject ;
import com.hp.hpl.jena.sparql.core.DatasetGraph ;
import com.hp.hpl.jena.sparql.core.Prologue ;
import com.hp.hpl.jena.sparql.core.Var ;
import com.hp.hpl.jena.sparql.engine.QueryIterator ;
import com.hp.hpl.jena.sparql.resultset.PlainFormat ;
import com.hp.hpl.jena.sparql.resultset.ResultSetApply ;
import com.hp.hpl.jena.sparql.resultset.ResultsFormat ;
import com.hp.hpl.jena.sparql.vocabulary.ResultSetGraphVocab ;

/** Some utilities for query processing. */
public class QueryExecUtils
{
    protected static PrefixMapping globalPrefixMap = new PrefixMappingImpl() ;
    static {
        globalPrefixMap.setNsPrefix("rdf",  ARQConstants.rdfPrefix) ;
        globalPrefixMap.setNsPrefix("rdfs", ARQConstants.rdfsPrefix) ;
        globalPrefixMap.setNsPrefix("xsd",  ARQConstants.xsdPrefix) ;

        globalPrefixMap.setNsPrefix("rs",  ResultSetGraphVocab.getURI()) ;
        //globalPrefixMap.setNsPrefix("owl" , ARQConstants.owlPrefix) ;
    }

    public static void executeQuery(Query query, QueryExecution queryExecution)
    {
        executeQuery(query,  queryExecution, ResultsFormat.FMT_TEXT) ;
    }

    public static void executeQuery(Query query, QueryExecution queryExecution, ResultsFormat outputFormat)
    {
        if ( query.isSelectType() )
            doSelectQuery(query, queryExecution, outputFormat) ;
        if ( query.isDescribeType() )
            doDescribeQuery(query, queryExecution, outputFormat) ;
        if ( query.isConstructType() )
            doConstructQuery(query, queryExecution, outputFormat) ;
        if ( query.isAskType() )
            doAskQuery(query, queryExecution, outputFormat) ;
        queryExecution.close() ;
    }

    public static void execute(Op op, DatasetGraph dsg)
    {
        execute(op, dsg, ResultsFormat.FMT_TEXT) ;
    }
    
    public static void execute(Op op, DatasetGraph dsg, ResultsFormat outputFormat)
    {
        QueryIterator qIter = Algebra.exec(op, dsg) ;

        List<String> vars = null ;
        if ( op instanceof OpProject )
            vars = Var.varNames(((OpProject)op).getVars()) ;
        else
            // The variables defined in patterns (not Filters, nor NOT EXISTS, nor ORDER BY)
            vars = Var.varNames(OpVars.patternVars(op)) ;

        ResultSet results = ResultSetFactory.create(qIter, vars) ;
        outputResultSet(results, null, outputFormat) ;
    }
    
    public static void outputResultSet(ResultSet results, Prologue prologue, ResultsFormat outputFormat)
    {
        boolean done = false ;
        if ( prologue == null )
            prologue = new Prologue(globalPrefixMap) ;

        if ( outputFormat.equals(ResultsFormat.FMT_UNKNOWN) )
            outputFormat = ResultsFormat.FMT_TEXT ;
        
        if ( outputFormat.equals(ResultsFormat.FMT_NONE) ||
             outputFormat.equals(ResultsFormat.FMT_COUNT) )
        {
            int count = ResultSetFormatter.consume(results) ;
            if ( outputFormat.equals(ResultsFormat.FMT_COUNT) )
            {
                System.out.println("Count = "+count) ;
            }
            done = true ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RS_RDF) ||
             outputFormat.equals(ResultsFormat.FMT_RDF_N3) ||
             outputFormat.equals(ResultsFormat.FMT_RDF_TTL) )
        {
            Model m = ResultSetFormatter.toModel(results) ;
            m.setNsPrefixes(prologue.getPrefixMapping()) ;
            RDFWriter rdfw = m.getWriter("TURTLE") ;
            m.setNsPrefix("rs", ResultSetGraphVocab.getURI()) ;
            rdfw.write(m, System.out, null) ;
            done = true ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RS_XML) )
        {
            ResultSetFormatter.outputAsXML(System.out, results) ;
            done = true ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RS_JSON) )
        {
            ResultSetFormatter.outputAsJSON(System.out, results) ;
            done = true ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RS_SSE) )
        {
            ResultSetFormatter.outputAsSSE(System.out, results, prologue) ;
            done = true ;
        }
        
        if ( outputFormat.equals(ResultsFormat.FMT_TEXT) )
        {
            ResultSetFormatter.out(System.out, results, prologue) ;
            done = true ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_TUPLES) )
        {
            PlainFormat pFmt = new PlainFormat(System.out, prologue) ;
            ResultSetApply a = new ResultSetApply(results, pFmt) ;
            a.apply() ;
            done = true ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RS_CSV ) )
        {
            ResultSetFormatter.outputAsCSV(System.out, results) ;
            done = true ;
        }
        
        if ( outputFormat.equals(ResultsFormat.FMT_RS_TSV ) )
        {
            ResultSetFormatter.outputAsTSV(System.out, results) ;
            done = true ;
        }
        
        if ( outputFormat.equals(ResultsFormat.FMT_RS_BIO ) )
        {
            ResultSetFormatter.outputAsBIO(System.out, results) ;
            done = true ;
        }
        
        if ( ! done )
            System.err.println("Unknown format request: "+outputFormat) ;
        results = null ;

        System.out.flush() ;
    }

    private static void doSelectQuery(Query query, QueryExecution qe, ResultsFormat outputFormat)
    {
        if ( outputFormat == null || outputFormat == ResultsFormat.FMT_UNKNOWN )
            outputFormat = ResultsFormat.FMT_TEXT ; 
        ResultSet results = qe.execSelect() ;
        outputResultSet(results, query, outputFormat) ;
    }


    private static void doDescribeQuery(Query query, QueryExecution qe, ResultsFormat outputFormat)
    {
        if ( outputFormat == null || outputFormat == ResultsFormat.FMT_UNKNOWN )
            outputFormat = ResultsFormat.FMT_RDF_TTL ;

        Model r = qe.execDescribe() ;
        writeModel(query, r, outputFormat) ;
    }


    private static void doConstructQuery(Query query, QueryExecution qe, ResultsFormat outputFormat)
    {
        if ( outputFormat == null || outputFormat == ResultsFormat.FMT_UNKNOWN )
            outputFormat = ResultsFormat.FMT_RDF_TTL ;

        Model r = qe.execConstruct() ;
        writeModel(query, r, outputFormat) ;
    }

    private static void writeModel(Query query, Model model, ResultsFormat outputFormat)
    {
        if ( outputFormat == null || outputFormat == ResultsFormat.FMT_UNKNOWN )
            outputFormat = ResultsFormat.FMT_TEXT ;

        if ( outputFormat.equals(ResultsFormat.FMT_NONE) )
            return ;

        if ( outputFormat.equals(ResultsFormat.FMT_TEXT))
        {
            String qType = "" ;
            if ( query.isDescribeType() ) qType = "DESCRIBE" ;
            if ( query.isConstructType() ) qType = "CONSTRUCT" ;

            System.out.println("# ======== "+qType+" results ") ;
            model.write(System.out, "N3", null) ; // Base is meaningless
            System.out.println("# ======== ") ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_XML) )
        {
            model.write(System.out, "RDF/XML-ABBREV", null) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_TTL) )
        {
            model.write(System.out, "N3", null) ;
            return ;
        }
        
        if ( outputFormat.equals(ResultsFormat.FMT_RDF_N3) )
        {
            model.write(System.out, "N3", null) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_NT) )
        {
            model.write(System.out, "N-TRIPLES", null) ;
            return ;
        }

        System.err.println("Unknown format: "+outputFormat) ;
    }

    private static void doAskQuery(Query query, QueryExecution qe, ResultsFormat outputFormat)
    {
        boolean b = qe.execAsk() ;

        if ( outputFormat == null || outputFormat == ResultsFormat.FMT_UNKNOWN )
            outputFormat = ResultsFormat.FMT_TEXT ;

        if ( outputFormat.equals(ResultsFormat.FMT_RS_XML) )
        {
            ResultSetFormatter.outputAsXML(System.out, b) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RDF_N3) || 
        outputFormat.equals(ResultsFormat.FMT_RDF_TTL) )
        {
            ResultSetFormatter.outputAsRDF(System.out, "TURTLE", b) ;
            System.out.flush() ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RS_JSON) )
        {
            ResultSetFormatter.outputAsJSON(System.out, b) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_TEXT) )
        {
            //ResultSetFormatter.out(System.out, b) ;
            System.out.println("Ask => "+(b?"Yes":"No")) ;
            return ;
        }

        if ( outputFormat.equals(ResultsFormat.FMT_RS_CSV) )
        {
            ResultSetFormatter.outputAsCSV(System.out, b) ;
            return ;
        }
        
        System.err.println("Unknown format: "+outputFormat) ;
    }
    
    /** Execute a query, expecting the result to be one row, one column.  Return that one RDFNode */
    public static RDFNode getExactlyOne(String qs, Model model)
    { return getExactlyOne(qs, DatasetFactory.create(model)) ; }
    
    /** Execute a query, expecting the result to be one row, one column.  Return that one RDFNode */
    public static RDFNode getExactlyOne(String qs, Dataset ds)
    {
        Query q = QueryFactory.create(qs) ;
        if ( q.getResultVars().size() != 1 )
            throw new ARQException("getExactlyOne: Must have exactly one result columns") ;
        String varname = q.getResultVars().get(0) ;
        QueryExecution qExec = QueryExecutionFactory.create(q, ds);
        return getExactlyOne(qExec, varname) ;
    }
    
    /** Execute, expecting the result to be one row, one column.  Return that one RDFNode or throw an exception */
    public static RDFNode getExactlyOne(QueryExecution qExec, String varname)
    {
        try {
            ResultSet rs = qExec.execSelect() ;
            
            if ( ! rs.hasNext() )
                throw new ARQException("Not found: var ?"+varname) ;

            QuerySolution qs = rs.nextSolution() ;
            RDFNode r = qs.get(varname) ;
            if ( rs.hasNext() )
                throw new ARQException("More than one: var ?"+varname) ;
            return r ;
        } finally { qExec.close() ; }
    }
    
    /** Execute, expecting the result to be one row, one column. 
     * Return that one RDFNode or null
     * Throw excpetion if more than one.
     */
    public static RDFNode getOne(QueryExecution qExec, String varname)
    {
        try {
            ResultSet rs = qExec.execSelect() ;
            
            if ( ! rs.hasNext() )
                return null ;

            QuerySolution qs = rs.nextSolution() ;
            RDFNode r = qs.get(varname) ;
            if ( rs.hasNext() )
                throw new ARQException("More than one: var ?"+varname) ;
            return r ;
        } finally { qExec.close() ; }
    }
        

}
