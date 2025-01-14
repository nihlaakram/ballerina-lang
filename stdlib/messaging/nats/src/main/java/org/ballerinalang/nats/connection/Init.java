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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.nats.connection;

import io.nats.client.Connection;
import io.nats.client.ErrorListener;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.util.exceptions.BallerinaConnectorException;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.Receiver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import static org.ballerinalang.nats.Constants.CONNECTED_CLIENTS;
import static org.ballerinalang.nats.Constants.CONNECTION_CONFIG_SECURE_SOCKET;
import static org.ballerinalang.nats.Constants.CONNECTION_KEYSTORE;
import static org.ballerinalang.nats.Constants.CONNECTION_PROTOCOL;
import static org.ballerinalang.nats.Constants.CONNECTION_TRUSTORE;
import static org.ballerinalang.nats.Constants.ERROR_SETTING_UP_SECURED_CONNECTION;
import static org.ballerinalang.nats.Constants.KEY_STORE_PASS;
import static org.ballerinalang.nats.Constants.KEY_STORE_PATH;
import static org.ballerinalang.nats.Constants.KEY_STORE_TYPE;
import static org.ballerinalang.nats.Constants.NATS_CONNECTION;
import static org.ballerinalang.nats.Constants.SERVICE_LIST;

/**
 * Establish a connection with NATS server.
 *
 * @since 0.995
 */
@BallerinaFunction(
        orgName = "ballerina",
        packageName = "nats",
        functionName = "init",
        receiver = @Receiver(type = TypeKind.OBJECT, structType = "Connection", structPackage = "ballerina/nats"),
        isPublic = true
)
public class Init {

    private static final String RECONNECT_WAIT = "reconnectWaitInSeconds";
    private static final String SERVER_URL_SEPARATOR = ",";
    private static final String CONNECTION_NAME = "connectionName";
    private static final String MAX_RECONNECT = "maxReconnect";
    private static final String CONNECTION_TIMEOUT = "connectionTimeoutInSeconds";
    private static final String PING_INTERVAL = "pingIntervalInMinutes";
    private static final String MAX_PINGS_OUT = "maxPingsOut";
    private static final String INBOX_PREFIX = "inboxPrefix";
    private static final String NO_ECHO = "noEcho";
    private static final String ENABLE_ERROR_LISTENER = "enableErrorListener";

    public static void init(Strand strand, ObjectValue connectionObject, String urlString,
                            MapValue connectionConfig) {
        Options.Builder opts = new Options.Builder();

        // Add server endpoint urls.
        String[] serverUrls;
        if (urlString != null && urlString.contains(SERVER_URL_SEPARATOR)) {
            serverUrls = urlString.split(SERVER_URL_SEPARATOR);
        } else {
            serverUrls = new String[]{urlString};
        }
        opts.servers(serverUrls);

        // Add connection name.
        opts.connectionName(connectionConfig.getStringValue(CONNECTION_NAME));

        // Add max reconnect.
        opts.maxReconnects(Math.toIntExact(connectionConfig.getIntValue(MAX_RECONNECT)));

        // Add reconnect wait.
        opts.reconnectWait(Duration.ofSeconds(connectionConfig.getIntValue(RECONNECT_WAIT)));


        // Add connection timeout.
        opts.connectionTimeout(Duration.ofSeconds(connectionConfig.getIntValue(CONNECTION_TIMEOUT)));

        // Add ping interval.
        opts.pingInterval(Duration.ofMinutes(connectionConfig.getIntValue(PING_INTERVAL)));

        // Add max ping out.
        opts.maxPingsOut(Math.toIntExact(connectionConfig.getIntValue(MAX_PINGS_OUT)));

        // Add inbox prefix.
        opts.inboxPrefix(connectionConfig.getStringValue(INBOX_PREFIX));

        List<ObjectValue> serviceList = Collections.synchronizedList(new ArrayList<>());
        // Add NATS connection listener.
        opts.connectionListener(new DefaultConnectionListener(strand.scheduler, serviceList));

        // Add NATS error listener.
        if (connectionConfig.getBooleanValue(ENABLE_ERROR_LISTENER)) {
            ErrorListener errorListener = new DefaultErrorListener(strand.scheduler, serviceList);
            opts.errorListener(errorListener);
        }

        // Add noEcho.
        if (connectionConfig.getBooleanValue(NO_ECHO)) {
            opts.noEcho();
        }

        MapValue secureSocket = connectionConfig.getMapValue(CONNECTION_CONFIG_SECURE_SOCKET);
        if (secureSocket != null) {
            SSLContext sslContext = getSSLContext(secureSocket);
            opts.sslContext(sslContext);
        }

        try {
            Connection natsConnection = Nats.connect(opts.build());
            connectionObject.addNativeData(NATS_CONNECTION, natsConnection);
            connectionObject.addNativeData(CONNECTED_CLIENTS, new AtomicInteger(0));
            connectionObject.addNativeData(SERVICE_LIST, serviceList);

        } catch (IOException | InterruptedException e) {
            String errorMsg = "Error while setting up a connection. " +
                    (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            throw new BallerinaConnectorException(errorMsg);
        }
    }

    /**
     * Creates and retrieves the SSLContext from socket configuration.
     *
     * @param secureSocket secureSocket record.
     * @return Initialized SSLContext.
     */
    private static SSLContext getSSLContext(MapValue secureSocket) {
        try {
            MapValue cryptoKeyStore = secureSocket.getMapValue(CONNECTION_KEYSTORE);
            KeyManagerFactory keyManagerFactory = null;
            if (cryptoKeyStore != null) {
                char[] keyPassphrase = cryptoKeyStore.getStringValue(KEY_STORE_PASS).toCharArray();
                String keyFilePath = cryptoKeyStore.getStringValue(KEY_STORE_PATH);
                KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE);
                if (keyFilePath != null) {
                    try (FileInputStream keyFileInputStream = new FileInputStream(keyFilePath)) {
                        keyStore.load(keyFileInputStream, keyPassphrase);
                    }
                } else {
                    throw new BallerinaConnectorException(ERROR_SETTING_UP_SECURED_CONNECTION +
                            "Keystore path doesn't exist.");
                }
                keyManagerFactory =
                        KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, keyPassphrase);
            }

            MapValue cryptoTrustStore = secureSocket.getMapValue(CONNECTION_TRUSTORE);
            TrustManagerFactory trustManagerFactory = null;
            if (cryptoTrustStore != null) {
                KeyStore trustStore = KeyStore.getInstance(KEY_STORE_TYPE);
                char[] trustPassphrase = cryptoTrustStore.getStringValue(KEY_STORE_PASS).toCharArray();
                String trustFilePath = cryptoTrustStore.getStringValue(KEY_STORE_PATH);
                if (trustFilePath != null) {
                    try (FileInputStream trustFileInputStream = new FileInputStream(trustFilePath)) {
                        trustStore.load(trustFileInputStream, trustPassphrase);
                    }
                } else {
                    throw new BallerinaConnectorException(ERROR_SETTING_UP_SECURED_CONNECTION +
                            "Truststore path doesn't exist.");
                }
                trustManagerFactory =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustStore);
            }

            String tlsVersion = secureSocket.getStringValue(CONNECTION_PROTOCOL);
            SSLContext sslContext = SSLContext.getInstance(tlsVersion);
            sslContext.init(keyManagerFactory != null ? keyManagerFactory.getKeyManagers() : null,
                    trustManagerFactory != null ? trustManagerFactory.getTrustManagers() : null, null);
            return sslContext;
        } catch (FileNotFoundException e) {
            throw new BallerinaConnectorException(ERROR_SETTING_UP_SECURED_CONNECTION +
                    "File not found error, " + e.getMessage());
        } catch (CertificateException e) {
            throw new BallerinaConnectorException(ERROR_SETTING_UP_SECURED_CONNECTION +
                    "Certificate error, " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            throw new BallerinaConnectorException(ERROR_SETTING_UP_SECURED_CONNECTION +
                    "Algorithm error, " + e.getMessage());
        } catch (IOException e) {
            throw new BallerinaConnectorException(ERROR_SETTING_UP_SECURED_CONNECTION +
                    "IO error, " + e.getMessage());
        } catch (KeyStoreException e) {
            throw new BallerinaConnectorException(ERROR_SETTING_UP_SECURED_CONNECTION +
                    "Keystore error, " + e.getMessage());
        } catch (UnrecoverableKeyException e) {
            throw new BallerinaConnectorException(ERROR_SETTING_UP_SECURED_CONNECTION +
                    "The key in the keystore cannot be recovered.");
        } catch (KeyManagementException e) {
            throw new BallerinaConnectorException(ERROR_SETTING_UP_SECURED_CONNECTION +
                    "Key management error, " + e.getMessage());
        }
    }
}
