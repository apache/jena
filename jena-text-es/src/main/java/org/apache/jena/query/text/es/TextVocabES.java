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

package org.apache.jena.query.text.es;

import static org.apache.jena.query.text.assembler.TextVocab.NS;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.assembler.Vocab;

public class TextVocabES {
    
    public static final Resource textIndexES        = Vocab.resource(NS, "TextIndexES") ;
    
    public static final Property pServerList        = Vocab.property(NS, "serverList");
    public static final Property pClusterName       = Vocab.property(NS, "clusterName");
    public static final Property pShards            = Vocab.property(NS, "shards");
    public static final Property pReplicas          = Vocab.property(NS, "replicas");
    public static final Property pIndexName          = Vocab.property(NS, "indexName");
}
