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

package com.hp.hpl.jena.rdfxml.xmlinput;

/**
 * The interface for being notified about namespace use in an RDF/XML
 * document.
 * This has methods copied form SAX  for
 * notifying the application of namespaces.
 *
*/
public interface NamespaceHandler {	
/**
	* A namespace prefix is being defined..
	* 
	* @param prefix
	*            the name of the prefix (ie the X in xmlns:X=U)
	* @param uri
	*            the uri string (ie the U)
	*/
   public void startPrefixMapping(String prefix, String uri);
   /**
	   * A namespace prefix is going out of scope.
	   * There is no guarantee that start and end PrefixMapping
	   * calls nest.
	   * 
	   * @param prefix
	   *            the name of the prefix (ie the X in xmlns:X=U)
	   * 
	   * 
	   */
   public void endPrefixMapping(String prefix);

}
