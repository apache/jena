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

package org.apache.jena.riot.writer;

import java.io.OutputStream ;
import java.io.Writer ;

import org.apache.jena.riot.WriterDatasetRIOT ;
import org.apache.jena.riot.system.PrefixMap ;
import org.apache.jena.riot.system.PrefixMapFactory ;
import org.apache.jena.sparql.core.DatasetGraph ;
import org.apache.jena.sparql.util.Context ;

public abstract class WriterDatasetRIOTBase implements WriterDatasetRIOT
{
    private static PrefixMap prefixMap(DatasetGraph dataset)
    {
        return PrefixMapFactory.create(dataset.getDefaultGraph().getPrefixMapping()) ;
    }
    
    @Override
    public abstract void write(Writer out, DatasetGraph dataset, PrefixMap prefixMap, String baseURI, Context context) ;
    
    @Override
    public abstract void write(OutputStream out, DatasetGraph dataset, PrefixMap prefixMap, String baseURI, Context context) ;
}
