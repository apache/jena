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

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.hp.hpl.jena.rdfxml.xmlinput.impl.XMLHandler ;
import com.hp.hpl.jena.vocabulary.RDF;

public class TestPropEltErrorMsg extends TestCase {
    
    static String rslts[] = {
        null,
        null,
        "pT=L,",
        "",
        "pT=R,",
        "",
        "pT=C,",
        "",
        "t,",
        "",
        "pT=L,t,",
        "On a property element, only one of the attributes rdf:parseType or rdf:type is permitted.\n",
        "pT=R,t,",
        "On a property element, only one of the attributes rdf:parseType or rdf:type is permitted.\n",
        "pT=C,t,",
        "On a property element, only one of the attributes rdf:parseType or rdf:type is permitted.\n",
        "r,",
        "",
        "pT=L,r,",
        "On a property element, only one of the attributes rdf:parseType or rdf:resource is permitted.\n",
        "pT=R,r,",
        "On a property element, only one of the attributes rdf:parseType or rdf:resource is permitted.\n",
        "pT=C,r,",
        "On a property element, only one of the attributes rdf:parseType or rdf:resource is permitted.\n",
        "t,r,",
        "",
        "pT=L,t,r,",
        "On a property element, the attribute rdf:parseType is incompatible with both the attribute rdf:type and the attribute rdf:resource.\n",
        "pT=R,t,r,",
        "On a property element, the attribute rdf:parseType is incompatible with both the attribute rdf:type and the attribute rdf:resource.\n",
        "pT=C,t,r,",
        "On a property element, the attribute rdf:parseType is incompatible with both the attribute rdf:type and the attribute rdf:resource.\n",
        "p,",
        "",
        "pT=L,p,",
        "The attribute rdf:parseType is not permitted with property attributes (eg:prop) on a property element.\n",
        "pT=R,p,",
        "The attribute rdf:parseType is not permitted with property attributes (eg:prop) on a property element.\n",
        "pT=C,p,",
        "The attribute rdf:parseType is not permitted with property attributes (eg:prop) on a property element.\n",
        "t,p,",
        "",
        "pT=L,t,p,",
        "On a property element, the attribute rdf:parseType is incompatible with both the attribute rdf:type and the property attributes (eg:prop).\n",
        "pT=R,t,p,",
        "On a property element, the attribute rdf:parseType is incompatible with both the attribute rdf:type and the property attributes (eg:prop).\n",
        "pT=C,t,p,",
        "On a property element, the attribute rdf:parseType is incompatible with both the attribute rdf:type and the property attributes (eg:prop).\n",
        "r,p,",
        "",
        "pT=L,r,p,",
        "On a property element, the attribute rdf:parseType is incompatible with the property attributes (eg:prop) and the attribute rdf:resource.\n",
        "pT=R,r,p,",
        "On a property element, the attribute rdf:parseType is incompatible with the property attributes (eg:prop) and the attribute rdf:resource.\n",
        "pT=C,r,p,",
        "On a property element, the attribute rdf:parseType is incompatible with the property attributes (eg:prop) and the attribute rdf:resource.\n",
        "t,r,p,",
        "",
        "pT=L,t,r,p,",
        "On a property element, the attribute rdf:parseType is incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the attribute rdf:resource.\n",
        "pT=R,t,r,p,",
        "On a property element, the attribute rdf:parseType is incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the attribute rdf:resource.\n",
        "pT=C,t,r,p,",
        "On a property element, the attribute rdf:parseType is incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the attribute rdf:resource.\n",
        "n,",
        "",
        "pT=L,n,",
        "On a property element, only one of the attributes rdf:parseType or rdf:nodeID is permitted.\n",
        "pT=R,n,",
        "On a property element, only one of the attributes rdf:parseType or rdf:nodeID is permitted.\n",
        "pT=C,n,",
        "On a property element, only one of the attributes rdf:parseType or rdf:nodeID is permitted.\n",
        "t,n,",
        "",
        "pT=L,t,n,",
        "On a property element, the attribute rdf:parseType is incompatible with both the attribute rdf:type and the attribute rdf:nodeID.\n",
        "pT=R,t,n,",
        "On a property element, the attribute rdf:parseType is incompatible with both the attribute rdf:type and the attribute rdf:nodeID.\n",
        "pT=C,t,n,",
        "On a property element, the attribute rdf:parseType is incompatible with both the attribute rdf:type and the attribute rdf:nodeID.\n",
        "r,n,",
        "On a property element, only one of the attributes rdf:nodeID or rdf:resource is permitted.\n",
        "pT=L,r,n,",
        "On a property element, only one of the attributes rdf:parseType, rdf:nodeID or rdf:resource is permitted.\n",
        "pT=R,r,n,",
        "On a property element, only one of the attributes rdf:parseType, rdf:nodeID or rdf:resource is permitted.\n",
        "pT=C,r,n,",
        "On a property element, only one of the attributes rdf:parseType, rdf:nodeID or rdf:resource is permitted.\n",
        "t,r,n,",
        "On a property element, only one of the attributes rdf:nodeID or rdf:resource is permitted.\n",
        "pT=L,t,r,n,",
        "On a property element, the attribute rdf:parseType is incompatible with both the attribute rdf:type and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "pT=R,t,r,n,",
        "On a property element, the attribute rdf:parseType is incompatible with both the attribute rdf:type and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "pT=C,t,r,n,",
        "On a property element, the attribute rdf:parseType is incompatible with both the attribute rdf:type and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "p,n,",
        "",
        "pT=L,p,n,",
        "On a property element, the attribute rdf:parseType is incompatible with the property attributes (eg:prop) and the attribute rdf:nodeID.\n",
        "pT=R,p,n,",
        "On a property element, the attribute rdf:parseType is incompatible with the property attributes (eg:prop) and the attribute rdf:nodeID.\n",
        "pT=C,p,n,",
        "On a property element, the attribute rdf:parseType is incompatible with the property attributes (eg:prop) and the attribute rdf:nodeID.\n",
        "t,p,n,",
        "",
        "pT=L,t,p,n,",
        "On a property element, the attribute rdf:parseType is incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the attribute rdf:nodeID.\n",
        "pT=R,t,p,n,",
        "On a property element, the attribute rdf:parseType is incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the attribute rdf:nodeID.\n",
        "pT=C,t,p,n,",
        "On a property element, the attribute rdf:parseType is incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the attribute rdf:nodeID.\n",
        "r,p,n,",
        "On a property element, only one of the attributes rdf:nodeID or rdf:resource is permitted.\n",
        "pT=L,r,p,n,",
        "On a property element, the attribute rdf:parseType is incompatible with the property attributes (eg:prop) and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "pT=R,r,p,n,",
        "On a property element, the attribute rdf:parseType is incompatible with the property attributes (eg:prop) and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "pT=C,r,p,n,",
        "On a property element, the attribute rdf:parseType is incompatible with the property attributes (eg:prop) and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "t,r,p,n,",
        "On a property element, only one of the attributes rdf:nodeID or rdf:resource is permitted.\n",
        "pT=L,t,r,p,n,",
        "On a property element, the attribute rdf:parseType is incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "pT=R,t,r,p,n,",
        "On a property element, the attribute rdf:parseType is incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "pT=C,t,r,p,n,",
        "On a property element, the attribute rdf:parseType is incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "d,",
        "",
        "pT=L,d,",
        "On a property element, only one of the attributes rdf:parseType or rdf:datatype is permitted.\n",
        "pT=R,d,",
        "On a property element, only one of the attributes rdf:parseType or rdf:datatype is permitted.\n",
        "pT=C,d,",
        "On a property element, only one of the attributes rdf:parseType or rdf:datatype is permitted.\n",
        "t,d,",
        "On a property element, only one of the attributes rdf:datatype or rdf:type is permitted.\n",
        "pT=L,t,d,",
        "On a property element, only one of the attributes rdf:parseType, rdf:datatype or rdf:type is permitted.\n",
        "pT=R,t,d,",
        "On a property element, only one of the attributes rdf:parseType, rdf:datatype or rdf:type is permitted.\n",
        "pT=C,t,d,",
        "On a property element, only one of the attributes rdf:parseType, rdf:datatype or rdf:type is permitted.\n",
        "r,d,",
        "On a property element, only one of the attributes rdf:datatype or rdf:resource is permitted.\n",
        "pT=L,r,d,",
        "On a property element, only one of the attributes rdf:parseType, rdf:datatype or rdf:resource is permitted.\n",
        "pT=R,r,d,",
        "On a property element, only one of the attributes rdf:parseType, rdf:datatype or rdf:resource is permitted.\n",
        "pT=C,r,d,",
        "On a property element, only one of the attributes rdf:parseType, rdf:datatype or rdf:resource is permitted.\n",
        "t,r,d,",
        "On a property element, the attribute rdf:datatype is incompatible with both the attribute rdf:type and the attribute rdf:resource.\n",
        "pT=L,t,r,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with both the attribute rdf:type and the attribute rdf:resource.\n",
        "pT=R,t,r,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with both the attribute rdf:type and the attribute rdf:resource.\n",
        "pT=C,t,r,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with both the attribute rdf:type and the attribute rdf:resource.\n",
        "p,d,",
        "The attribute rdf:datatype is not permitted with property attributes (eg:prop) on a property element.\n",
        "pT=L,p,d,",
        "On a property element, only one of the rdf:parseType or rdf:datatype attributes or property attributes (eg:prop) is permitted.\n",
        "pT=R,p,d,",
        "On a property element, only one of the rdf:parseType or rdf:datatype attributes or property attributes (eg:prop) is permitted.\n",
        "pT=C,p,d,",
        "On a property element, only one of the rdf:parseType or rdf:datatype attributes or property attributes (eg:prop) is permitted.\n",
        "t,p,d,",
        "On a property element, the attribute rdf:datatype is incompatible with both the attribute rdf:type and the property attributes (eg:prop).\n",
        "pT=L,t,p,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with both the attribute rdf:type and the property attributes (eg:prop).\n",
        "pT=R,t,p,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with both the attribute rdf:type and the property attributes (eg:prop).\n",
        "pT=C,t,p,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with both the attribute rdf:type and the property attributes (eg:prop).\n",
        "r,p,d,",
        "On a property element, the attribute rdf:datatype is incompatible with the property attributes (eg:prop) and the attribute rdf:resource.\n",
        "pT=L,r,p,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with the property attributes (eg:prop) and the attribute rdf:resource.\n",
        "pT=R,r,p,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with the property attributes (eg:prop) and the attribute rdf:resource.\n",
        "pT=C,r,p,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with the property attributes (eg:prop) and the attribute rdf:resource.\n",
        "t,r,p,d,",
        "On a property element, the attribute rdf:datatype is incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the attribute rdf:resource.\n",
        "pT=L,t,r,p,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the attribute rdf:resource.\n",
        "pT=R,t,r,p,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the attribute rdf:resource.\n",
        "pT=C,t,r,p,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the attribute rdf:resource.\n",
        "n,d,",
        "On a property element, only one of the attributes rdf:datatype or rdf:nodeID is permitted.\n",
        "pT=L,n,d,",
        "On a property element, only one of the attributes rdf:parseType, rdf:datatype or rdf:nodeID is permitted.\n",
        "pT=R,n,d,",
        "On a property element, only one of the attributes rdf:parseType, rdf:datatype or rdf:nodeID is permitted.\n",
        "pT=C,n,d,",
        "On a property element, only one of the attributes rdf:parseType, rdf:datatype or rdf:nodeID is permitted.\n",
        "t,n,d,",
        "On a property element, the attribute rdf:datatype is incompatible with both the attribute rdf:type and the attribute rdf:nodeID.\n",
        "pT=L,t,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with both the attribute rdf:type and the attribute rdf:nodeID.\n",
        "pT=R,t,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with both the attribute rdf:type and the attribute rdf:nodeID.\n",
        "pT=C,t,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with both the attribute rdf:type and the attribute rdf:nodeID.\n",
        "r,n,d,",
        "On a property element, only one of the attributes rdf:datatype, rdf:nodeID or rdf:resource is permitted.\n",
        "pT=L,r,n,d,",
        "On a property element, only one of the attributes rdf:parseType, rdf:datatype, rdf:nodeID or rdf:resource is permitted.\n",
        "pT=R,r,n,d,",
        "On a property element, only one of the attributes rdf:parseType, rdf:datatype, rdf:nodeID or rdf:resource is permitted.\n",
        "pT=C,r,n,d,",
        "On a property element, only one of the attributes rdf:parseType, rdf:datatype, rdf:nodeID or rdf:resource is permitted.\n",
        "t,r,n,d,",
        "On a property element, the attribute rdf:datatype is incompatible with both the attribute rdf:type and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "pT=L,t,r,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with both the attribute rdf:type and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "pT=R,t,r,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with both the attribute rdf:type and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "pT=C,t,r,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with both the attribute rdf:type and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "p,n,d,",
        "On a property element, the attribute rdf:datatype is incompatible with the property attributes (eg:prop) and the attribute rdf:nodeID.\n",
        "pT=L,p,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with the property attributes (eg:prop) and the attribute rdf:nodeID.\n",
        "pT=R,p,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with the property attributes (eg:prop) and the attribute rdf:nodeID.\n",
        "pT=C,p,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with the property attributes (eg:prop) and the attribute rdf:nodeID.\n",
        "t,p,n,d,",
        "On a property element, the attribute rdf:datatype is incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the attribute rdf:nodeID.\n",
        "pT=L,t,p,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the attribute rdf:nodeID.\n",
        "pT=R,t,p,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the attribute rdf:nodeID.\n",
        "pT=C,t,p,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the attribute rdf:nodeID.\n",
        "r,p,n,d,",
        "On a property element, the attribute rdf:datatype is incompatible with the property attributes (eg:prop) and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "pT=L,r,p,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with the property attributes (eg:prop) and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "pT=R,r,p,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with the property attributes (eg:prop) and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "pT=C,r,p,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with the property attributes (eg:prop) and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "t,r,p,n,d,",
        "On a property element, the attribute rdf:datatype is incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "pT=L,t,r,p,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "pT=R,t,r,p,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
        "pT=C,t,r,p,n,d,",
        "On a property element, the mutually incompatible attributes rdf:datatype and rdf:parseType are incompatible with each of the attribute rdf:type, the property attributes (eg:prop) and the mutually incompatible attributes rdf:nodeID and rdf:resource.\n",
    };
    static private class Att {
        private final String prefix;
        private final String localName;
        private final String value;
        
        Att(String p, String n, String v) {
            prefix = p;
            localName = n;
            value = v;
        }
        String getURI() {
            return prefix.equals("rdf")?RDF.getURI():"http://example.org/";
        }

        public String getValue() {
            return value;
        }

        public String getQName() {
            return prefix + ":" + localName;
        }

        public String getLocalName() {
           return localName;
        }
    }
        
    static private class Atts implements Attributes {
        ArrayList<Att> atts = new ArrayList<>();
        @Override
        public int getLength() {
            return atts.size();
        }
        @Override
        public String getURI(int index) {
            return atts.get(index).getURI();
        }
        @Override
        public String getLocalName(int index) {
            return atts.get(index).getLocalName();
        }

        @Override
        public String getQName(int index) {
            return atts.get(index).getQName();
        }

        @Override
        public String getType(int index) {
            return null;
        }

        @Override
        public String getValue(int index) {
            return atts.get(index).getValue();
        }

        @Override
        public int getIndex(String uri, String localName) {
            return -1;
        }

        @Override
        public int getIndex(String qName) {
            return -1;
        }

        @Override
        public String getType(String uri, String localName) {
            return null;
        }

        @Override
        public String getType(String qName) {
            return null;
        }

        @Override
        public String getValue(String uri, String localName) {
            return null;
        }

        @Override
        public String getValue(String qName) {
            return null;
        }
        public void add(Att att) {
            atts.add(att);
        }
        
    }
    
    final Atts testAtts;
    final int n;

	public TestPropEltErrorMsg(String name, Atts atts, int i) {
		super(name);
        testAtts = atts;
        n = i;
	}
	@Override
    public String toString() {
		return getName();
	}

	public static Test suite() {
		TestSuite s= new TestSuite();
		s.setName("ARP Property Element Error Messages");
        for (int i=1;i<128;i++) {
            Atts atts = new Atts();
            StringBuilder name = new StringBuilder();
            switch(i&3) {
            case 0:
                break;
            case 1:
                atts.add(new Att("rdf","parseType","Literal"));
                name.append("pT=L,");
                break;
            case 2:
                atts.add(new Att("rdf","parseType","Resource"));
                name.append("pT=R,");
                break;
            case 3:
                atts.add(new Att("rdf","parseType","Collection"));
                name.append("pT=C,");
                break;
            }    
            if ((i&4)==4){    
                atts.add(new Att("rdf","type","foo"));
                name.append("t,");
            }
            if ((i&8)==8) {
                atts.add(new Att("rdf","resource","foo"));
                name.append("r,");
            }
            if ((i&16)==16) {
                atts.add(new Att("eg","prop","foo"));
                name.append("p,");
            }
            if ((i&32)==32) {
                atts.add(new Att("rdf","nodeID","foo"));
                name.append("n,");
            }
            if ((i&64)==64) {
                atts.add(new Att("rdf","datatype","foo"));
                name.append("d,");
            }
            s.addTest(new TestPropEltErrorMsg(name.toString(),atts,i));
            
        }
        return s;
	}
	@Override
    protected void runTest() {
        Attributes noAtts = new Atts();
		final StringBuffer buf = new StringBuffer();
		XMLHandler arp = new XMLHandler();
		arp.getHandlers().setErrorHandler(new ErrorHandler() {

			@Override
            public void warning(SAXParseException exception) {
				buf.append(exception.getMessage());
				buf.append("\n");
			}

			@Override
            public void error(SAXParseException e) {
				warning(e);
			}

			@Override
            public void fatalError(SAXParseException e) {
				warning(e);
			}

		});
		try {
		arp.initParse("http://example.org/","");
        arp.startElement(RDF.getURI(),"RDF","rdf:RDF",noAtts);
        arp.startElement(RDF.getURI(),"Description","rdf:Description",noAtts);
        arp.startElement(RDF.getURI(),"value","rdf:value",testAtts);
        }
		catch (SAXException e){
			
		}

//        System.err.println("===");
        
        
//        System.err.println("\""+getName()+"\",");

//        System.err.println("---");
        String contents = buf.toString();

        
//        System.err.println("\""+(contents.length()>7?contents.substring(7).replace("\n","\\n"):"")+"\",");
		assertEquals("test data muddled",rslts[n*2],getName());
        assertTrue("error message has changed.",contents.endsWith(rslts[n*2+1]));
        contents = null;
	}
	
	
}
