/*
 * (c) Copyright 2004 Hewlett-Packard Development Company, LP [See end of file]
 */

package com.hp.hpl.jena.rdf.arp;

import java.util.Arrays;

import com.hp.hpl.jena.graph.BulkUpdateHandler;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.graph.*;

final class JenaHandler extends ARPSaxErrorHandler implements StatementHandler,
		NamespaceHandler {
	static private final int BULK_UPDATE_SIZE = 1000;

	private final BulkUpdateHandler bulk;

	private final Model model;

	final Triple triples[];

	int ix = 0;
	JenaHandler(Model m,
			RDFErrorHandler e) {
		this(m.getGraph(),m,e);
	}
	JenaHandler(Graph g,
			RDFErrorHandler e) {
		this(g,null,e);
	}	
	JenaHandler(Graph g, Model m,
			RDFErrorHandler e) {
		this(g.getBulkUpdateHandler(),m,e);
	}
	private JenaHandler(BulkUpdateHandler bulk, Model model,
			RDFErrorHandler errorHandler) {
		super(errorHandler);
		this.bulk = bulk;
		this.model = model;
		triples = new Triple[BULK_UPDATE_SIZE];
	}

	void useWith(ARPHandlers h) {
		h.setStatementHandler(this);
		h.setErrorHandler(this);
		h.setNamespaceHandler(this);
	}

	public void statement(AResource subj, AResource pred, AResource obj) {
		try {
			triples[ix++] = JenaReader.convert(subj, pred, obj);
		} catch (JenaException e) {
			errorHandler.error(e);
		}
		if (ix == BULK_UPDATE_SIZE)
			bulkUpdate();
	}

	public void statement(AResource subj, AResource pred, ALiteral lit) {
		try {
			triples[ix++] = JenaReader.convert(subj, pred, lit);
		} catch (JenaException e) {
			errorHandler.error(e);
		}
		if (ix == BULK_UPDATE_SIZE)
			bulkUpdate();
	}

	void bulkUpdate() {
		try {
			if (ix == BULK_UPDATE_SIZE)
				bulk.add(triples);
			else
				bulk.add(Arrays.asList(triples).subList(0, ix));

		} catch (JenaException e) {
			errorHandler.error(e);
		} finally {
			ix = 0;
		}
	}

	public void startPrefixMapping(String prefix, String uri) {
		if (model != null && PrefixMappingImpl.isNiceURI(uri))
			model.setNsPrefix(prefix, uri);
	}

	public void endPrefixMapping(String prefix) {
	}
}

/*
 * (c) Copyright 2004 Hewlett-Packard Development Company, LP All rights
 * reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. The name of the author may not
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

