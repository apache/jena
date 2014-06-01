package org.apache.jena.riot.adapters;

import java.io.InputStream;
import java.io.Reader;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFErrorHandler;
import com.hp.hpl.jena.rdf.model.RDFReader;

public class RDFReaderRIOT_CSV implements RDFReader{

	private RDFReader reader ;
	public RDFReaderRIOT_CSV(){
		reader = new RDFReaderRIOT("CSV");
	}

	@Override
	public void read(Model model, Reader r, String base) {
		reader.read(model, r, base);
		
	}

	@Override
	public void read(Model model, InputStream r, String base) {
		reader.read(model, r, base);
		
	}

	@Override
	public void read(Model model, String url) {
		reader.read(model, url);
		
	}

	@Override
	public Object setProperty(String propName, Object propValue) {
		return reader.setProperty(propName, propValue);
	}

	@Override
	public RDFErrorHandler setErrorHandler(RDFErrorHandler errHandler) {
		return reader.setErrorHandler(errHandler);
	}
}
