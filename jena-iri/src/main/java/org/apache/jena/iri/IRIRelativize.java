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

package org.apache.jena.iri;

/**
 * Constants for use with {@link IRI#relativize(IRI, int)}
 * and {@link IRI#relativize(String, int)}.
 * These constants can be or-red together.
 */
public interface IRIRelativize {
    /**
     * Allow same document references (e.g. "" or "#frag").
     */
    static final public int SAMEDOCUMENT = 1;
    /**
     * Allow network relative references (e.g. "//example.org/a/b/c").
     */
    static final public int NETWORK = 2;
    /**
     * Allow absolute relative references (e.g. "/a/b/c").
     */
    
    static final public int ABSOLUTE = 4;
    
    /**
     * allow child relative references (e.g. "b/c").
     */
    static final public int CHILD = 8;
    /**
     * allow parent relative references (e.g. "../b/c").
     */
    
    static final public int PARENT = 16;

    /**
     * allow grandparent relative references (e.g. "../../b/c").
     */
    static final public int GRANDPARENT = 32;
  
}
