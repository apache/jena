package org.apache.jena.sparql.core.thrift;

import org.apache.jena.riot.system.PrefixMap;
import org.apache.jena.riot.system.PrefixMapFactory;
import org.apache.jena.riot.system.StreamRDFBase;
import org.apache.jena.riot.thrift.TRDF;
import org.apache.jena.riot.thrift.Thrift2StreamRDF;
import org.apache.jena.riot.thrift.wire.RDF_StreamRow;
import org.apache.jena.sparql.core.Quad;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

public class IteratorPaged2EQuad extends IteratorPaged2E<Quad> {
	
    protected TTransport transport;

    protected TProtocol protocol;
    
    protected boolean encodeValues = false;
	
    protected PrefixMap prefixMap = PrefixMapFactory.create();
    
	protected RDF_StreamRow rdf_StreamRow = new RDF_StreamRow();

	protected boolean hasNext = false;
	
	protected Thrift2StreamRDF thrift2StreamRDF = new Thrift2StreamRDF(prefixMap, new StreamRDFBase() {
		@Override
		public void quad(final Quad quad) {
			hasNext = true;
			next = quad;
		}
	});

	public IteratorPaged2EQuad(InputStreamPaged inputStreamPaged) {
		super(inputStreamPaged);
		// TODO We don't use the following because TRDF wraps the InputStream in a BufferedInputStream.
//		TRDF.protocol(inputStreamPaged);
	    transport = new TIOStreamTransport(inputStreamPaged);
	    protocol = TRDF.protocol(transport);
	}

	@Override
	public boolean hasNext() {
		try {
			/*
			 * Loop until we read a Quad.
			 */
			// TODO Exception if we read anything other than a Prefix or Quad?
			next = null;
			while (next == null) {
				rdf_StreamRow.read(protocol);
				TRDF.visit(rdf_StreamRow, thrift2StreamRDF);
		        rdf_StreamRow.clear() ;
			}
		} catch (TTransportException e) {
            if (e.getType() != TTransportException.END_OF_FILE) {
            	e.printStackTrace();
            }
            hasNext = false;
        	next = null;
        } catch (TException ex) {
        	ex.printStackTrace();
        	hasNext = false;
        	next = null;
        }
		return hasNext;
	}
	
}