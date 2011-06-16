package com.hp.hpl.jena.sparql.resultset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.openjena.atlas.io.IO;
import org.openjena.riot.tokens.Tokenizer;
import org.openjena.riot.tokens.TokenizerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.ARQException;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ResultSetStream;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;

/**
 * Input reader associated to {@link TSVOutput}.
 * 
 * @author Laurent Pellegrino
 */
public class TSVInput {

    public static ResultSet fromTSV(InputStream in) {
    	BufferedReader reader = IO.asBufferedUTF8(in);
        List<Var> vars = new ArrayList<Var>();
        List<String> varNames = new ArrayList<String>();
        List<Binding> bindings = new ArrayList<Binding>();

        boolean first = true;
        try {
        	String line = null;
        	while ( ( line = reader.readLine() ) != null ) {
            	StringTokenizer st = new StringTokenizer(line, "\t");
        		if ( first ) {
                	while ( st.hasMoreTokens() ) {
                		String token = st.nextToken();
                		if ( token.startsWith("?") ) 
                			token = token.substring(1);
                		Var var = Var.alloc(token);
                		vars.add(var);
                		varNames.add(var.getName());
                	}
                	first = false;
        		} else {
        			int i = 0;
        	        Binding binding = BindingFactory.create();
                	while ( st.hasMoreTokens() ) {
                		String token = st.nextToken();
                		Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(token);
                		if ( tokenizer.hasNext() ) {
                			Node node = tokenizer.next().asNode();
                			binding.add(vars.get(i), node);
                			i++;
                		}
                	}
                	bindings.add(binding);
        		}
        	}
        } catch ( IOException ex ) {
        	throw new ARQException(ex) ;
        }

        return new ResultSetStream(varNames, null, new QueryIterPlainWrapper(bindings.iterator()));
    }

}
