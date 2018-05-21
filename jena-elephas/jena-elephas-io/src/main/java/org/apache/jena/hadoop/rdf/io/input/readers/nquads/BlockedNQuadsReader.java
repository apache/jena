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

package org.apache.jena.hadoop.rdf.io.input.readers.nquads;

import org.apache.jena.hadoop.rdf.io.input.readers.AbstractBlockBasedQuadReader;
import org.apache.jena.riot.Lang;

/**
 * A record for NQuads
 * <p>
 * This is a hybrid of the {@link NQuadsReader} and the
 * {@link WholeFileNQuadsReader} in that it does not process individual lines
 * rather it processes the inputs in blocks of lines parsing the whole block
 * rather than individual lines. This provides a compromise between the higher
 * parser setup of creating more parsers and the benefit of being able to split
 * input files over multiple mappers.
 * </p>
 * 
 * 
 * 
 */
public class BlockedNQuadsReader extends AbstractBlockBasedQuadReader {

    @Override
    protected Lang getRdfLanguage() {
        return Lang.NQUADS;
    }

}
