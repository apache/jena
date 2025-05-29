package org.apache.jena.sparql.exec.tracker;

import java.util.stream.IntStream;

import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.exec.QueryExec;
import org.apache.jena.sparql.exec.RowSet;
import org.apache.jena.sparql.exec.RowSetOps;
import org.apache.jena.sparql.exec.UpdateExec;
import org.junit.Test;

public class TestExecTracker2 {
    @Test
    public void test() {
        DatasetGraph dsg = DatasetGraphFactory.createTxnMem();
        IntStream.range(0, 1000).mapToObj(i -> NodeFactory.createURI("urn:foo:bar" + i))
            .forEach(x -> dsg.getDefaultGraph().add(x, x, x));

        TaskTrackerRegistry registry = TaskTrackerRegistry.getOrSet(dsg.getContext());

        TaskListener<BasicTaskExec> listener = new TaskListener<>() {
            @Override
            public void onStateChange(BasicTaskExec task) {
                System.out.println(task.getState() + " " + task.getDescription() + " throwable: " + task.getThrowable());
                // task.abort();
            }
        };

        // registry.addListener(QueryExecTask.class, listener);
        Runnable deregister = registry.addListener(UpdateExecTask.class, listener);
        // deregister.run();

        // QueryExecRegistry.
        try (QueryExec qe = TaskTrackerRegistry.track(QueryExec.dataset(dsg).query("SELECT (COUNT(*) AS ?c ) { ?s ?p ?o }").build())) {
            System.out.println("in here");
            RowSet rs = qe.select();
            RowSetOps.count(rs);
        }

        DatasetGraph dsg2 = DatasetGraphFactory.empty();
        UpdateExec ue = TaskTrackerRegistry.track(dsg.getContext(), UpdateExec.dataset(dsg2).update("INSERT DATA { <s> <p> <o> }").build());
        UpdateExecTask ueTask = (UpdateExecTask)ue;
        try {
            ueTask.execute();
        } catch (Throwable t) {
            // Ignore execution error here because it should be tracked in the task.
        }

        System.out.println(ueTask.getThrowable());
        System.out.println(ueTask.getFinishTime());

//        DatasetGraph finalDsg = DatasetGraphWithExecTracker.wrap(dsg);
//        ExecTracker execTracker = ExecTracker.requireTracker(dsg.getContext());
//
//        IntStream.range(0, 1000).boxed().collect(Collectors.toCollection(ArrayList::new)).parallelStream().forEach(x -> {
////            Table actualTable = QueryExec.newBuilder()
////                    .dataset(finalDsg).query("SELECT * { ?s ?p ?o }").table();
////            System.out.println("" + x + ": " + execTracker);
//            Txn.executeWrite(dsg, () -> {
//                UpdateExec.dataset(finalDsg).update("INSERT { ?s ?p 1 } WHERE { ?s ?p ?o }").execute();
//            });
//        });
//        System.out.println("Result: " + execTracker);
   }

}
