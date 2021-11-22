package mwg.wb.client.graph;
 
 

import org.apache.tinkerpop.gremlin.orientdb.OrientGraph;
import org.apache.tinkerpop.gremlin.orientdb.OrientGraphFactory;
 
import com.orientechnologies.common.concur.ONeedRetryException;
 
import java.util.function.BiFunction;
 
import java.util.logging.Logger;

/**
 * Created by jlowens on 3/22/16.
 */
public class ODB {
    private final OrientGraphFactory factory;
    private final int maxRetries;
    private static Logger log = Logger.getLogger("ODB");

    public ODB(String connectionPath) {
        this(connectionPath, 3);
    }

    public ODB(String connectionPath, String user, String pass) {
        this(connectionPath, user, pass, 3);
    }

    public ODB(String connectionPath, int maxRetries) {
        this.factory = new OrientGraphFactory(connectionPath);
        this.maxRetries = maxRetries;
    }

    public ODB(String connectionPath, String user, String pass, int maxRetries) {
        this.factory = new OrientGraphFactory(connectionPath, user, pass);
        this.factory.setupPool(4, 16);
        this.maxRetries = maxRetries;
    }

    public void disconnect() {
        this.factory.close();
    }

    public interface DBOperation {
        Object withDB(OrientGraph graph);
    }

    private class WithDB implements BiFunction<OrientGraph, DBOperation, Object> {
        @Override
        public Object apply(OrientGraph g, DBOperation f) {
            for (int i = 0; i < maxRetries; i++) {
                try {
                    return f.withDB(g);
                } catch (ONeedRetryException e) {
                    if (i == (maxRetries - 1)) {
                        throw e;
                    }
                } catch (Exception e) {
                    throw e;
                }
            }
	    return null;
        }
    }

    public Object withDB(DBOperation f) {
        OrientGraph g = this.factory.getTx();
        try {
            Object ret = new WithDB().apply(g,f);
            g.commit();
            return ret;
        } catch (Throwable t) {
            g.rollback();
            //t.printStackTrace();
	    throw t;
        } finally {
            g.close(); 
        }
    }

    public static String asClass(String name)
    {
        return "class:" + name;
    }

     

     

    
    
    
}

