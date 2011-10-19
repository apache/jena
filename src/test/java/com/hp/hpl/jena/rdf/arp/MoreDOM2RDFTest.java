/*
 * (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 * [See end of file]
 */

package com.hp.hpl.jena.rdf.arp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.arp.ALiteral;
import com.hp.hpl.jena.rdf.arp.AResource;
import com.hp.hpl.jena.rdf.arp.DOM2Model;
import com.hp.hpl.jena.rdf.arp.StatementHandler;

/**
 * @author Jeremy J. Carroll
 *
 */
public class MoreDOM2RDFTest extends TestCase implements StatementHandler {

    int count = 0;

	public MoreDOM2RDFTest(String name) {
		super(name);
	}
	
	static private DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    // DOM must have namespace information inside it!
	static { factory.setNamespaceAware(true);}
	static private DocumentBuilder domParser;
	
	static {
		try {
		domParser = factory.newDocumentBuilder();
		}
		catch (ParserConfigurationException rte){
			throw new RuntimeException(rte);
		}
	}
	

	public void testDOMwithARP() throws SAXException, IOException {
		
        InputStream in = new FileInputStream("testing/wg/Class/conclusions001.rdf");
		Document document = domParser
				.parse(in,"http://www.example.org/");
			
		DOM2Model d2m = DOM2Model.createD2M("http://www.example.org/",null);	

		d2m.getHandlers().setStatementHandler(this);
		
			try {
		        d2m.load(document);
			} finally {
				d2m.close();
			}
		
         assertEquals("Incorrect number of triples",3,count);

	}


    @Override
    public void statement(AResource subj, AResource pred, AResource obj) {
        count++;
        
    }


    @Override
    public void statement(AResource subj, AResource pred, ALiteral lit) {
        count++;
        
    }

}


/*
 *  (c) Copyright 2004, 2005, 2006, 2007, 2008, 2009 Hewlett-Packard Development Company, LP
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 
