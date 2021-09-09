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

public class FOAF {

/**
 *  FOAF is a project devoted to linking people and information using the Web.
 *  <p>
 *	See <a href="http://xmlns.com/foaf/spec/">FOAF Vocabulary Terms</a>.
 *  <p>
 *  <a href="http://xmlns.com/foaf/0.1/">Base URI and namepace</a>.
 */
    private static final Model m = ModelFactory.createDefaultModel();
    public static final String NS = "http://xmlns.com/foaf/0.1/";

    public static final Resource OnlineChatAccount = m.createResource(NS+"OnlineChatAccount");
    public static final Resource PersonalProfileDocument = m.createResource(NS+"PersonalProfileDocument");
    public static final Resource OnlineEcommerceAccount = m.createResource(NS+"OnlineEcommerceAccount");
    public static final Resource Image = m.createResource(NS+"Image");
    public static final Resource Person = m.createResource(NS+"Person");
    public static final Resource Organization = m.createResource(NS+"Organization");
    public static final Resource Document = m.createResource(NS+"Document");
    public static final Resource Group = m.createResource(NS+"Group");
    public static final Resource LabelProperty = m.createResource(NS+"LabelProperty");
    public static final Resource Agent = m.createResource(NS+"Agent");
    public static final Resource Project = m.createResource(NS+"Project");
    public static final Resource OnlineAccount = m.createResource(NS+"OnlineAccount");
    public static final Resource OnlineGamingAccount = m.createResource(NS+"OnlineGamingAccount");
    public static final Property mbox = m.createProperty(NS+"mbox");
    public static final Property isPrimaryTopicOf = m.createProperty(NS+"isPrimaryTopicOf");
    public static final Property surname = m.createProperty(NS+"surname");
    public static final Property yahooChatID = m.createProperty(NS+"yahooChatID");
    public static final Property msnChatID = m.createProperty(NS+"msnChatID");
    public static final Property skypeID = m.createProperty(NS+"skypeID");
    public static final Property topic_interest = m.createProperty(NS+"topic_interest");
    public static final Property title = m.createProperty(NS+"title");
    public static final Property icqChatID = m.createProperty(NS+"icqChatID");
    public static final Property dnaChecksum = m.createProperty(NS+"dnaChecksum");
    public static final Property based_near = m.createProperty(NS+"based_near");
    public static final Property phone = m.createProperty(NS+"phone");
    public static final Property publications = m.createProperty(NS+"publications");
    public static final Property gender = m.createProperty(NS+"gender");
    public static final Property givenname = m.createProperty(NS+"givenname");
    public static final Property myersBriggs = m.createProperty(NS+"myersBriggs");
    public static final Property account = m.createProperty(NS+"account");
    public static final Property weblog = m.createProperty(NS+"weblog");
    public static final Property theme = m.createProperty(NS+"theme");
    public static final Property depiction = m.createProperty(NS+"depiction");
    public static final Property workplaceHomepage = m.createProperty(NS+"workplaceHomepage");
    public static final Property maker = m.createProperty(NS+"maker");
    public static final Property pastProject = m.createProperty(NS+"pastProject");
    public static final Property jabberID = m.createProperty(NS+"jabberID");
    public static final Property age = m.createProperty(NS+"age");
    public static final Property lastName = m.createProperty(NS+"lastName");
    public static final Property logo = m.createProperty(NS+"logo");
    public static final Property givenName = m.createProperty(NS+"givenName");
    public static final Property openid = m.createProperty(NS+"openid");
    public static final Property topic = m.createProperty(NS+"topic");
    public static final Property mbox_sha1sum = m.createProperty(NS+"mbox_sha1sum");
    public static final Property birthday = m.createProperty(NS+"birthday");
    public static final Property primaryTopic = m.createProperty(NS+"primaryTopic");
    public static final Property name = m.createProperty(NS+"name");
    public static final Property knows = m.createProperty(NS+"knows");
    public static final Property plan = m.createProperty(NS+"plan");
    public static final Property homepage = m.createProperty(NS+"homepage");
    public static final Property membershipClass = m.createProperty(NS+"membershipClass");
    public static final Property tipjar = m.createProperty(NS+"tipjar");
    public static final Property currentProject = m.createProperty(NS+"currentProject");
    public static final Property accountServiceHomepage = m.createProperty(NS+"accountServiceHomepage");
    public static final Property member = m.createProperty(NS+"member");
    public static final Property page = m.createProperty(NS+"page");
    public static final Property sha1 = m.createProperty(NS+"sha1");
    public static final Property interest = m.createProperty(NS+"interest");
    public static final Property schoolHomepage = m.createProperty(NS+"schoolHomepage");
    public static final Property fundedBy = m.createProperty(NS+"fundedBy");
    public static final Property familyName = m.createProperty(NS+"familyName");
    public static final Property status = m.createProperty(NS+"status");
    public static final Property holdsAccount = m.createProperty(NS+"holdsAccount");
    public static final Property accountName = m.createProperty(NS+"accountName");
    public static final Property thumbnail = m.createProperty(NS+"thumbnail");
    public static final Property geekcode = m.createProperty(NS+"geekcode");
    public static final Property made = m.createProperty(NS+"made");
    public static final Property aimChatID = m.createProperty(NS+"aimChatID");
    public static final Property workInfoHomepage = m.createProperty(NS+"workInfoHomepage");
    public static final Property nick = m.createProperty(NS+"nick");
    public static final Property depicts = m.createProperty(NS+"depicts");
    public static final Property firstName = m.createProperty(NS+"firstName");
    public static final Property family_name = m.createProperty(NS+"family_name");
    public static final Property focus = m.createProperty(NS+"focus");
    public static final Property img = m.createProperty(NS+"img");
}
