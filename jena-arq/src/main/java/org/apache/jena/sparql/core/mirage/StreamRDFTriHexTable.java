package org.apache.jena.sparql.core.mirage;

import org.apache.jena.graph.Triple;
import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.mem.HexTable;
import org.apache.jena.sparql.core.mem.TriTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamRDFTriHexTable implements StreamRDF {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StreamRDFTriHexTable.class);

	private final PrefixMap prefixes = PrefixMapFactory.createForInput();
	
	private String baseIRI;
	
	private final TriTable triples;
	
	private final HexTable quads;

	public StreamRDFTriHexTable() {
		super();
		triples = new TriTable();
		quads = new HexTable();
	}

	public StreamRDFTriHexTable(final TriTable triTable, final HexTable hexTable) {
		super();
		this.triples = triTable; 
		this.quads = hexTable;
	}

	public TriTable getTriples() {
		return triples;
	}
	
	public HexTable getQuads() {
		return quads;
	}

	public PrefixMap getPrefixex() {
		return prefixes;
	}

	public String getBaseIRI() {
		return baseIRI;
	}
	
	/*
	 * StreamRDF
	 */
	
	@Override
	public void start() {
	}
	
	@Override
	public void triple(Triple triple) {
		LOGGER.debug("triple(triple=[{}])", triple);
		getTriples().add(triple);
	}

	@Override
	public void quad(Quad quad) {
		LOGGER.debug("quad(quad=[{}]", quad);
		getQuads().add(quad);
	}

	@Override
	public void base(String base) {
		this.baseIRI = base; 
	}

	@Override
	public void prefix(String prefix, String iri) {
		prefixes.add(prefix, iri); 
	}

	@Override
	public void finish() {
	}

}
