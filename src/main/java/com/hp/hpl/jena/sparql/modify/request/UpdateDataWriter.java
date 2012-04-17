package com.hp.hpl.jena.sparql.modify.request;

import org.openjena.atlas.io.IndentedWriter;
import org.openjena.riot.out.SinkQuadBracedOutput;

import com.hp.hpl.jena.sparql.serializer.SerializationContext;

public class UpdateDataWriter extends SinkQuadBracedOutput
{
    /**
     * The mode an UpdateDataWriter is in.
     */
    public enum UpdateMode
    {
        INSERT,
        DELETE,
    }
    
    private final UpdateMode mode;
    
    public UpdateDataWriter(UpdateMode mode, IndentedWriter out, SerializationContext sCxt)
    {
        super(out, sCxt);
        this.mode = mode;
    }
    
    public UpdateMode getMode()
    {
        return mode;
    }
    
    @Override
    public void open()
    {
        out.ensureStartOfLine();
        out.print(mode.toString());
        out.print(" DATA ");
        super.open();
    }
}
