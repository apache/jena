package com.hp.hpl.jena.sparql.function.library;
// Contribution from Leigh Dodds 

import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ARQ Extension Function that will calculate the 
 * SHA1 sum of any literal. Useful for working with 
 * FOAF data.
 * 
 * @author ldodds
 */
public class sha1sum extends FunctionBase1 
{
    
    private static char[] HEX_CHAR = 
       { '0', '1', '2', '3', '4', '5', '6', '7',
         '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
   
    // Caching - Andy Seaborne
    NodeValue lastSeen = null ;
    NodeValue lastCalc = null ;
    
    @Override
    public NodeValue exec(NodeValue nodeValue) 
    {
        if ( lastSeen != null && lastSeen.equals(nodeValue) )
            return lastCalc ;
            
        if ( nodeValue.asNode().isBlank() )
            throw new ExprEvalException("Attempt to sha1 a blank node") ;
        
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA");
            // URIs and literals (.asString is a force to node then str())
            String value = nodeValue.asString();
            String hex = getHexString( digest.digest(value.getBytes() ) );
            lastSeen = nodeValue ;
            lastCalc = NodeValue.makeNodeString(hex);
            return lastCalc ;
        } catch (NoSuchAlgorithmException e)
        {
            throw new ExprEvalException("Unable to get SHA1 digester", e) ;
        }
    }
    
    private String getHexString(byte[] bytes)
    {
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        for ( int i = 0; i < bytes.length; i++ )
        {
            sb.append(HEX_CHAR[(bytes[i] & 0xf0) >> 4]);
            sb.append(HEX_CHAR[(bytes[i] & 0x0f)]);
        }
        return sb.toString();
    }
}
