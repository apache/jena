/**
 * Copyright 2018 the original author or authors.
 * See the notice.md file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.ext.io.github.galbiston.rdf_tables.file;

import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;

/**
 *
 *
 */
public interface DefaultValues {

    public static final char COLUMN_DELIMITER = ',';
    public static final Resource NO_CLASS_ANON = ResourceFactory.createResource("http://example.org#NoClass");
    public static final String HEADER_ITEM_DELIMITER = "\\|";       //Have to escape the pipe when reading.
    public static final String HEADER_ITEM_DELIMITER_CHARACTER = "|";
    public static final String CLASS_CHARACTER = ":";
    public static final Boolean IS_RDFS_LABEL = Boolean.TRUE;
    public static final Resource NAMED_INDIVIDUAL = ResourceFactory.createResource(OWL.NS + "NamedIndividual");
    public static final char INVERT_CHARACTER = '^';

}
