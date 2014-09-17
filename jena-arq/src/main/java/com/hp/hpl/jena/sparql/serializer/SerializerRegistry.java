package com.hp.hpl.jena.sparql.serializer;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.riot.system.IRIResolver;

import com.hp.hpl.jena.query.QueryVisitor;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.sparql.core.Prologue;
import com.hp.hpl.jena.sparql.modify.request.UpdateSerializer;
import com.hp.hpl.jena.sparql.modify.request.UpdateWriter;
import com.hp.hpl.jena.sparql.util.NodeToLabelMapBNode;

/**
 * Provides a registry of serializers for queries and updates
 * 
 */
public class SerializerRegistry {

    private Map<Syntax, QuerySerializerFactory> querySerializers = new HashMap<>();
    private Map<Syntax, UpdateSerializerFactory> updateSerializers = new HashMap<>();

    private SerializerRegistry() {
    }

    private static SerializerRegistry registry;

    private static synchronized void init() {
        SerializerRegistry reg = new SerializerRegistry();

        // Register standard serializers
        QuerySerializerFactory arqQuerySerializerFactory = new QuerySerializerFactory() {

            @Override
            public QueryVisitor create(Syntax syntax, Prologue prologue, IndentedWriter writer) {
                // For the query pattern
                SerializationContext cxt1 = new SerializationContext(prologue, new NodeToLabelMapBNode("b", false));
                // For the construct pattern
                SerializationContext cxt2 = new SerializationContext(prologue, new NodeToLabelMapBNode("c", false));

                return new QuerySerializer(writer, new FormatterElement(writer, cxt1), new FmtExprSPARQL(writer, cxt1),
                        new FmtTemplate(writer, cxt2));
            }

            @Override
            public boolean accept(Syntax syntax) {
                // Since ARQ syntax is a super set of SPARQL 1.1 both SPARQL 1.0
                // and SPARQL 1.1 can be serialized by the same serializer
                return Syntax.syntaxARQ.equals(syntax) || Syntax.syntaxSPARQL_10.equals(syntax)
                        || Syntax.syntaxSPARQL_11.equals(syntax);
            }
        };
        reg.addQuerySerializer(Syntax.syntaxARQ, arqQuerySerializerFactory);
        reg.addQuerySerializer(Syntax.syntaxSPARQL_10, arqQuerySerializerFactory);
        reg.addQuerySerializer(Syntax.syntaxSPARQL_11, arqQuerySerializerFactory);

        UpdateSerializerFactory arqUpdateSerializerFactory = new UpdateSerializerFactory() {

            @Override
            public UpdateSerializer create(Syntax syntax, Prologue prologue, IndentedWriter writer) {
                if ( ! prologue.explicitlySetBaseURI() )
                    prologue = new Prologue(prologue.getPrefixMapping(), (IRIResolver)null) ;
                
                SerializationContext context = new SerializationContext(prologue);
                return new UpdateWriter(writer, context);
            }

            @Override
            public boolean accept(Syntax syntax) {
                // Since ARQ syntax is a super set of SPARQL 1.1 both SPARQL 1.0
                // and SPARQL 1.1 can be serialized by the same serializer
                return Syntax.syntaxARQ.equals(syntax) || Syntax.syntaxSPARQL_10.equals(syntax)
                        || Syntax.syntaxSPARQL_11.equals(syntax);
            }
        };
        reg.addUpdateSerializer(Syntax.syntaxARQ, arqUpdateSerializerFactory);
        reg.addUpdateSerializer(Syntax.syntaxSPARQL_10, arqUpdateSerializerFactory);
        reg.addUpdateSerializer(Syntax.syntaxSPARQL_11, arqUpdateSerializerFactory);

        registry = reg;
    }

    public static SerializerRegistry get() {
        if (registry == null)
            init();

        return registry;
    }

    public void addQuerySerializer(Syntax syntax, QuerySerializerFactory factory) {
        if (!factory.accept(syntax))
            throw new IllegalArgumentException("Factory does not accept the specified syntax");
        querySerializers.put(syntax, factory);
    }

    public void addUpdateSerializer(Syntax syntax, UpdateSerializerFactory factory) {
        if (!factory.accept(syntax))
            throw new IllegalArgumentException("Factory does not accept the specified syntax");
        updateSerializers.put(syntax, factory);
    }

    public boolean containsQuerySerializer(Syntax syntax) {
        return querySerializers.containsKey(syntax);
    }

    public boolean containsUpdateSerializer(Syntax syntax) {
        return updateSerializers.containsKey(syntax);
    }

    public QuerySerializerFactory getQuerySerializerFactory(Syntax syntax) {
        return querySerializers.get(syntax);
    }

    public UpdateSerializerFactory getUpdateSerializerFactory(Syntax syntax) {
        return updateSerializers.get(syntax);
    }

    public void removeQuerySerializer(Syntax syntax) {
        querySerializers.remove(syntax);
    }

    public void removeUpdateSerializer(Syntax syntax) {
        updateSerializers.remove(syntax);
    }
}
