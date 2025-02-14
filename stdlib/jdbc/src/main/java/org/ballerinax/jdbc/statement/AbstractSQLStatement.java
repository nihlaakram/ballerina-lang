/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinax.jdbc.statement;

import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.ColumnDefinition;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.TableResourceManager;
import org.ballerinalang.jvm.TypeChecker;
import org.ballerinalang.jvm.transactions.BallerinaTransactionContext;
import org.ballerinalang.jvm.transactions.TransactionLocalContext;
import org.ballerinalang.jvm.transactions.TransactionResourceManager;
import org.ballerinalang.jvm.transactions.TransactionUtils;
import org.ballerinalang.jvm.types.BArrayType;
import org.ballerinalang.jvm.types.BPackage;
import org.ballerinalang.jvm.types.BRecordType;
import org.ballerinalang.jvm.types.BStructureType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.types.TypeTags;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.jvm.values.TableValue;
import org.ballerinax.jdbc.Constants;
import org.ballerinax.jdbc.datasource.SQLDatasource;
import org.ballerinax.jdbc.exceptions.ApplicationException;
import org.ballerinax.jdbc.exceptions.DatabaseException;
import org.ballerinax.jdbc.exceptions.ErrorGenerator;
import org.ballerinax.jdbc.table.BCursorTable;
import org.ballerinax.jdbc.table.SQLDataIterator;
import org.ballerinax.jdbc.transaction.SQLTransactionContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import static org.ballerinax.jdbc.Constants.PARAMETER_DIRECTION_FIELD;
import static org.ballerinax.jdbc.Constants.PARAMETER_SQL_TYPE_FIELD;
import static org.ballerinax.jdbc.Constants.PARAMETER_VALUE_FIELD;

/**
 * Represent an abstract SQL statement. Contains the common functionality for any SQL statement.
 *
 * @since 1.0.0
 */
public abstract class AbstractSQLStatement implements SQLStatement {
    Calendar utcCalendar = Calendar.getInstance(TimeZone.getTimeZone(Constants.TIMEZONE_UTC));
    Strand strand;

    AbstractSQLStatement(Strand strand) {
        this.strand = strand;
    }

    ArrayValue constructParameters(ArrayValue parameters) throws ApplicationException {
        ArrayValue parametersNew = new ArrayValue();
        int paramCount = parameters.size();
        for (int i = 0; i < paramCount; ++i) {
            Object value = parameters.getValue(i);
            MapValue<String, Object> paramRecord;
            BType type = TypeChecker.getType(value);
            if (type.getTag() == TypeTags.OBJECT_TYPE_TAG
                    || type.getTag() == TypeTags.RECORD_TYPE_TAG) {
                paramRecord = (MapValue<String, Object>) value;
            } else {
                paramRecord = getSQLParameter();
                paramRecord.put(PARAMETER_VALUE_FIELD, value);
                paramRecord.put(PARAMETER_DIRECTION_FIELD, Constants.QueryParamDirection.DIR_IN);
                paramRecord.put(PARAMETER_SQL_TYPE_FIELD, getSQLType(value));
            }
            parametersNew.add(i, paramRecord);
        }
        return parametersNew;
    }

    /**
     * If there are any arrays of parameter for types other than sql array, the given query is expanded by adding "?" s
     * to match with the array size.
     *
     * @param query The query to be processed
     * @param parameters Array of parameters belonging to the query
     *
     * @return The string with "?" place holders for parameters
     */
     String createProcessedQueryString(String query, ArrayValue parameters) {
        String currentQuery = query;
        if (parameters != null) {
            int start = 0;
            Object[] vals;
            int count;
            int paramCount = parameters.size();
            for (int i = 0; i < paramCount; i++) {
                // types.bal Parameter
                MapValue<String, Object> paramValue = (MapValue<String, Object>) parameters.getRefValue(i);
                if (paramValue != null) {
                    String sqlType = StatementProcessUtils.getSQLType(paramValue);
                    Object value = paramValue.get(PARAMETER_VALUE_FIELD);
                    BType type = TypeChecker.getType(value);
                    if (value != null && (type.getTag() == TypeTags.ARRAY_TAG
                            && ((BArrayType) type).getElementType().getTag()
                            != TypeTags.BYTE_TAG)
                            && !Constants.SQLDataTypes.ARRAY.equalsIgnoreCase(sqlType)) {
                        count = ((ArrayValue) value).size();
                    } else {
                        count = 1;
                    }
                    vals = expandQuery(start, count, currentQuery);
                    start = (Integer) vals[0];
                    currentQuery = (String) vals[1];
                }
            }
        }
        return currentQuery;
    }

    TableValue constructTable(TableResourceManager rm, ResultSet rs, BStructureType structType,
            List<ColumnDefinition> columnDefinitions, String databaseProductName) {
        BStructureType tableConstraint = structType;
        if (structType == null) {
            tableConstraint = new BRecordType("$table$anon$constraint$",
                    new BPackage(Names.BUILTIN_ORG.getValue(), Names.LANG.value + Names.DOT.value + Names.ANNOTATIONS,
                            Names.DEFAULT_VERSION.getValue()), 0, false);
            ((BRecordType) tableConstraint).restFieldType = BTypes.typeAnydata;
        }
        return new BCursorTable(
                new SQLDataIterator(rm, rs, utcCalendar, columnDefinitions, structType, databaseProductName),
                tableConstraint);
    }

     List<ColumnDefinition> getColumnDefinitions(ResultSet rs) throws SQLException {
        List<ColumnDefinition> columnDefs = new ArrayList<>();
        Set<String> columnNames = new HashSet<>();
        ResultSetMetaData rsMetaData = rs.getMetaData();
        int cols = rsMetaData.getColumnCount();
        for (int i = 1; i <= cols; i++) {
            String colName = rsMetaData.getColumnLabel(i);
            if (columnNames.contains(colName)) {
                String tableName = rsMetaData.getTableName(i).toUpperCase(Locale.getDefault());
                colName = tableName + "." + colName;
            }
            int colType = rsMetaData.getColumnType(i);
            int mappedType = getColumnType(colType);
            columnDefs.add(new SQLDataIterator.SQLColumnDefinition(colName, mappedType, colType));
            columnNames.add(colName);
        }
        return columnDefs;
    }

    /**
     * This method will return equal ballerina data type for SQL type.
     *
     * @param sqlType SQL type in column
     * @return TypeKind that represent respective ballerina type.
     */
    private int getColumnType(int sqlType) {
        switch (sqlType) {
            case Types.ARRAY:
                return TypeTags.ARRAY_TAG;
            case Types.CHAR:
            case Types.VARCHAR:
            case Types.LONGVARCHAR:
            case Types.NCHAR:
            case Types.NVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.CLOB:
            case Types.NCLOB:
            case Types.DATE:
            case Types.TIME:
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
            case Types.TIME_WITH_TIMEZONE:
            case Types.ROWID:
                return TypeTags.STRING_TAG;
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                return TypeTags.INT_TAG;
            case Types.BIT:
            case Types.BOOLEAN:
                return TypeTags.BOOLEAN_TAG;
            case Types.NUMERIC:
            case Types.DECIMAL:
                return TypeTags.DECIMAL_TAG;
            case Types.REAL:
            case Types.FLOAT:
            case Types.DOUBLE:
                return TypeTags.FLOAT_TAG;
            case Types.BLOB:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return TypeTags.BYTE_ARRAY_TAG;
            case Types.STRUCT:
                return TypeTags.RECORD_TYPE_TAG;
            default:
                return TypeTags.NONE_TAG;
        }
    }

    private String getSQLType(Object value) throws ApplicationException {
        BType type = TypeChecker.getType(value);
        int tag = type.getTag();
        switch (tag) {
        case TypeTags.INT_TAG:
            return Constants.SQLDataTypes.BIGINT;
        case TypeTags.STRING_TAG:
            return Constants.SQLDataTypes.VARCHAR;
        case TypeTags.FLOAT_TAG:
            return Constants.SQLDataTypes.DOUBLE;
        case TypeTags.BOOLEAN_TAG:
            return Constants.SQLDataTypes.BOOLEAN;
        case TypeTags.DECIMAL_TAG:
            return Constants.SQLDataTypes.DECIMAL;
        case TypeTags.ARRAY_TAG:
            if (((BArrayType) type).getElementType().getTag() == TypeTags.BYTE_TAG) {
                return Constants.SQLDataTypes.BINARY;
            } else {
                throw new ApplicationException("Array data type as direct value is supported only " +
                        "with byte type elements, use jdbc:Parameter " + type.getName());
            }
        default:
            throw new ApplicationException(
                    "unsupported data type as direct value for sql operation, use jdbc:Parameter: " + type.getName());
        }
    }

    private MapValue<String, Object> getSQLParameter() {
        return BallerinaValues.createRecordValue(Constants.JDBC_PACKAGE_PATH, Constants.SQL_PARAMETER);
    }

    /**
     * This will close database connection and statement.
     *
     * @param stmt SQL statement
     * @param conn SQL connection
     * @param connectionClosable Whether the connection is closable or not. If the connection is not closable this
     * method will not release the connection. Therefore to avoid connection leaks it should have been taken care
     * of externally.
     */
    void cleanupResources(Statement stmt, Connection conn, boolean connectionClosable) {
        try {
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
            }
            if (conn != null && !conn.isClosed() && connectionClosable) {
                conn.close();
            }
        } catch (SQLException e) {
            throw ErrorGenerator.getSQLDatabaseError(e, "error in cleaning sql resources: ");
        }
    }

    /**
     * This will close database connection, statement and the resultset.
     *
     * @param rs   SQL resultset
     * @param stmt SQL statement
     * @param conn SQL connection
     * @param connectionClosable Whether the connection is closable or not. If the connection is not closable this
     * method will not release the connection. Therefore to avoid connection leaks it should have been taken care
     * of externally.
     */
    void cleanupResources(ResultSet rs, Statement stmt, Connection conn, boolean connectionClosable) {
        try {
            if (rs != null && !rs.isClosed()) {
                rs.close();
            }
            cleanupResources(stmt, conn, connectionClosable);
        } catch (SQLException e) {
            throw ErrorGenerator.getSQLDatabaseError(e, "error in cleaning sql resources: ");

        }
    }

    void handleErrorOnTransaction(Strand strand) {
        TransactionLocalContext transactionLocalContext = strand.getLocalTransactionContext();
        if (transactionLocalContext == null) {
            return;
        }
        notifyTxMarkForAbort(strand, transactionLocalContext);
    }

    Connection getDatabaseConnection(Strand strand, ObjectValue client, SQLDatasource datasource,
                                     boolean isSelectQuery) throws DatabaseException {
        Connection conn;
        try {
            boolean isInTransaction = strand.isInTransaction();
            // Here when isSelectQuery condition is true i.e. in case of a select operation, we allow
            // it to use a normal database connection. This is because,
            // 1. In mysql (and possibly some other databases) another operation cannot be performed over a connection
            // which has an open result set on top of it
            // 2. But inside a transaction we use the same connection to perform all the db operation, so unless the
            // result set is fully iterated, it won't be possible to perform rest of the operations inside the 
            // transaction
            // 3. Therefore, we allow select operations to be performed on separate db connections inside transactions
            // (XA or general transactions)
            // 4. However for call operations, despite of the fact that they could output resultsets
            // (as OUT params or return values) we do not use a separate connection, because,
            // call operations can contain UPDATE actions as well inside the procedure which may require to happen 
            // in the
            // same scope as any other individual UPDATE actions
            if (!isInTransaction || isSelectQuery) {
                conn = datasource.getSQLConnection();
                return conn;
            } else {
                //This is when there is an infected transaction block. But this is not participated to the transaction
                //since the action call is outside of the transaction block.
                if (!strand.getLocalTransactionContext().hasTransactionBlock()) {
                    conn = datasource.getSQLConnection();
                    return conn;
                }
            }
            String connectorId = retrieveConnectorId(client);
            boolean isXAConnection = datasource.isXAConnection();
            TransactionLocalContext transactionLocalContext = strand.getLocalTransactionContext();
            String globalTxId = transactionLocalContext.getGlobalTransactionId();
            String currentTxBlockId = transactionLocalContext.getCurrentTransactionBlockId();
            BallerinaTransactionContext txContext = transactionLocalContext.getTransactionContext(connectorId);
            if (txContext == null) {
                if (isXAConnection) {
                    XAConnection xaConn = datasource.getXADataSource().getXAConnection();
                    XAResource xaResource = xaConn.getXAResource();
                    TransactionResourceManager.getInstance()
                                              .beginXATransaction(globalTxId, currentTxBlockId, xaResource);
                    conn = xaConn.getConnection();
                    txContext = new SQLTransactionContext(conn, xaResource);
                } else {
                    conn = datasource.getSQLConnection();
                    conn.setAutoCommit(false);
                    txContext = new SQLTransactionContext(conn);
                }
                transactionLocalContext.registerTransactionContext(connectorId, txContext);
                TransactionResourceManager.getInstance().register(globalTxId, currentTxBlockId, txContext);
            } else {
                conn = ((SQLTransactionContext) txContext).getConnection();
            }
        } catch (SQLException e) {
            throw new DatabaseException("error in get connection: " + Constants.CONNECTOR_NAME + ": ", e);
        }
        return conn;
    }

    private String retrieveConnectorId(ObjectValue bConnector) {
        return (String) bConnector.getNativeData(Constants.CONNECTOR_ID_KEY);
    }

    private void notifyTxMarkForAbort(Strand strand, TransactionLocalContext transactionLocalContext) {
        String globalTransactionId = transactionLocalContext.getGlobalTransactionId();
        String transactionBlockId = transactionLocalContext.getCurrentTransactionBlockId();

        transactionLocalContext.markFailure();
        TransactionUtils.notifyTransactionAbort(strand, globalTransactionId, transactionBlockId);
    }

    /**
     * Search for the first occurrence of "?" from the given starting point and replace it with given number of "?"'s.
     */
    private Object[] expandQuery(int start, int count, String query) {
        StringBuilder result = new StringBuilder();
        int n = query.length();
        boolean doubleQuoteExists = false;
        boolean singleQuoteExists = false;
        int end = n;
        for (int i = start; i < n; i++) {
            if (query.charAt(i) == '\'') {
                singleQuoteExists = !singleQuoteExists;
            } else if (query.charAt(i) == '\"') {
                doubleQuoteExists = !doubleQuoteExists;
            } else if (query.charAt(i) == '?' && !(doubleQuoteExists || singleQuoteExists)) {
                result.append(query, 0, i);
                result.append(generateQuestionMarks(count));
                end = result.length() + 1;
                if (i + 1 < n) {
                    result.append(query.substring(i + 1));
                }
                break;
            }
        }
        return new Object[] { end, result.toString() };
    }

    private String generateQuestionMarks(int n) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            builder.append(Constants.QUESTION_MARK);
            if (i + 1 < n) {
                builder.append(",");
            }
        }
        return builder.toString();
    }
}
