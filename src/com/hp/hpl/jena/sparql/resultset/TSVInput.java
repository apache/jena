package com.hp.hpl.jena.sparql.resultset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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

	static Pattern pattern = Pattern.compile("\t");
	
    public static ResultSet fromTSV(InputStream in) {
    	BufferedReader reader = IO.asBufferedUTF8(in);
        List<Var> vars = new ArrayList<Var>();
        List<String> varNames = new ArrayList<String>();
        List<Binding> bindings = new ArrayList<Binding>();

        boolean first = true;
    	String str = null;
    	int line = 0;
        try {
        	while ( ( str = reader.readLine() ) != null ) {
        		line++;
        		String[] tokens = pattern.split(str);
        		if ( first ) {
        			for ( String token : tokens ) {
                		if ( token.startsWith("?") ) 
                			token = token.substring(1);
                		Var var = Var.alloc(token);
                		vars.add(var);
                		varNames.add(var.getName());
                	}
                	first = false;
        		} else {
        			int num_tokens = tokens.length;
        			if ( str.endsWith("\t") )
        				num_tokens += 1;
        	        if ( num_tokens != vars.size() ) {
        	        	 throw new ARQException(String.format("Line %d has %d values instead of %d.", line, num_tokens, vars.size()));
        	        }
        	        Binding binding = BindingFactory.create();
        	        for ( int i = 0; i < tokens.length; i++ ) {
        	        	String token = tokens[i];
                		Tokenizer tokenizer = TokenizerFactory.makeTokenizerString(token);
                		if ( tokenizer.hasNext() && token.length() > 0 ) {
                			Node node = tokenizer.next().asNode();
                			binding.add(vars.get(i), node);
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
