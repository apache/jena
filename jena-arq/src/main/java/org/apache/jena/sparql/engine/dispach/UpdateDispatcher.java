package org.apache.jena.sparql.engine.dispach;

import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.exec.UpdateExec;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.update.UpdateRequest;

public interface UpdateDispatcher {
    UpdateExec create(String updateRequestString, DatasetGraph dsg, Binding initialBinding, Context context);
    UpdateExec create(UpdateRequest updateRequest, DatasetGraph dsg, Binding initialBinding, Context context);
}
