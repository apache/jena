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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdfxml.xmlinput.NTriple ;

class Performance  {
	static String allFiles[][] =
		{
			{
				"testing/arp/onts/ont1",
				"http://cicho0.tripod.com/cs_Courses_ont" },
			{
			"testing/arp/onts/ont2",
				"http://cicho0.tripod.com/cs_LecturingStaff_ont" },
				{
			"testing/arp/onts/ont3",
				"http://cicho0.tripod.com/cs_Staff_ont" },
				{
			"testing/arp/onts/ont4",
				"http://cicho0.tripod.com/Dep_of_Computer_Science" },
				{
			"testing/arp/onts/ont10",
				"http://daml.umbc.edu/ontologies/calendar-ont.daml" },
				{
			"testing/arp/onts/ont11",
				"http://daml.umbc.edu/ontologies/classification.daml" },
				{
			"testing/arp/onts/ont12",
				"http://daml.umbc.edu/ontologies/dreggie-ont.daml" },
				{
			"testing/arp/onts/ont13",
				"http://daml.umbc.edu/ontologies/profile-ont.daml" },
				{
			"testing/arp/onts/ont14",
				"http://daml.umbc.edu/ontologies/talk-ont.daml" },
				{
			"testing/arp/onts/ont15",
				"http://daml.umbc.edu/ontologies/topic-ont.daml" },
				{
			"testing/arp/onts/ont16",
				"http://daml.umbc.edu/ontologies/trust-ont.daml" },
				{
			"testing/arp/onts/ont17",
				"http://derpi.tuwien.ac.at/~andrei/cerif-rdf-dc-mn.daml" },
				{
			"testing/arp/onts/ont18",
				"http://edge.mcs.drexel.edu/MUG/2001/05/16/sbf.daml" },
				{
			"testing/arp/onts/ont19",
				"http://grcinet.grci.com/maria/www/codipsite/Onto/DublinCore/DublinCore_V27Aug2001.daml" },
				{
			"testing/arp/onts/ont20",
				"http://grcinet.grci.com/maria/www/codipsite/Onto/Project/ProjectOntology_V26Jul2001.daml" },
				{
			"testing/arp/onts/ont21",
				"http://grcinet.grci.com/maria/www/codipsite/Onto/TMD/TMDOntology_V27Aug2001.daml" },
				{
			"testing/arp/onts/ont22",
				"http://grcinet.grci.com/maria/www/codipsite/Onto/WebDirectory/WebDirectory_V27Aug2001.daml" },
				{
			"testing/arp/onts/ont23",
				"http://grcinet.grci.com/maria/www/CodipSite/Onto/WebSite/WebSiteOntology_V27Aug2001.daml" },
				{
			"testing/arp/onts/ont26",
				"http://isx.com/~phaglic/horus/daml/onts/englishpubont.daml" },
				{
			"testing/arp/onts/ont27",
				"http://ksl.stanford.edu/projects/DAML/chimaera-jtp-cardinality-test1.daml" },
				{
			"testing/arp/onts/ont28",
				"http://mnemosyne.umd.edu/~aelkiss/daml/serial1.2.daml" },
            /*
            {
                "testing/arp/onts/ont71",
                    "http://orlando.drc.com/daml/Ontology/TaskListUJTLScenario/current/" },
*/
				{
			"testing/arp/onts/ont29",
				"http://mnemosyne.umd.edu/~aelkiss/weather-ont.daml" },
				{
			"testing/arp/onts/ont30",
				"http://mr.teknowledge.com/DAML/ArtOntology.daml" },
				{
			"testing/arp/onts/ont31",
				"http://mr.teknowledge.com/daml/Homeworks/HomeWork1/ResearchProjectOntology.daml" },
				{
			"testing/arp/onts/ont32",
				"http://mr.teknowledge.com/daml/homeworks/HomeWork3/BriefingOntology.daml" },
				{
			"testing/arp/onts/ont34",
				"http://mr.teknowledge.com/DAML/Imaging.daml" },
				{
			"testing/arp/onts/ont35",
				"http://mr.teknowledge.com/daml/ontologies/ImageFingerprinting/2001/04/BriefingsOntology.daml" },
				{
			"testing/arp/onts/ont36",
				"http://mr.teknowledge.com/daml/ontologies/ImageFingerprinting/2001/04/ImageFingerprintingOntology-web.daml" },
				{
			"testing/arp/onts/ont37",
				"http://mr.teknowledge.com/daml/ontologies/ImageFingerprinting/2001/04/ImageFingerprintsOntology-briefings.daml" },
				{
			"testing/arp/onts/ont38",
				"http://mr.teknowledge.com/DAML/pptOntology.daml" },
				{
			"testing/arp/onts/ont39",
				"http://ontobroker.semanticweb.org/ontos/compontos/tourism_I1.daml" },
				{
			"testing/arp/onts/ont40",
				"http://ontobroker.semanticweb.org/ontos/compontos/tourism_I2.daml" },
				{
			"testing/arp/onts/ont41",
				"http://ontobroker.semanticweb.org/ontos/compontos/tourism_I3.daml" },
				{
			"testing/arp/onts/ont42",
				"http://ontobroker.semanticweb.org/ontos/compontos/tourism_I4.daml" },
				{
			"testing/arp/onts/ont43",
				"http://ontobroker.semanticweb.org/ontos/compontos/tourism_II1.daml" },
				{
			"testing/arp/onts/ont44",
				"http://ontobroker.semanticweb.org/ontos/compontos/tourism_II2.daml" },
				{
			"testing/arp/onts/ont45",
				"http://ontobroker.semanticweb.org/ontos/compontos/tourism_II3.daml" },
				{
			"testing/arp/onts/ont46",
				"http://ontobroker.semanticweb.org/ontos/compontos/tourism_II4.daml" },
				{
			"testing/arp/onts/ont47",
				"http://ontobroker.semanticweb.org/ontos/compontos/tourism_III1.daml" },
				{
			"testing/arp/onts/ont48",
				"http://ontobroker.semanticweb.org/ontos/compontos/tourism_III2.daml" },
				{
			"testing/arp/onts/ont49",
				"http://ontobroker.semanticweb.org/ontos/compontos/tourism_III3.daml" },
				{
			"testing/arp/onts/ont50",
				"http://ontobroker.semanticweb.org/ontos/compontos/tourism_III4.daml" },
				{
			"testing/arp/onts/ont53",
				"http://opencyc.sourceforge.net/daml/cyc-transportation.daml" },
				{
			"testing/arp/onts/ont54",
				"http://opencyc.sourceforge.net/daml/cyc.daml" },
				{
			"testing/arp/onts/ont55",
				"http://opencyc.sourceforge.net/daml/naics" },
				{
			"testing/arp/onts/ont56",
				"http://orlando.drc.com/daml/ontology/Bibliographic/current/" },
				{
			"testing/arp/onts/ont57",
				"http://orlando.drc.com/daml/Ontology/Commercial/Shipping/current/" },
				{
			"testing/arp/onts/ont58",
				"http://orlando.drc.com/daml/Ontology/Condition/UJTL/v4.0/current/" },
				{
			"testing/arp/onts/ont59",
				"http://orlando.drc.com/daml/Ontology/DAML-extension/current/" },
				{
			"testing/arp/onts/ont60",
				"http://orlando.drc.com/daml/ontology/DC/current/" },
				{
			"testing/arp/onts/ont61",
				"http://orlando.drc.com/daml/ontology/Fugitive/current/" },
				{
			"testing/arp/onts/ont62",
				"http://orlando.drc.com/daml/Ontology/Genealogy/current/" },
				{
			"testing/arp/onts/ont63",
				"http://orlando.drc.com/daml/ontology/Glossary/current/" },
				{
			"testing/arp/onts/ont64",
				"http://orlando.drc.com/daml/Ontology/GPS/Coordinates/current/" },
				{
			"testing/arp/onts/ont65",
				"http://orlando.drc.com/daml/Ontology/Intelligence/Report/current/" },
				{
			"testing/arp/onts/ont66",
				"http://orlando.drc.com/daml/ontology/Locator/current/" },
				{
			"testing/arp/onts/ont67",
				"http://orlando.drc.com/daml/ontology/Organization/current/" },
				{
			"testing/arp/onts/ont68",
				"http://orlando.drc.com/daml/ontology/Person/current/" },
				{
			"testing/arp/onts/ont69",
				"http://orlando.drc.com/daml/Ontology/POC/current/" },
				{
			"testing/arp/onts/ont70",
				"http://orlando.drc.com/daml/ontology/TaskList/current/" },
                /*
				{
			"testing/arp/onts/ont71",
				"http://orlando.drc.com/daml/Ontology/TaskListUJTLScenario/current/" },
				{
			"testing/arp/onts/ont72",
				"http://orlando.drc.com/daml/Ontology/Thesaurus/CALL/current/" },
				{
			"testing/arp/onts/ont73",
				"http://orlando.drc.com/daml/ontology/UniversalProperty/current/" },
				{
			"testing/arp/onts/ont74",
				"http://orlando.drc.com/daml/ontology/VES/current/" },
				{
			"testing/arp/onts/ont75",
				"http://orlando.drc.com/SemanticWeb/DAML/Ontology/dc" },
				{
			"testing/arp/onts/ont76",
				"http://orlando.drc.com/SemanticWeb/DAML/Ontology/DIS/Entity/Platform/Land" },
				{
			"testing/arp/onts/ont77",
				"http://orlando.drc.com/SemanticWeb/DAML/Ontology/Goal-Objective" },
				{
			"testing/arp/onts/ont78",
				"http://orlando.drc.com/SemanticWeb/DAML/Ontology/NationalSecurity" },
				{
			"testing/arp/onts/ont79",
				"http://orlando.drc.com/SemanticWeb/DAML/Ontology/VES" },
				{
			"testing/arp/onts/ont80",
				"http://orlando.drc.com/SemanticWeb/OWL/Ontology/spaceshuttle/crew" },
				{
			"testing/arp/onts/ont81",
				"http://orlando.drc.com/SemanticWeb/OWL/Ontology/spaceshuttle/mission" },
				{
			"testing/arp/onts/ont83",
				"http://phd1.cs.yale.edu:8080/ontologies/wsdl-ont.daml" },
				{
			"testing/arp/onts/ont84",
				"http://phd1.cs.yale.edu:8080/umls/UMLSinDAML/NET/SRDEF.daml" },
				{
			"testing/arp/onts/ont85",
				"http://phd1.cs.yale.edu:8080/umls/UMLSinDAML/NET/SRSTR.daml" },
				{
			"testing/arp/onts/ont86",
				"http://projects.teknowledge.com/DAML/DynamicOntology1.daml" },
				{
			"testing/arp/onts/ont87",
				"http://projects.teknowledge.com/DAML/Ontology.daml" },
				{
			"testing/arp/onts/ont88", "http://purl.org/net/swn" }, {
			"testing/arp/onts/ont89", "http://purl.org/rss/1.0/" }, {
			"testing/arp/onts/ont90",
				"http://reliant.teknowledge.com/DAML/SUO.daml" },
				{
			"testing/arp/onts/ont91",
				"http://ubot.lockheedmartin.com/ubot/2001/08/baby-shoe/shoeproj-ont.daml" },
				{
			"testing/arp/onts/ont92",
				"http://ubot.lockheedmartin.com/ubot/2001/08/extraction-ont.daml" },
				{
			"testing/arp/onts/ont93",
				"http://ubot.lockheedmartin.com/ubot/2001/08/ubot-ont.daml" },
				{
			"testing/arp/onts/ont94",
				"http://www.ai.sri.com/daml/ontologies/sri-basic/1-0/Awards.daml" },
				{
			"testing/arp/onts/ont95",
				"http://www.ai.sri.com/daml/ontologies/sri-basic/1-0/Bio.daml" },
				{
			"testing/arp/onts/ont96",
				"http://www.ai.sri.com/daml/ontologies/sri-basic/1-0/ContactInfo.daml" },
				{
			"testing/arp/onts/ont97",
				"http://www.ai.sri.com/daml/ontologies/sri-basic/1-0/Course.daml" },
				{
			"testing/arp/onts/ont98",
				"http://www.ai.sri.com/daml/ontologies/sri-basic/1-0/Date.daml" },
				{
			"testing/arp/onts/ont99",
				"http://www.ai.sri.com/daml/ontologies/sri-basic/1-0/Image.daml" },
				{
			"testing/arp/onts/ont100",
				"http://www.ai.sri.com/daml/ontologies/sri-basic/1-0/Organization.daml" },
				{
			"testing/arp/onts/ont101",
				"http://www.ai.sri.com/daml/ontologies/sri-basic/1-0/Person.daml" },
				{
			"testing/arp/onts/ont102",
				"http://www.ai.sri.com/daml/ontologies/sri-basic/1-0/ProfessionalExperienceAndEducation.daml" },
				{
			"testing/arp/onts/ont103",
				"http://www.ai.sri.com/daml/ontologies/sri-basic/1-0/Project.daml" },
				{
			"testing/arp/onts/ont104",
				"http://www.ai.sri.com/daml/ontologies/sri-basic/1-0/Publication.daml" },
				{
			"testing/arp/onts/ont105",
				"http://www.ai.sri.com/daml/ontologies/sri-basic/1-0/Researcher.daml" },
				{
			"testing/arp/onts/ont106",
				"http://www.ai.sri.com/daml/ontologies/sri-basic/1-0/Time.daml" },
				{
			"testing/arp/onts/ont107",
				"http://www.ai.sri.com/daml/ontologies/sri-basic/1-0/Topic.daml" },
				{
			"testing/arp/onts/ont108",
				"http://www.cs.man.ac.uk/~horrocks/Ontologies/tambis.daml" },
				{
			"testing/arp/onts/ont109",
				"http://www.cs.umbc.edu/~yzou1/daml/acl.daml" },
				{
			"testing/arp/onts/ont110",
				"http://www.cs.umbc.edu/~yzou1/daml/acldaml.daml" },
				{
			"testing/arp/onts/ont111",
				"http://www.cs.umd.edu/projects/plus/DAML/onts/beer1.0.daml" },
				{
			"testing/arp/onts/ont112",
				"http://www.cs.umd.edu/projects/plus/DAML/onts/cs1.0.daml" },
				{
			"testing/arp/onts/ont113",
				"http://www.cs.umd.edu/projects/plus/DAML/onts/cs1.1.daml" },
				{
			"testing/arp/onts/ont114",
				"http://www.cs.umd.edu/projects/plus/DAML/onts/docmnt1.0.daml" },
				{
			"testing/arp/onts/ont115",
				"http://www.cs.umd.edu/projects/plus/DAML/onts/general1.0.daml" },
				{
			"testing/arp/onts/ont116",
				"http://www.cs.umd.edu/projects/plus/DAML/onts/personal1.0.daml" },
				{
			"testing/arp/onts/ont117",
				"http://www.cs.umd.edu/projects/plus/DAML/onts/tseont.daml" },
				{
			"testing/arp/onts/ont118",
				"http://www.cs.umd.edu/projects/plus/DAML/onts/univ1.0.daml" },
				{
			"testing/arp/onts/ont119",
				"http://www.cs.umd.edu/~golbeck/daml/baseball.daml" },
				{
			"testing/arp/onts/ont120",
				"http://www.cs.umd.edu/~golbeck/daml/running.daml" },
				{
			"testing/arp/onts/ont121",
				"http://www.cs.umd.edu/~golbeck/daml/vegetarian.daml" },
				{
			"testing/arp/onts/ont122",
				"http://www.cs.yale.edu/~dvm/daml/agent-ont.daml" },
				{
			"testing/arp/onts/ont123",
				"http://www.cs.yale.edu/~dvm/daml/bib-ont.daml" },
				{
			"testing/arp/onts/ont124",
				"http://www.cs.yale.edu/~dvm/daml/drsonto.daml" },
				{
			"testing/arp/onts/ont125",
				"http://www.cs.yale.edu/~dvm/daml/exp-ont.daml" },
				{
			"testing/arp/onts/ont126",
				"http://www.cs.yale.edu/~dvm/daml/pddlonto.daml" },
				{
			"testing/arp/onts/ont127",
				"http://www.cyc.com/2002/04/08/cyc.daml" },
				{
			"testing/arp/onts/ont129",
				"http://www.daml.org/2000/10/daml-ont" },
				{
			"testing/arp/onts/ont130",
				"http://www.daml.org/2000/12/daml+oil" },
				{
			"testing/arp/onts/ont131",
				"http://www.daml.org/2001/01/gedcom/gedcom.daml" },
				{
			"testing/arp/onts/ont132",
				"http://www.daml.org/2001/02/geofile/geofile-ont.daml" },
				{
			"testing/arp/onts/ont133",
				"http://www.daml.org/2001/02/projectplan/projectplan.daml" },
				{
			"testing/arp/onts/ont134",
				"http://www.daml.org/2001/03/daml+oil" },
				{
			"testing/arp/onts/ont135",
				"http://www.daml.org/2001/06/itinerary/itinerary-ont.daml" },
				{
			"testing/arp/onts/ont136",
				"http://www.daml.org/2001/06/map/map-ont" },
				{
			"testing/arp/onts/ont137",
				"http://www.daml.org/2001/08/baseball/baseball-ont" },
				{
			"testing/arp/onts/ont138",
				"http://www.daml.org/2001/10/agenda/agenda-ont" },
				{
			"testing/arp/onts/ont139",
				"http://www.daml.org/2001/12/factbook/factbook-ont" },
				{
			"testing/arp/onts/ont140",
				"http://www.daml.org/ontologies/ontologies-ont" },
				{
			"testing/arp/onts/ont141",
				"http://www.daml.org/projects/integration/projects-20010811" },
				{
			"testing/arp/onts/ont142",
				"http://www.daml.org/tools/tools-ont" },
				{
			"testing/arp/onts/ont143",
				"http://www.daml.ri.cmu.edu/ont/AirportCodes.daml" },
				{
			"testing/arp/onts/ont144",
				"http://www.daml.ri.cmu.edu/ont/homework/atlas-cmu.daml" },
				{
			"testing/arp/onts/ont145",
				"http://www.daml.ri.cmu.edu/ont/homework/atlas-date.daml" },
				{
			"testing/arp/onts/ont146",
				"http://www.daml.ri.cmu.edu/ont/homework/atlas-employment_categories.daml" },
				{
			"testing/arp/onts/ont147",
				"http://www.daml.ri.cmu.edu/ont/homework/atlas-publications.daml" },
				{
			"testing/arp/onts/ont148",
				"http://www.daml.ri.cmu.edu/ont/homework/cmu-ri-center-ont.daml" },
				{
			"testing/arp/onts/ont149",
				"http://www.daml.ri.cmu.edu/ont/homework/cmu-ri-courses-ont.daml" },
				{
			"testing/arp/onts/ont150",
				"http://www.daml.ri.cmu.edu/ont/homework/cmu-ri-employmenttypes-ont.daml" },
				{
			"testing/arp/onts/ont151",
				"http://www.daml.ri.cmu.edu/ont/homework/cmu-ri-labgroup-ont.daml" },
				{
			"testing/arp/onts/ont152",
				"http://www.daml.ri.cmu.edu/ont/homework/cmu-ri-people-ont.daml" },
				{
			"testing/arp/onts/ont153",
				"http://www.daml.ri.cmu.edu/ont/homework/cmu-ri-project-ont.daml" },
				{
			"testing/arp/onts/ont154",
				"http://www.daml.ri.cmu.edu/ont/homework/cmu-ri-publications-ont.daml" },
				{
			"testing/arp/onts/ont155",
				"http://www.daml.ri.cmu.edu/ont/USCity.daml" },
				{
			"testing/arp/onts/ont156",
				"http://www.daml.ri.cmu.edu/ont/USRegionState.daml" },
				{
			"testing/arp/onts/ont157",
				"http://www.davincinetbook.com:8080/daml/rdf/homework3/projectGutenbergOnt.daml" },
				{
			"testing/arp/onts/ont161",
				"http://www.isi.edu/webscripter/bibtex.o.daml" },
				{
			"testing/arp/onts/ont162",
				"http://www.isi.edu/webscripter/communityreview/abstract-review-o" },
				{
			"testing/arp/onts/ont163",
				"http://www.isi.edu/webscripter/communityreview/scientific-review-o" },
				{
			"testing/arp/onts/ont164",
				"http://www.isi.edu/webscripter/division.o.daml" },
				{
			"testing/arp/onts/ont165",
				"http://www.isi.edu/webscripter/document.o.daml" },
				{
			"testing/arp/onts/ont166",
				"http://www.isi.edu/webscripter/event.o.daml" },
				{
			"testing/arp/onts/ont167",
				"http://www.isi.edu/webscripter/person.o.daml" },
				{
			"testing/arp/onts/ont168",
				"http://www.isi.edu/webscripter/project.o.daml" },
				{
			"testing/arp/onts/ont169",
				"http://www.isi.edu/webscripter/publication.o.daml" },
				{
			"testing/arp/onts/ont170",
				"http://www.isi.edu/webscripter/snapshot.o.daml" },
				{
			"testing/arp/onts/ont171",
				"http://www.isi.edu/webscripter/todo.o.daml" },
				{
			"testing/arp/onts/ont172",
				"http://www.kestrel.edu/DAML/2000/12/CAPACITY.daml" },
				{
			"testing/arp/onts/ont173",
				"http://www.kestrel.edu/DAML/2000/12/DEMAND.daml" },
				{
			"testing/arp/onts/ont174",
				"http://www.kestrel.edu/DAML/2000/12/instances.daml" },
				{
			"testing/arp/onts/ont175",
				"http://www.kestrel.edu/DAML/2000/12/OPERATION.daml" },
				{
			"testing/arp/onts/ont176",
				"http://www.kestrel.edu/DAML/2000/12/RESOURCE.daml" },
				{
			"testing/arp/onts/ont177",
				"http://www.kestrel.edu/DAML/2000/12/TIME.daml" },
				{
			"testing/arp/onts/ont178",
				"http://www.ksl.stanford.edu/projects/DAML/ksl-daml-desc.daml" },
				{
			"testing/arp/onts/ont179",
				"http://www.ksl.stanford.edu/projects/DAML/ksl-daml-instances.daml" },
				{
			"testing/arp/onts/ont181",
				"http://www.lgi2p.ema.fr/~ranwezs/ontologies/musicV1.0.daml" },
				{
			"testing/arp/onts/ont182",
				"http://www.lgi2p.ema.fr/~ranwezs/ontologies/soccerV2.0.daml" },
				{
			"testing/arp/onts/ont183",
				"http://www.semanticweb.org/library/wordnet/wordnet-20000620.rdfs" },
				{
			"testing/arp/onts/ont184",
				"http://www.semanticweb.org/ontologies/swrc-onto-2000-09-10.daml" },
				{
			"testing/arp/onts/ont185",
				"http://www.w3.org/2000/10/annotation-ns#" },
				{
			"testing/arp/onts/ont186",
				"http://www.w3.org/2000/10/annotationType#" },
				{
			"testing/arp/onts/ont187",
				"http://www.w3.org/2000/10/swap/infoset/infoset-diagram.rdf" },
				{
			"testing/arp/onts/ont188",
				"http://www.w3.org/2000/10/swap/pim/contact.rdf" },
				{
			"testing/arp/onts/ont189",
				"http://www.w3.org/2000/10/swap/pim/doc.rdf" },
				{
			"testing/arp/onts/ont190",
				"http://www.w3.org/2001/03/earl/0.95.rdf" },
				{
			"testing/arp/onts/ont191", "http://www.w3.org/2001/03/thread" }, {
			"testing/arp/onts/ont192",
				"http://www.w3.org/2001/05/rdf-ds/datastore-schema" },
                */
				};
	static String files[][];
	static int totalLength;
    static int totalTime;
	static public void main(String args[]) {
		int k;
		files = new String[20][];
		for (int ii = 0;
			ii + files.length <= allFiles.length;
			ii += files.length) {
			for (k = 0; k < files.length; k++)
				files[k] = allFiles[ii+k];
			totalLength = 0;
            double s1 = speed();
			double s2 = speed();
			System.err.println(
					files[0][0]
						+ "\t"
						+ totalLength
						+ "\t"
						+ s1 +"\t" + s2 
						+ "\t"
						+ files[0][1]);
			
		}
        System.err.println("Total time: " + totalTime + " ms");
	}
	static byte[][] load() {
		byte rslt[][] = new byte[files.length][];
		for (int i = 0; i < files.length; i++) {
			File f = new File(files[i][0]);
			totalLength += (int) f.length();
			if (f.length() == 0)
				continue;
			rslt[i] = new byte[(int) f.length()];
			try ( InputStream in = new FileInputStream(f) ) {
				in.read(rslt[i]);
			} catch (IOException e) {
				System.err.println(files[i][1] + " " + e.getMessage());
			}
		}
		return rslt;
	}
	static int sum(byte array[]) {
		int r = 0;
		if (array == null)
			return 0;
        for ( byte anArray : array )
        {
            r += anArray;
        }
		return r;
	}
	static double speed() {
		boolean realData = false;
		byte data[][] = load();
		int s = 0;
		long startRead = System.currentTimeMillis();
		for (int i = 0; i < files.length; i++)
			for (int k = 0; k < 50; k++)
				s += sum(data[i]);
		long startTest = System.currentTimeMillis();
		for (int i = 0; i < files.length; i++) {
		    if (data[i] == null)
		        continue;
		    realData = true;
		    parseRDF(new ByteArrayInputStream(data[i]), files[i][1]);
		}
		long endTime = System.currentTimeMillis();
        totalTime += (int)(endTime-startTest);
		if (!realData)
			return Double.NaN;
		return (double) (endTime - startTest)
			/ (double) (startTest - startRead);
	}
	static void parseRDF(InputStream in, String base) {
		ErrorHandler eh = new ErrorHandler() {
			@Override
            public void warning(SAXParseException e) {
			}
			@Override
            public void error(SAXParseException e) {
			}
			@Override
            public void fatalError(SAXParseException e) {
			}
		};
		InputStream oldIn = System.in;
		//InputStream ntIn = null;    // Not used
		try {
			System.setIn(in);
			NTriple.mainEh(
				new String[] { "-b", base, "-t" },
				eh, null);

		} catch (Exception e) {
			System.err.println(base + " " + e.toString());
		} finally {
			System.setIn(oldIn);
//			if (ntIn != null)
//				ntIn.close();
		}
	}

}
