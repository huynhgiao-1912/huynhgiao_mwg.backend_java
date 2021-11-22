package mwg.wb.client.graph;

import com.orientechnologies.common.concur.ONeedRetryException;
import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.command.script.OCommandScriptException;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.db.ODatabaseSession;
import com.orientechnologies.orient.core.db.OrientDB;
import com.orientechnologies.orient.core.db.OrientDBConfig;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.exception.ODatabaseException;
import com.orientechnologies.orient.core.exception.OSchemaException;
import com.orientechnologies.orient.core.sql.OCommandSQLParsingException;
import com.orientechnologies.orient.core.sql.executor.OResultSet;
import com.orientechnologies.orient.core.tx.OTransaction;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

/**
 * OrientDatabase is the database client for connecting to OrientDB.
 */
public class OrientDatabase implements Closeable {
    private OrientDB orient;
    private ODatabaseSession db;

    /**
     * Create new OrientDB Client.
     *
     * @param host     The remote OrientDB host
     * @param port     The remote OrientDB port
     * @param database the database to open
     * @param username username of a database user or a server user allowed to open the database
     * @param password related to the specified username
     * @throws IOException
     */
    public OrientDatabase(String host, String port, String database, String username, String password) throws IOException {
        orient = new OrientDB("remote:" + host + ":" + port, OrientDBConfig.defaultConfig());
        db = orient.open(database, username, password);
    }

    /**
     * Begins a new transaction. By default the type is OPTIMISTIC. If a previous transaction was started it will be rollbacked and
     * closed before to start a new one. A transaction once begun has to be closed by calling the {@link #commit()} or
     * {@link #rollback()}.
     */
    public void begin() {
        db.begin();
    }

    /**
     * Begins a new transaction specifying the transaction type. If a previous transaction was started it will be rollbacked and
     * closed before to start a new one. A transaction once begun has to be closed by calling the {@link #commit()} or
     * {@link #rollback()}.
     */
    public void begin(OTransaction.TXTYPE iStatus) {
        db.begin(iStatus);
    }

    /**
     * Commits the current transaction. The approach is all or nothing. All changes will be permanent following the storage type. If
     * the operation succeed all the entities changed inside the transaction context will be effectives. If the operation fails, all
     * the changed entities will be restored in the datastore. Memory instances are not guaranteed to being restored as well.
     */
    public void commit() {
        db.commit();
    }

    /**
     * Aborts the current running transaction. All the pending changed entities will be restored in the datastore. Memory instances
     * are not guaranteed to being restored as well.
     */
    public void rollback() {
        db.rollback();
    }

    /**
     * Tries to execute a lambda in a transaction, retrying it if an ONeedRetryException is thrown.
     * <p>
     * If the DB does not have an active transaction, after the execution you will still be out of tx.
     * <p>
     * If the DB has an active transaction, then the transaction has to be empty (no operations executed yet)
     * and after the execution you will be in a new transaction.
     *
     * @param nRetries the maximum number of retries (> 0)
     * @param function a lambda containing application code to execute in a commit/retry loop
     * @param <T>      the return type of the lambda
     * @return The result of the execution of the lambda
     * @throws IllegalStateException         if there are operations in the current transaction
     * @throws ONeedRetryException           if the maximum number of retries is executed and all failed with an ONeedRetryException
     * @throws IllegalArgumentException      if nRetries is <= 0
     * @throws UnsupportedOperationException if this type of database does not support automatic commit/retry
     */
    public <T> T executeWithRetry(int nRetries, Function<ODatabaseSession, T> function)
            throws IllegalStateException, IllegalArgumentException, ONeedRetryException, UnsupportedOperationException {
        if (nRetries < 1) {
            throw new IllegalArgumentException("invalid number of retries: " + nRetries);
        }
        OTransaction tx = db.getTransaction();
        boolean txActive = tx.isActive();
        if (txActive) {
            if (tx.getEntryCount() > 0) {
                throw new IllegalStateException(
                        "executeWithRetry() cannot be used within a pending (dirty) transaction. Please commit or rollback before invoking it");
            }
        }
        if (!txActive) {
            begin();
        }

        T result = null;

        for (int i = 0; i < nRetries; i++) {
            try {
                result = function.apply((ODatabaseSession) this);
                commit();
                break;
            } catch (ONeedRetryException e) {
                if (i == nRetries - 1) {
                    throw e;
                }
                rollback();
                begin();
            } catch (Exception e) {
                throw OException.wrapException(new ODatabaseException("Error during tx retry"), e);
            }
        }

        if (txActive) {
            begin();
        }

        return result;
    }

    /**
     * Creates a new vertex class (a class that extends V).
     *
     * @param className the class name
     */
    public void createVertexClass(String className) throws OSchemaException {
        if (db.getClass(className) == null) {
            db.createVertexClass(className);
        }
    }

    /**
     * creates a new edge class (a class that extends E)
     *
     * @param className the class name
     */
    public void createEdgeClass(String className) throws OSchemaException {
        if (db.getClass(className) == null) {
            db.createEdgeClass(className);
        }
    }

    /**
     * Executes an SQL query. The result set has to be closed after usage
     * <br><br>
     * Sample usage:
     * <p>
     * <code>
     * OResultSet rs = db.query("SELECT FROM V where name = ?", "John");
     * while(rs.hasNext()){
     * OResult item = rs.next();
     * ...
     * }
     * rs.close();
     * </code>
     *
     * @param query the query string
     * @param args  query parameters (positional)
     * @return the query result set
     */
    public OResultSet query(String query, Object... args)
            throws OCommandSQLParsingException, OCommandExecutionException {
        return db.query(query, args);
    }

    /**
     * Executes an SQL query (idempotent). The result set has to be closed after usage
     * <br><br>
     * Sample usage:
     * <p>
     * <code>
     * Map&lt;String, Object&gt params = new HashMapMap&lt;&gt();
     * params.put("name", "John");
     * OResultSet rs = db.query("SELECT FROM V where name = :name", params);
     * while(rs.hasNext()){
     * OResult item = rs.next();
     * ...
     * }
     * rs.close();
     * </code>
     *
     * @param query the query string
     * @param args  query parameters (named)
     * @return
     */
    public OResultSet query(String query, Map args)
            throws OCommandSQLParsingException, OCommandExecutionException {
        return db.query(query, args);
    }

    /**
     * Execute a SQL script with given arguments.
     *
     * @param script A SQL script
     * @param args   Arguments
     * @throws OCommandExecutionException
     * @throws OCommandScriptException
     */
    public void command(String script, Object... args)
            throws OCommandExecutionException, OCommandScriptException {
        OResultSet rs = db.command(script, args);
        rs.close();
    }

    /**
     * Execute a SQL script with given arguments.
     *
     * @param script A SQL script
     * @param args   Arguments
     * @throws OCommandExecutionException
     * @throws OCommandScriptException
     */
    public void command(String script, Map<String, ?> args)
            throws OCommandExecutionException, OCommandScriptException {
        OResultSet rs = db.command(script, args);
        rs.close();
    }

    /**
     * Close the database session and OrientDB client.
     */
    public void close() {
        db.close();
        orient.close();
    }
}
