
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.sax.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import com.hp.hpl.jena.rdf.arp.*;
import com.hp.hpl.jena.rdf.model.*;

/**
 * @author Jeremy J. Carroll
 *  
 */
public class DOM2Jena {

	public static void main(String args[]) throws Exception {
		
		// Create DOM:
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    // DOM must have namespace information inside it!
		factory.setNamespaceAware(true);
		DocumentBuilder domParser = factory.newDocumentBuilder();
		Document document = domParser
				.parse(new File("testing/wg/Manifest.rdf"));
		// Make DOM into transformer input
		Source input = new DOMSource(document);
		
		// Make SAX2Model SAX event handler
		Model m = ModelFactory.createDefaultModel();
		SAX2Model handler = SAX2Model.create(
				"http://www.w3.org/2000/10/rdf-tests/rdfcore/Manifest.rdf", m);

		
		// Make a SAXResult object using this handler
		SAXResult output = new SAXResult(handler);
		output.setLexicalHandler(handler);
		
		// Run transform
		TransformerFactory xformFactory = TransformerFactory.newInstance();
		Transformer idTransform = xformFactory.newTransformer();
		idTransform.transform(input, output);

		// Use Model
		m.write(System.out, "N-TRIPLE");
	}

}

