package org.apache.jena.update;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.IterAbortable;
import org.apache.jena.sparql.lang.UpdateParser;
import org.apache.jena.sparql.modify.UpdateEngineFactory;
import org.apache.jena.sparql.modify.UpdateEngineRegistry;
import org.apache.jena.sparql.modify.UpdateProcessorStreamingBase;
import org.apache.jena.sparql.util.Context;

public sealed interface UpdateSource {
    // String getBaseURI();
    Iterator<Update> iterator();

    // InputStream input
    // UsingList usingList, String baseURI, Syntax syntax
    public record UpdateSourceIterable(Iterator<Update> iterator) implements UpdateSource {}

    public record UpdateSourceUpdateRequest(UpdateRequest updateRequest) implements UpdateSource {
//        @Override
//        public String getBaseURI() {
//            return updateRequest.getBaseURI();
//        }

        @Override
        public Iterator<Update> iterator() {
            return updateRequest.iterator();
        }
    }


    // Everything for local updates comes through one of these two make methods
    /*package*/ static UpdateProcessorStreaming makeStreaming(DatasetGraph datasetGraph, Binding inputBinding, Context context) {
        Prologue prologue = new Prologue();
        Context cxt = Context.setupContextForDataset(context, datasetGraph);
        UpdateEngineFactory f = UpdateEngineRegistry.get().find(datasetGraph, cxt);
        UpdateProcessorStreamingBase uProc = new UpdateProcessorStreamingBase(datasetGraph, inputBinding, prologue, cxt, f);
        // uProc.getUpdateSink().
        return uProc;
    }

//    public static IterAbortable<Update> toIterator(InputStream input, Prologue prologue) {
//        UpdateParser parser = UpdateFactory.setupParser(uProc.getPrologue(), baseURI, syntax);
//        parser.parse(sink, uProc.getPrologue(), input);
//
//    }
}
