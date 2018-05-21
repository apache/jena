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

package org.apache.jena.query.text.es;

import org.apache.jena.query.Dataset ;
import org.apache.jena.query.text.EntityDefinition;
import org.apache.jena.query.text.TextDatasetFactory;
import org.apache.jena.query.text.TextIndex;
import org.apache.jena.query.text.TextIndexConfig;
import org.apache.jena.sys.JenaSystem ;

public class TextESDatasetFactory
{
    static { JenaSystem.init(); }
    
    /**
     * Create an ElasticSearch based Index and return a Dataset based on this index
     * @param base the base {@link Dataset}
     * @param config {@link TextIndexConfig} containing the {@link EntityDefinition}
     * @param settings ElasticSearch specific settings for initializing and connecting to an ElasticSearch Cluster
     * @return The config definition for the index instantiation
     */
    public static Dataset createES(Dataset base, TextIndexConfig config, ESSettings settings)
    {
        TextIndex index = createESIndex(config, settings) ;
        return TextDatasetFactory.create(base, index, true) ;
    }

    /**
     * Create an ElasticSearch based Index
     * @param config {@link TextIndexConfig} containing the {@link EntityDefinition}
     * @param settings ElasticSearch specific settings for initializing and connecting to an ElasticSearch Cluster
     * @return a configured instance of TextIndexES
     */
    public static TextIndex createESIndex(TextIndexConfig config, ESSettings settings)
    {
        return new TextIndexES(config, settings) ;
    }

}

