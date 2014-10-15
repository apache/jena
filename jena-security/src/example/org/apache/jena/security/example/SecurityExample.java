/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jena.security.example;

import java.net.URL;

import org.apache.jena.security.Factory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

public class SecurityExample {

	/**
	 * @param args
	 */

	public static void main(String[] args) {
		String[] names = { "alice", "bob", "chuck", "darla" };

		RDFNode msgType = ResourceFactory
				.createResource("http://example.com/msg");
		Property pTo = ResourceFactory.createProperty("http://example.com/to");
		Property pFrom = ResourceFactory
				.createProperty("http://example.com/from");
		Property pSubj = ResourceFactory
				.createProperty("http://example.com/subj");

		Model model = ModelFactory.createDefaultModel();
		URL url = SecurityExample.class.getClassLoader().getResource(
				"org/apache/jena/security/example/example.ttl");
		model.read(url.toExternalForm());
		ResIterator ri = model.listSubjectsWithProperty(RDF.type, msgType);
		System.out.println("All the messages");
		while (ri.hasNext()) {
			Resource msg = ri.next();
			Statement to = msg.getProperty(pTo);
			Statement from = msg.getProperty(pFrom);
			Statement subj = msg.getProperty(pSubj);
			System.out.println(String.format("%s to: %s  from: %s  subj: %s",
					msg, to.getObject(), from.getObject(), subj.getObject()));
		}
		System.out.println();

		ExampleEvaluator evaluator = new ExampleEvaluator(model);
		model = Factory.getInstance(evaluator,
				"http://example.com/SecuredModel", model);
		for (String userName : names) {
			evaluator.setPrincipal(userName);

			System.out.println("Messages " + userName + " can manipulate");
			ri = model.listSubjectsWithProperty(RDF.type, msgType);
			while (ri.hasNext()) {
				Resource msg = ri.next();
				Statement to = msg.getProperty(pTo);
				Statement from = msg.getProperty(pFrom);
				Statement subj = msg.getProperty(pSubj);
				System.out.println(String.format(
						"%s to: %s  from: %s  subj: %s", msg, to.getObject(),
						from.getObject(), subj.getObject()));
			}
			ri.close();
			for (String name : names)
			{
				System.out.println( String.format( "%s messages to %s", model.listSubjectsWithProperty( pTo, name ).toList().size(), name ) );
			}
			System.out.println();
		}
	}

}
