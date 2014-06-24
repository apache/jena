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

package com.hp.hpl.jena.assembler.assemblers;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.*;

public class OntModelAssembler extends InfModelAssembler implements Assembler
    {
    @Override public Model openEmptyModel( Assembler a, Resource root, Mode mode )
        {
        checkType( root, JA.OntModel );
        Model baseModel = getBase( a, root, mode );
        OntModelSpec oms = getOntModelSpec( a, root );
        OntModel om = ModelFactory.createOntologyModel( oms, baseModel );
        addSubModels( a, root, mode, om );
        return om;
        }

    private void addSubModels( Assembler a, Resource root, Mode mode, OntModel om )
        {
        List<Model> subModels = getSubModels( a, root, mode );
            for ( Model subModel : subModels )
            {
                om.addSubModel( subModel );
            }
        }

    private List<Model> getSubModels( Assembler a, Resource root, Mode mode )
        {
        List<Model> result = new ArrayList<>();
        for (StmtIterator it = root.listProperties( JA.subModel ); it.hasNext();)
            result.add( a.openModel( it.nextStatement().getResource(), mode ) );
        return result;
        }

    private static final OntModelSpec defaultSpec = OntModelSpec.OWL_MEM_RDFS_INF;

    protected OntModelSpec getOntModelSpec( Assembler a, Resource root )
        {
        Resource r = getUniqueResource( root, JA.ontModelSpec );
        return r == null ? defaultSpec : (OntModelSpec) a.open( r );
        }
    }
