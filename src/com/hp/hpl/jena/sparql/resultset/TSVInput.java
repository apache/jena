package com.hp.hpl.jena.sparql.resultset;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.openjena.riot.tokens.Token;
import org.openjena.riot.tokens.Tokenizer;
import org.openjena.riot.tokens.TokenizerFactory;

import com.hp.hpl.jena.query.ResultSet;
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
        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(in);
        List<String> varNames = new ArrayList<String>();
        List<Binding> bindings = new ArrayList<Binding>();
        
        // reads the variables
        List<Var> vars = new ArrayList<Var>();
        Token token = null;
        while (tokenizer.getLine() == 1)
        {
            token = tokenizer.next();

            if (token.isWord()) 
            {
                vars.add(Var.alloc(token.asWord()));
                varNames.add(vars.get(vars.size() - 1).getName());
            }
        }
        
        Binding binding = BindingFactory.create();;
        // the first token from the second line is already
        // consumed, hence we have to apply a specific 
        // behavior to handle it
        binding.add(vars.get(0), token.asNode());
        for (byte i=1; i<vars.size(); i++)
        {
            binding.add(vars.get(i), tokenizer.next().asNode());
        }
        bindings.add(binding);
        
        // reads the next lines
        while (tokenizer.hasNext()) 
        {
            binding = BindingFactory.create();
            
            // reads each node from a line
            for (byte i=1; i<vars.size(); i++)
            {
                binding.add(vars.get(i), tokenizer.next().asNode());
            }
            
            bindings.add(binding);
        }
        
        return new ResultSetStream(varNames, null, new QueryIterPlainWrapper(bindings.iterator()));
    }

}
