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

package org.apache.jena.vocabulary;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
 
/**
 * Constants for the W3C Organization Ontology.
 * 
 * @see <a href="https://www.w3.org/TR/vocab-org/">Organization Ontology</a>
 */
public class ORG {
	private static final Model m = ModelFactory.createDefaultModel();

	public static final String NS = "http://www.w3.org/ns/org#";
	public static final Resource NAMESPACE = m.createResource(NS);
	
	/**
	 * Returns the URI for this schema
	 * @return URI
	 */
	public static String getURI() {
		return NS;
	}
	
	// Classes
	public static final Resource ChangeEvent = m.createResource(NS + "ChangeEvent");
	public static final Resource FormalOrganization = m.createResource(NS + "FormalOrganization");
	public static final Resource Membership = m.createResource(NS + "Membership");
	public static final Resource OrganizationalCollaboration = m.createResource(NS + "OrganizationalCollaboration");
	public static final Resource OrganizationalUnit = m.createResource(NS + "OrganizationalUnit");
	public static final Resource Organization = m.createResource(NS + "Organization");
	public static final Resource Post = m.createResource(NS + "Post");
	public static final Resource Role = m.createResource(NS + "Role");
	public static final Resource Site = m.createResource(NS + "Site");

	// Properties
	public static final Property basedAt = m.createProperty(NS + "basedAt");
	public static final Property changedBy = m.createProperty(NS + "changedBy");
	public static final Property classification = m.createProperty(NS + "classification");
	public static final Property hasMember = m.createProperty(NS + "hasMember");
	public static final Property hasMembership = m.createProperty(NS + "hasMembership");
	public static final Property hasPost = m.createProperty(NS + "hasPost");
	public static final Property hasPrimarySite = m.createProperty(NS + "hasPrimarySite");
	public static final Property hasRegisteredSite = m.createProperty(NS + "hasRegisteredSite");
	public static final Property hasSite = m.createProperty(NS + "hasSite");
	public static final Property hasSubOrganization = m.createProperty(NS + "hasSubOrganization");
	public static final Property hasUnit = m.createProperty(NS + "hasUnit");
	public static final Property headOf = m.createProperty(NS + "headOf");
	public static final Property heldBy = m.createProperty(NS + "heldBy");
	public static final Property holds = m.createProperty(NS + "holds");
	public static final Property identifier = m.createProperty(NS + "identifier");
	public static final Property linkedTo = m.createProperty(NS + "linkedTo");
	public static final Property location = m.createProperty(NS + "location");
	public static final Property memberDuring = m.createProperty(NS + "memberDuring");
	public static final Property memberOf = m.createProperty(NS + "memberOf");
	public static final Property member = m.createProperty(NS + "member");
	public static final Property organization = m.createProperty(NS + "organization");
	public static final Property originalOrganization = m.createProperty(NS + "originalOrganization");
	public static final Property postIn = m.createProperty(NS + "postIn");
	public static final Property purpose = m.createProperty(NS + "purpose");
	public static final Property remuneration = m.createProperty(NS + "remuneration");
	public static final Property reportsTo = m.createProperty(NS + "reportsTo");
	public static final Property resultedFrom = m.createProperty(NS + "resultedFrom");
	public static final Property resultingOrganization = m.createProperty(NS + "resultingOrganization");
	public static final Property role = m.createProperty(NS + "role");
	public static final Property roleProperty = m.createProperty(NS + "roleProperty");
	public static final Property siteAddress = m.createProperty(NS + "siteAddress");
	public static final Property siteOf = m.createProperty(NS + "siteOf");
	public static final Property subOrganizationOf = m.createProperty(NS + "subOrganizationOf");
	public static final Property transitiveSubOrganizationOf = m.createProperty(NS + "transitiveSubOrganizationOf");
	public static final Property unitOf = m.createProperty(NS + "unitOf");
}
