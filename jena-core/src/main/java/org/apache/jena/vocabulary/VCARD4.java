/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
 * Constants for the https://www.w3.org/TR/2014/NOTE-vcard-rdf-20140522/.
 *
 * @see <a href="vCard Ontology">https://www.w3.org/TR/2014/NOTE-vcard-rdf-20140522/</a>
 */
public class VCARD4 {
    private static final Model m = ModelFactory.createDefaultModel();

    /**
     * The VCARD4 namespace: http://www.w3.org/2006/vcard/ns#
     */
    public static final String NS = "http://www.w3.org/2006/vcard/ns#";
    public static final Resource NAMESPACE = m.createResource(NS);

    /**
     * Returns the URI for this schema
     * @return URI
     */
    public static String getURI() {
        return NS;
    }
    
    // Classes
    public static final Resource Acquaintance = m.createResource(NS + "Acquaintance");
    public static final Resource Address = m.createResource(NS + "Address");
    public static final Resource Agent = m.createResource(NS + "Agent");
    @Deprecated
    public static final Resource BBS = m.createResource(NS + "BBS");
    @Deprecated
    public static final Resource Car = m.createResource(NS + "Car");
    public static final Resource Cell = m.createResource(NS + "Cell");
    public static final Resource Child = m.createResource(NS + "Child");
    public static final Resource Colleague = m.createResource(NS + "Colleague");
    public static final Resource Contact = m.createResource(NS + "Contact");
    public static final Resource Coresident = m.createResource(NS + "Coresident");
    public static final Resource Coworker = m.createResource(NS + "Coworker");
    public static final Resource Crush = m.createResource(NS + "Crush");
    public static final Resource Date = m.createResource(NS + "Date");
    @Deprecated
    public static final Resource Dom = m.createResource(NS + "Dom");
    @Deprecated
    public static final Resource Email = m.createResource(NS + "Email");
    public static final Resource Emergency = m.createResource(NS + "Emergency");
    public static final Resource Fax = m.createResource(NS + "Fax");
    public static final Resource Female = m.createResource(NS + "Female");
    public static final Resource Friend = m.createResource(NS + "Friend");
    public static final Resource Gender = m.createResource(NS + "Gender");
    public static final Resource Group = m.createResource(NS + "Group");
    public static final Resource Home = m.createResource(NS + "Home");
    @Deprecated
    public static final Resource ISDN = m.createResource(NS + "ISDN");
    public static final Resource Individual = m.createResource(NS + "Individual");
    @Deprecated
    public static final Resource Internet = m.createResource(NS + "Internet");
    @Deprecated
    public static final Resource Intl = m.createResource(NS + "Intl");
    public static final Resource Kin = m.createResource(NS + "Kin");
    public static final Resource Kind = m.createResource(NS + "Kind");
    @Deprecated
    public static final Resource Label = m.createResource(NS + "Label");
    public static final Resource Location = m.createResource(NS + "Location");
    public static final Resource Male = m.createResource(NS + "Male");
    public static final Resource Me = m.createResource(NS + "Me");
    public static final Resource Met = m.createResource(NS + "Met");
    @Deprecated
    public static final Resource Modem = m.createResource(NS + "Modem");
    @Deprecated
    public static final Resource Msg = m.createResource(NS + "Msg");
    public static final Resource Muse = m.createResource(NS + "Muse");
    public static final Resource Name = m.createResource(NS + "Name");
    public static final Resource Neighbor = m.createResource(NS + "Neighbor");
    public static final Resource None = m.createResource(NS + "None");
    public static final Resource Organization = m.createResource(NS + "Organization");
    public static final Resource Other = m.createResource(NS + "Other");
    @Deprecated
    public static final Resource PCS = m.createResource(NS + "PCS");
    public static final Resource Pager = m.createResource(NS + "Pager");
    @Deprecated
    public static final Resource Parcel = m.createResource(NS + "Parcel");
    public static final Resource Parent = m.createResource(NS + "Parent");
    @Deprecated
    public static final Resource Postal = m.createResource(NS + "Postal");
    @Deprecated
    public static final Resource Pref = m.createResource(NS + "Pref");
    public static final Resource RelatedType = m.createResource(NS + "RelatedType");
    public static final Resource Sibling = m.createResource(NS + "Sibling");
    public static final Resource Spouse = m.createResource(NS + "Spouse");
    public static final Resource Sweetheart = m.createResource(NS + "Sweetheart");
    @Deprecated
    public static final Resource Tel = m.createResource(NS + "Tel");
    public static final Resource TelephoneType = m.createResource(NS + "TelephoneType");
    public static final Resource Text = m.createResource(NS + "Text");
    public static final Resource TextPhone = m.createResource(NS + "TextPhone");
    public static final Resource Type = m.createResource(NS + "Type");
    public static final Resource Unknown = m.createResource(NS + "Unknown");
    public static final Resource VCard = m.createResource(NS + "VCard");
    public static final Resource Video = m.createResource(NS + "Video");
    public static final Resource Voice = m.createResource(NS + "Voice");
    public static final Resource Work = m.createResource(NS + "Work");
    @Deprecated
    public static final Resource X400 = m.createResource(NS + "X400");

    // Properties
    public static final Property additional_name = m.createProperty(NS + "additional-name");
    public static final Property adr = m.createProperty(NS + "adr");
    @Deprecated
    public static final Property agent = m.createProperty(NS + "agent");
    public static final Property anniversary = m.createProperty(NS + "anniversary");
    public static final Property bday = m.createProperty(NS + "bday");
    public static final Property category = m.createProperty(NS + "category");
    @Deprecated
    public static final Property class_prop = m.createProperty(NS + "class");
    public static final Property country_name = m.createProperty(NS + "country-name");
    public static final Property email = m.createProperty(NS + "email");
    @Deprecated
    public static final Property extended_address = m.createProperty(NS + "extended-address");
    public static final Property family_name = m.createProperty(NS + "family-name");
    public static final Property fn = m.createProperty(NS + "fn");
    public static final Property geo = m.createProperty(NS + "geo");
    public static final Property given_name = m.createProperty(NS + "given-name");
    public static final Property hasAdditionalName = m.createProperty(NS + "hasAdditionalName");
    public static final Property hasAddress = m.createProperty(NS + "hasAddress");
    public static final Property hasCalendarBusy = m.createProperty(NS + "hasCalendarBusy");
    public static final Property hasCalendarLink = m.createProperty(NS + "hasCalendarLink");
    public static final Property hasCalendarRequest = m.createProperty(NS + "hasCalendarRequest");
    public static final Property hasCategory = m.createProperty(NS + "hasCategory");
    public static final Property hasCountryName = m.createProperty(NS + "hasCountryName");
    public static final Property hasEmail = m.createProperty(NS + "hasEmail");
    public static final Property hasFN = m.createProperty(NS + "hasFN");
    public static final Property hasFamilyName = m.createProperty(NS + "hasFamilyName");
    public static final Property hasGender = m.createProperty(NS + "hasGender");
    public static final Property hasGeo = m.createProperty(NS + "hasGeo");
    public static final Property hasGivenName = m.createProperty(NS + "hasGivenName");
    public static final Property hasHonorificPrefix = m.createProperty(NS + "hasHonorificPrefix");
    public static final Property hasHonorificSuffix = m.createProperty(NS + "hasHonorificSuffix");
    public static final Property hasInstantMessage = m.createProperty(NS + "hasInstantMessage");
    public static final Property hasKey = m.createProperty(NS + "hasKey");
    public static final Property hasLanguage = m.createProperty(NS + "hasLanguage");
    public static final Property hasLocality = m.createProperty(NS + "hasLocality");
    public static final Property hasLogo = m.createProperty(NS + "hasLogo");
    public static final Property hasMember = m.createProperty(NS + "hasMember");
    public static final Property hasName = m.createProperty(NS + "hasName");
    public static final Property hasNickname = m.createProperty(NS + "hasNickname");
    public static final Property hasNote = m.createProperty(NS + "hasNote");
    public static final Property hasOrganizationName = m.createProperty(NS + "hasOrganizationName");
    public static final Property hasOrganizationUnit = m.createProperty(NS + "hasOrganizationUnit");
    public static final Property hasPhoto = m.createProperty(NS + "hasPhoto");
    public static final Property hasPostalCode = m.createProperty(NS + "hasPostalCode");
    public static final Property hasRegion = m.createProperty(NS + "hasRegion");
    public static final Property hasRelated = m.createProperty(NS + "hasRelated");
    public static final Property hasRole = m.createProperty(NS + "hasRole");
    public static final Property hasSound = m.createProperty(NS + "hasSound");
    public static final Property hasSource = m.createProperty(NS + "hasSource");
    public static final Property hasStreetAddress = m.createProperty(NS + "hasStreetAddress");
    public static final Property hasTelephone = m.createProperty(NS + "hasTelephone");
    public static final Property hasTitle = m.createProperty(NS + "hasTitle");
    public static final Property hasUID = m.createProperty(NS + "hasUID");
    public static final Property hasURL = m.createProperty(NS + "hasURL");
    public static final Property hasValue = m.createProperty(NS + "hasValue");
    public static final Property honorific_prefix = m.createProperty(NS + "honorific-prefix");
    public static final Property honorific_suffix = m.createProperty(NS + "honorific-suffix");
    public static final Property key = m.createProperty(NS + "key");
    @Deprecated
    public static final Property label = m.createProperty(NS + "label");
    public static final Property language = m.createProperty(NS + "language");
    @Deprecated
    public static final Property latitude = m.createProperty(NS + "latitude");
    public static final Property locality = m.createProperty(NS + "locality");
    public static final Property logo = m.createProperty(NS + "logo");
    @Deprecated
    public static final Property longitude = m.createProperty(NS + "longitude");
    @Deprecated
    public static final Property mailer = m.createProperty(NS + "mailer");
    public static final Property n = m.createProperty(NS + "n");
    public static final Property nickname = m.createProperty(NS + "nickname");
    public static final Property note = m.createProperty(NS + "note");
    public static final Property org = m.createProperty(NS + "org");
    public static final Property organization_name = m.createProperty(NS + "organization-name");
    public static final Property organization_unit = m.createProperty(NS + "organization-unit");
    public static final Property photo = m.createProperty(NS + "photo");
    @Deprecated
    public static final Property post_office_box = m.createProperty(NS + "post-office-box");
    public static final Property postal_code = m.createProperty(NS + "postal-code");
    public static final Property prodid = m.createProperty(NS + "prodid");
    public static final Property region = m.createProperty(NS + "region");
    public static final Property rev = m.createProperty(NS + "rev");
    public static final Property role = m.createProperty(NS + "role");
    public static final Property sort_string = m.createProperty(NS + "sort-string");
    public static final Property sound = m.createProperty(NS + "sound");
    public static final Property street_address = m.createProperty(NS + "street-address");
    public static final Property tel = m.createProperty(NS + "tel");
    public static final Property title = m.createProperty(NS + "title");
    public static final Property tz = m.createProperty(NS + "tz");
    public static final Property url = m.createProperty(NS + "url");
    public static final Property value = m.createProperty(NS + "value");
}
