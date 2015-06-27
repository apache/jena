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

package org.apache.jena.tdb.modify;

import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.engine.binding.Binding ;
import org.apache.jena.sparql.modify.UpdateEngine ;
import org.apache.jena.sparql.modify.UpdateEngineFactory ;
import org.apache.jena.sparql.modify.UpdateEngineMain ;
import org.apache.jena.sparql.modify.UpdateEngineRegistry ;
import org.apache.jena.sparql.util.Context ;
import org.apache.jena.tdb.store.DatasetGraphTDB ;

public class UpdateEngineTDB extends UpdateEngineMain
{
    public UpdateEngineTDB(DatasetGraphTDB graphStore, Binding inputBinding, Context context)
    { super(graphStore, inputBinding, context) ; }
    

    // ---- Factory
    public static UpdateEngineFactory getFactory() { 
        return new UpdateEngineFactory()
        {
            @Override
            public boolean accept(DatasetGraph graphStore, Context context) {
                return (graphStore instanceof DatasetGraphTDB) ;
            }

            @Override
            public UpdateEngine create(DatasetGraph graphStore, Binding inputBinding, Context context) {
                return new UpdateEngineTDB((DatasetGraphTDB)graphStore, inputBinding, context) ;
            }
        } ;
    }

    public static void register() { UpdateEngineRegistry.get().add(getFactory()) ; }
}
