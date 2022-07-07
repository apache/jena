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

package org.apache.jena.sparql.resultset;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Iterator;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.vocabulary.ResultSetGraphVocab;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.RDF;


public class RDFOutput
{
    private boolean reportAllVars = false;
    private boolean includeTypeProperties = false;

    public RDFOutput() { }

    public boolean getAllVars() { return reportAllVars; }
    /** Set whether all variables, not just selected ones, are recorded */
    public void setAllVars(boolean all) { reportAllVars = all; }

    /**
     * Encode the result set as RDF.
     *
     * @return Model Model contains the results
     */
    public Model asModel(ResultSet resultSet) {
        Model m = GraphFactory.makeJenaDefaultModel();
        asRDF(m, resultSet);
        if ( m.getNsPrefixURI("rs") == null )
            m.setNsPrefix("rs", ResultSetGraphVocab.getURI());
        if ( m.getNsPrefixURI("rdf") == null )
            m.setNsPrefix("rdf", RDF.getURI());
        return m;
    }

    /** Encode the result set as RDF in the model provided.
     *
     * @param model     The place where to put the RDF.
     * @return Resource The resource for the result set.
     */
    public Resource asRDF(Model model, ResultSet resultSet) {
        return asRDF(model, resultSet, false);
    }

    public Resource asRDF(Model model, ResultSet resultSet, boolean includeRowIndex) {
        Resource results = model.createResource();
        // This always goes in.
        results.addProperty(RDF.type, ResultSetGraphVocab.ResultSet);

        for ( String vName : resultSet.getResultVars() )
            results.addProperty(ResultSetGraphVocab.resultVariable, vName);

        int count = 0;
        for (; resultSet.hasNext(); ) {
            count++;
            QuerySolution rBind = resultSet.nextSolution();
            Resource thisSolution = model.createResource();
            if ( includeTypeProperties )
                thisSolution.addProperty(RDF.type, ResultSetGraphVocab.ResultSolution);
            results.addProperty(ResultSetGraphVocab.solution, thisSolution);
            if ( includeRowIndex ) {
                // This can lead to equivalent result sets having different graphs
                // Best used if and only if query was completely sorted.
                Literal x = model.createTypedLiteral(count + "", XSDDatatype.XSDinteger);
                thisSolution.addLiteral(ResultSetGraphVocab.index, x);
            }

            Iterator<String> iter = getAllVars() ? rBind.varNames() : resultSet.getResultVars().iterator();

            for (; iter.hasNext(); ) {
                Resource thisBinding = model.createResource();
                String rVar = iter.next();
                RDFNode n = rBind.get(rVar);

                if ( n == null )
                    continue;

//                if ( ! explicitUndefinedTerm && n == null )
//                    continue;
//
//                if ( n == null ) {
//                    if ( !explicitUndefinedTerm )
//                        continue;
//                    // This variable was not found in the results.
//                    // Encode the result set with an explicit "not defined"
//                    n = ResultSetVocab.undefined;
//                }
                if ( includeTypeProperties )
                    thisBinding.addProperty(RDF.type, ResultSetGraphVocab.ResultBinding);
                thisBinding.addProperty(ResultSetGraphVocab.variable, rVar);
                thisBinding.addProperty(ResultSetGraphVocab.value, n);
                thisSolution.addProperty(ResultSetGraphVocab.binding, thisBinding);
            }
        }
        results.addProperty(ResultSetGraphVocab.size, model.createTypedLiteral(count));
        addPrefixes(model);
        return results;
    }

    // Boolean results
    public Model asModel(boolean result) {
        Model m = GraphFactory.makeJenaDefaultModel();
        encodeAsRDF(m, result);
        addPrefixes(m);
        return m;
    }

    private void addPrefixes(Model model) {
        if ( model.getNsPrefixURI("rs") == null )
            model.setNsPrefix("rs", ResultSetGraphVocab.getURI());
        if ( model.getNsPrefixURI("rdf") == null )
            model.setNsPrefix("rdf", RDF.getURI());
        if ( model.getNsPrefixURI("xsd") == null )
            model.setNsPrefix("xsd", XSDDatatype.XSD + "#");
    }

    public Resource asRDF(Model model, boolean result) {
        Resource results = model.createResource();
        results.addProperty(RDF.type, ResultSetGraphVocab.ResultSet);
        Literal lit = model.createTypedLiteral(result);
        results.addProperty(ResultSetGraphVocab.p_boolean, lit);
        return results;
    }

    /**
     * Encode a boolean result set as RDF.
     * @param booleanResult
     * @return Model       Model contains the results
     */
    public static Model encodeAsModel(boolean booleanResult) {
        RDFOutput rOut = new RDFOutput();
        return rOut.asModel(booleanResult);
    }

    /** Encode the result set as RDF.
     * @param  resultSet
     * @return Model       Model contains the results
     */
    public static Model encodeAsModel(ResultSet resultSet) {
        RDFOutput rOut = new RDFOutput();
        return rOut.asModel(resultSet);
    }

    /** Encode the boolean as RDF in the model provided.
     *
     * @param  model     The place where to put the RDF.
     * @param  booleanResult
     * @return Resource  The resource for the result set.
     */
    public static Resource encodeAsRDF(Model model, boolean booleanResult) {
        RDFOutput rOut = new RDFOutput();
        return rOut.asRDF(model, booleanResult);
    }

    /** Encode the result set as RDF in the model provided.
     *
     * @param  model     The place where to put the RDF.
     * @param  resultSet
     * @return Resource  The resource for the result set.
     */
    public static Resource encodeAsRDF(Model model, ResultSet resultSet) {
        RDFOutput rOut = new RDFOutput();
        return rOut.asRDF(model, resultSet);
    }

    /** Write out an RDF model that encodes the result set.
     *  See also the same method taking an output stream.
     *
     * @param out           Output : ideally, should be a UTF-8 print writer (not system default)
     * @param format        Name of RDF format (names as Jena writers)
     * @param resultSet     The result set to encode in RDF
     */
    private static void outputAsRDF(PrintWriter out, String format, ResultSet resultSet) {
        Model m = RDFOutput.encodeAsModel(resultSet);
        m.write(out, format);
        out.flush();
    }

    /** Write out an RDF model that encodes the result set
     *
     * @param outStream     Output
     * @param format        Name of RDF format (names as Jena writers)
     * @param resultSet     The result set to encode in RDF
     */
    public static void outputAsRDF(OutputStream outStream, String format, ResultSet resultSet) {
        PrintWriter out = FileUtils.asPrintWriterUTF8(outStream);
        RDFOutput.outputAsRDF(out, format, resultSet);
        out.flush();
    }

    /** Write out an RDF model that encodes the result set
     *
     * @param format        Name of RDF format (names as Jena writers)
     * @param resultSet     The result set to encode in RDF
     */
    public static void outputAsRDF(String format, ResultSet resultSet)
    { RDFOutput.outputAsRDF(System.out, format, resultSet); }

    /** Write out an RDF model that encodes a boolean result
     *
     * @param format        Name of RDF format (names as Jena writers)
     * @param booleanResult The boolean result to encode in RDF
     */
    public static void outputAsRDF(String format,  boolean booleanResult)
    { RDFOutput.outputAsRDF(System.out, format, booleanResult); }

    /** Write out an RDF model that encodes a boolean result
     *
     * @param outStream     Output
     * @param format        Name of RDF format (names as Jena writers)
     * @param booleanResult The boolean result to encode in RDF
     */
    public static void outputAsRDF(OutputStream outStream, String format, boolean booleanResult) {
        PrintWriter out = FileUtils.asPrintWriterUTF8(outStream);
        RDFOutput.outputAsRDF(out, format, booleanResult);
        out.flush();
    }

    /** Write out an RDF model that encodes a boolean result.
     *  See also the same method taking an output stream.
     *
     * @param out           Output : ideally, should be a UTF-8 print writer (not system default)
     * @param format        Name of RDF format (names as Jena writers)
     * @param booleanResult The boolean result to encode in RDF
     */
    private static void outputAsRDF(PrintWriter out, String format, boolean booleanResult) {
        Model m = RDFOutput.encodeAsModel(booleanResult);
        m.write(out, format);
        out.flush();
    }
}
