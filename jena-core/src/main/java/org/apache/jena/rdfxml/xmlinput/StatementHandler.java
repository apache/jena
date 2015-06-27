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

/*
 * StatementHandler.java
 *
 * Created on June 26, 2001, 9:30 AM
 */

package org.apache.jena.rdfxml.xmlinput;

/** The callback from a reader to an RDF application.
 * Each triple in the input file causes a call on one of the statement methods.
 * The same triple may occur more than once in a file, causing repeat calls
 * to the method.
 */
public interface StatementHandler  {

/** A triple in the file.
 * @param subj The subject.
 * @param pred The property.
 * @param obj The object.
 */    
   public void statement(AResource subj, AResource pred, AResource obj );
/** A triple in the file.
 * @param subj The subject.
 * @param pred The property.
 * @param lit The object.
 */    
   public void statement(AResource subj, AResource pred, ALiteral lit );

}
