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

package org.apache.jena.sparql.core.assembler;

import static org.apache.jena.sparql.core.assembler.DatasetAssemblerVocab.*;

import java.util.Objects;

import org.apache.jena.assembler.Assembler;
import org.apache.jena.assembler.JA;
import org.apache.jena.assembler.assemblers.AssemblerGroup;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.QuerySolutionMap;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.MappingRegistry;
import org.apache.jena.sparql.util.Symbol;
import org.apache.jena.sparql.util.TypeNotUniqueException;
import org.apache.jena.sparql.util.graph.GraphUtils;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.system.Txn;
import org.apache.jena.vocabulary.RDFS;

public class AssemblerUtils
{
    // Wrappers for reading things form a file - assumes one of the thing per file.
    public static PrefixMapping readPrefixMapping(String file)
    {
        PrefixMapping pm = (PrefixMapping)AssemblerUtils.build(file, JA.PrefixMapping);
        return pm;
    }

    private static boolean initialized = false;
    private static AssemblerGroup generalAssembler = Assembler.general();

    static { JenaSystem.init(); }

    static public void init()
    {
        if ( initialized )
            return;
        initialized = true;
        registerDataset(tDataset,         new DatasetAssemblerGeneral());
        registerDataset(tDatasetOne,      new DatasetOneAssembler());
        registerDataset(tDatasetZero,     new DatasetZeroAssembler());
        registerDataset(tDatasetSink,     new DatasetSinkAssembler());
        registerDataset(tMemoryDataset,   new InMemDatasetAssembler());
        registerDataset(tDatasetTxnMem,   new InMemDatasetAssembler());
        registerDataset(tDatasetView,     new ViewDatasetAssembler());
        registerModel(tViewGraph,         new ViewGraphAssembler());
    }

    private static Model modelExtras = ModelFactory.createDefaultModel();

    // Legacy. Supports assemblers using a general "rdf:type ja:dataset"
    private static Resource datasetAssemblerType = DatasetAssemblerVocab.tDataset;

    /** Register an assembler that creates a dataset */
    static public void registerDataset(Resource r, Assembler a) {
       register(generalAssembler, r, a, datasetAssemblerType);
    }

    /** Register an assembler that creates a dataset */
    static public void registerModel(Resource r, Assembler a) {
        register(generalAssembler, r, a, JA.Model);
    }

    /** Register an additional assembler */
    static public void register(AssemblerGroup g, Resource r, Assembler a, Resource superType) {
        registerAssembler(g, r, a);
        if ( superType != null && ! superType.equals(r) ) {
            // This is called during Jena-wide initialization.
            // Use function for constant (JENA-1294)
           modelExtras.add(r, RDFS.Init.subClassOf(), superType);
        }
    }

    /** register */
    public static void registerAssembler(AssemblerGroup group, Resource r, Assembler a) {
        if ( group == null )
            group = generalAssembler;
        group.implementWith(r, a);
        // assemblerAssertions.add(r, RDFS.subClassOf, JA.Object);
    }

    /**
     * Read a file and make ready for use with assemblers.
     * It calls {@link #prepareForAssembler}.
     */
    public static Model readAssemblerFile(String assemblerFile) {
        Model spec = null;
        try {
            spec = RDFDataMgr.loadModel(assemblerFile);
        } catch (Exception ex) {
            throw new ARQException("Failed reading assembler description: " + ex.getMessage());
        }
        prepareForAssembler(spec);
        return spec;
    }

    /** Add any extra information to the model.
     * Such information includes registration of datasets (e.g. TDB1, TDB2)
     * done by {@link #register} ({@link #registerDataset}, {@link #registerModel}
     * and adding subclass relationship used for models.
     * It avoids directly modifying {@link Assembler#general}.
     * @param model
     * @return Model The same model after modification.
     */
    public static Model prepareForAssembler(Model model) {
        model.add(modelExtras);
        return model;
    }

    /** @deprecated Use {@link #prepareForAssembler(Model)} */
    @Deprecated(forRemoval = true)
    public static Model addRegistered(Model model) {
        return prepareForAssembler(model);
    }

    public static Object build(String assemblerFile, String typeURI) {
        Resource type = ResourceFactory.createResource(typeURI);
        return build(assemblerFile, type);
    }

    public static Object build(String assemblerFile, Resource type) {
        Objects.requireNonNull(assemblerFile, "No assembler file");
        Model spec = readAssemblerFile(assemblerFile);
        return build(spec, type);
    }

    public static Object build(Model assemblerModel, Resource type) {
        Resource root = null;
        try {
            root = GraphUtils.findRootByType(assemblerModel, type);
            if ( root == null )
                throw new ARQException("No such type: <" + type + ">");
        } catch (TypeNotUniqueException ex) {
            throw new ARQException("Multiple types for: " + tDataset);
        }
        return generalAssembler.open(root);
    }
    /** Look for and build context declarations.
     * e.g.
     * <pre>
     * root ...;
     *   ja:context [ ja:cxtName "arq:queryTimeout";  ja:cxtValue "10000" ];
     *   ...
     * </pre>
     * Short name forms of context parameters can be used.
     * Setting as string "undef" will remove the context setting.
     * Returns null when there is no {@link JA#context} on the resource.
     */
    public static Context parseContext(Resource r)
    {
        if ( ! r.hasProperty(JA.context ) )
            return null;
        Context context = new Context();
        mergeContext(r, context);
        return context;
    }

    /**
     * Process {@code ja:data}.
     * <p>
     * The object value refers to a file, either by string name or a {@code file:}
     * URI. If it is a string, a relative filename will be relative to the JVM
     * current directory. If it is a {@code file:} URI, a relative filename will be
     * relative to the assembler file and it's base URI.
     */
    public static void loadData(DatasetGraph dataset, Resource root) {
      Txn.executeWrite(dataset, ()->{
          // Load data into the default graph
          // This also loads quads into the dataset.
          GraphUtils.multiValueAsFilename(root, JA.data)
              .forEach(filename -> RDFDataMgr.read(dataset, filename));

          // load data into named graphs
          GraphUtils.multiValueResource(root, DatasetAssemblerVocab.pNamedGraph).forEach(namedGraphResource -> {
              final String graphName = GraphUtils.getAsStringValue(namedGraphResource, DatasetAssemblerVocab.pGraphName);
              if (namedGraphResource.hasProperty(JA.data)) {
                  GraphUtils.multiValueAsFilename(namedGraphResource, JA.data)
                      .forEach(filename -> {
                          Node gn = NodeFactory.createURI(graphName);
                          RDFDataMgr.read(dataset.getGraph(gn), filename);
                      });
              }
          });
      });
    }


    /** Look for and merge in context declarations.
     * e.g.
     * <pre>
     * root ...;
     *   ja:context [ ja:cxtName "arq:queryTimeout";  ja:cxtValue "10000" ];
     *   ...
     * </pre>
     * Short name forms of context parameters can be used.
     * Setting as string "undef" will remove the context setting.
     */
    public static void mergeContext(Resource r, Context context) {
        String qs = "PREFIX ja: <"+JA.getURI()+">\nSELECT * { ?x ja:context [ ja:cxtName ?name; ja:cxtValue ?value ] }";
        QuerySolutionMap qsm = new QuerySolutionMap();
        qsm.add("x", r);
        QueryExecution qExec = QueryExecution.model(r.getModel()).query(qs).substitution(qsm).build();
        ResultSet rs = qExec.execSelect();
        while ( rs.hasNext() )
        {
            QuerySolution soln = rs.next();
            String name = soln.getLiteral("name").getLexicalForm();
            String value = soln.getLiteral("value").getLexicalForm();  // Works for numbers as well!
            name = MappingRegistry.mapPrefixName(name);
            Symbol symbol = Symbol.create(name);
            if ( "undef".equalsIgnoreCase(value) )
                context.remove(symbol);
            else
                context.set(symbol, value);
        }
    }
}
