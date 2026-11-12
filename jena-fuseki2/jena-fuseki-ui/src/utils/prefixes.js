/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Common prefixes offered in the query editor when a dataset has no
 * prefixes service, or its prefix store is empty. Uses the same
 * {prefix, uri} shape as the Fuseki prefixes service responses.
 */
export const DEFAULT_PREFIXES = [
  { prefix: 'rdf', uri: 'http://www.w3.org/1999/02/22-rdf-syntax-ns#' },
  { prefix: 'rdfs', uri: 'http://www.w3.org/2000/01/rdf-schema#' },
  { prefix: 'owl', uri: 'http://www.w3.org/2002/07/owl#' },
  { prefix: 'xsd', uri: 'http://www.w3.org/2001/XMLSchema#' }
]
