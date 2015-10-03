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

package org.apache.jena.testing_framework.manifest;

import org.apache.jena.n3.turtle.TurtleTestVocab;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.TestManifest;

public class ManifestItem {
	private Resource entry;

	public ManifestItem(Resource entry) {
		this.entry = entry;
	}

	public Resource getEntry() {
		return entry;
	}

	public String getTestName() {
		return Manifest.getLiteral(entry, TestManifest.name);
	}

	public Resource getAction() {
		return Manifest.getResource(entry, TestManifest.action);
	}

	public Resource getResult() {
		return Manifest.getResource(entry, TestManifest.result);
	}

	public Resource getType() {
		return Manifest.getResource(entry, RDF.type);
	}

	public Resource getOutput() {
		Resource result = getResult();
		return result == null ? null : Manifest.getResource(result,
				TurtleTestVocab.output);
	}

	public Resource getInput() {
		Resource action = getAction();
		return action == null ? null : Manifest.getResource(action,
				TurtleTestVocab.input);
	}

	public String getUriString() {
		Resource action = getAction();
		Resource inputIRIr = action == null ? null : Manifest.getResource(
				action, TurtleTestVocab.inputIRI);
		return (inputIRIr == null) ? null : inputIRIr.getURI();
	}
}
