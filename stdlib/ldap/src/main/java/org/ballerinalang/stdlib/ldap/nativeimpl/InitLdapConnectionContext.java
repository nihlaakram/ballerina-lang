/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.ldap.nativeimpl;

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.jvm.BallerinaValues;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.stdlib.ldap.CommonLdapConfiguration;
import org.ballerinalang.stdlib.ldap.LdapConnectionContext;
import org.ballerinalang.stdlib.ldap.LdapConstants;
import org.ballerinalang.stdlib.ldap.SslContextTrustManager;
import org.ballerinalang.stdlib.ldap.util.LdapUtils;
import org.ballerinalang.stdlib.ldap.util.SslUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.net.ssl.SSLContext;


/**
 * Initializes LDAP connection context.
 *
 * @since 0.983.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "ldap",
        functionName = "initLdapConnectionContext", isPublic = true)
public class InitLdapConnectionContext extends BlockingNativeCallableUnit {

    @Override
    public void execute(Context context) {

    }

    public static MapValue<String, Object> initLdapConnectionContext(Strand strand, MapValue<?, ?> authProviderConfig,
                                                                     String instanceId) {
        CommonLdapConfiguration commonLdapConfiguration = new CommonLdapConfiguration();

        commonLdapConfiguration.setDomainName(authProviderConfig.getStringValue(LdapConstants.DOMAIN_NAME));
        commonLdapConfiguration.setConnectionURL(authProviderConfig.getStringValue(LdapConstants.CONNECTION_URL));
        commonLdapConfiguration.setConnectionName(authProviderConfig.getStringValue(LdapConstants.CONNECTION_NAME));
        commonLdapConfiguration.setConnectionPassword(
                authProviderConfig.getStringValue(LdapConstants.CONNECTION_PASSWORD));

        commonLdapConfiguration.setUserSearchBase(authProviderConfig.getStringValue(LdapConstants.USER_SEARCH_BASE));
        commonLdapConfiguration.setUserEntryObjectClass(
                authProviderConfig.getStringValue(LdapConstants.USER_ENTRY_OBJECT_CLASS));
        commonLdapConfiguration.setUserNameAttribute(
                authProviderConfig.getStringValue(LdapConstants.USER_NAME_ATTRIBUTE));
        commonLdapConfiguration.setUserNameSearchFilter(
                authProviderConfig.getStringValue(LdapConstants.USER_NAME_SEARCH_FILTER));
        commonLdapConfiguration.setUserNameListFilter(
                authProviderConfig.getStringValue(LdapConstants.USER_NAME_LIST_FILTER));

        commonLdapConfiguration.setGroupSearchBase(getAsStringList(authProviderConfig.
                getArrayValue(LdapConstants.GROUP_SEARCH_BASE).getStringArray()));
        commonLdapConfiguration.setGroupEntryObjectClass(
                authProviderConfig.getStringValue(LdapConstants.GROUP_ENTRY_OBJECT_CLASS));
        commonLdapConfiguration.setGroupNameAttribute(
                authProviderConfig.getStringValue(LdapConstants.GROUP_NAME_ATTRIBUTE));
        commonLdapConfiguration.setGroupNameSearchFilter(
                authProviderConfig.getStringValue(LdapConstants.GROUP_NAME_SEARCH_FILTER));
        commonLdapConfiguration.setGroupNameListFilter(
                authProviderConfig.getStringValue(LdapConstants.GROUP_NAME_LIST_FILTER));

        commonLdapConfiguration.setMembershipAttribute(
                authProviderConfig.getStringValue(LdapConstants.MEMBERSHIP_ATTRIBUTE));
        commonLdapConfiguration.setUserRolesCacheEnabled(
                authProviderConfig.getBooleanValue(LdapConstants.USER_ROLE_CACHE_ENABLE));
        commonLdapConfiguration.setConnectionPoolingEnabled(
                authProviderConfig.getBooleanValue(LdapConstants.CONNECTION_POOLING_ENABLED));
        commonLdapConfiguration.setLdapConnectionTimeout(
                authProviderConfig.getIntValue(LdapConstants.CONNECTION_TIME_OUT).intValue());
        commonLdapConfiguration.setReadTimeout(
                authProviderConfig.getIntValue(LdapConstants.READ_TIME_OUT).intValue());
        commonLdapConfiguration.setRetryAttempts(
                authProviderConfig.getIntValue(LdapConstants.RETRY_ATTEMPTS).intValue());

        MapValue<?, ?> sslConfig = authProviderConfig.getMapValue(LdapConstants.SECURE_AUTH_STORE_CONFIG);
        try {
            if (sslConfig != null) {
                setSslConfig(sslConfig, commonLdapConfiguration, instanceId);
                LdapUtils.setServiceName(instanceId);
            }
            LdapConnectionContext connectionSource = new LdapConnectionContext(commonLdapConfiguration);
            DirContext dirContext = connectionSource.getContext();

            MapValue<String, Object> ldapConnectionRecord = BallerinaValues.createRecordValue(
                    LdapConstants.LDAP_PACKAGE, LdapConstants.LDAP_CONNECTION);
            ldapConnectionRecord.addNativeData(LdapConstants.LDAP_CONFIGURATION, commonLdapConfiguration);
            ldapConnectionRecord.addNativeData(LdapConstants.LDAP_CONNECTION_SOURCE, connectionSource);
            ldapConnectionRecord.addNativeData(LdapConstants.LDAP_CONNECTION_CONTEXT, dirContext);
            ldapConnectionRecord.addNativeData(LdapConstants.ENDPOINT_INSTANCE_ID, instanceId);
            return ldapConnectionRecord;
        } catch (KeyStoreException | KeyManagementException | NoSuchAlgorithmException
                | CertificateException | NamingException | IOException e) {
            throw new BallerinaException(e.getMessage(), e);
        } finally {
            if (sslConfig != null) {
                LdapUtils.removeServiceName();
            }
        }
    }

    private static void setSslConfig(MapValue sslConfig, CommonLdapConfiguration commonLdapConfiguration,
                                     String instanceId)
            throws IOException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException,
                   CertificateException {
        MapValue<?, ?> trustStore = sslConfig.getMapValue(LdapConstants.AUTH_STORE_CONFIG_TRUST_STORE);
        String trustCerts = sslConfig.getStringValue(LdapConstants.AUTH_STORE_CONFIG_TRUST_CERTIFICATES);

        if (trustStore != null) {
            String trustStoreFilePath = trustStore.getStringValue(LdapConstants.FILE_PATH);
            String trustStorePassword = trustStore.getStringValue(LdapConstants.PASSWORD);

            if (trustStoreFilePath != null) {
                File trustStoreFile = new File(LdapUtils.substituteVariables(trustStoreFilePath));
                if (!trustStoreFile.exists()) {
                    throw new IllegalArgumentException("trustStore File " + trustStoreFilePath + " not found");
                }
                if (trustStorePassword == null) {
                    throw new IllegalArgumentException("trustStorePass is not defined for HTTPS scheme");
                }
                commonLdapConfiguration.setTrustStoreFile(trustStoreFile);
                commonLdapConfiguration.setTrustStorePass(trustStorePassword);
                SSLContext sslContext = SslUtils.createClientSslContext(trustStoreFilePath, trustStorePassword);
                SslContextTrustManager.getInstance().addSSLContext(instanceId, sslContext);
            }
        } else if (StringUtils.isNotBlank(trustCerts)) {
            commonLdapConfiguration.setClientTrustCertificates(trustCerts);
            SSLContext sslContext = SslUtils.getSslContextForCertificateFile(trustCerts);
            SslContextTrustManager.getInstance().addSSLContext(instanceId, sslContext);
        }
    }

    private static List<String> getAsStringList(Object[] values) {
        if (values == null) {
            return null;
        }
        List<String> valuesList = new ArrayList<>();
        for (Object val : values) {
            valuesList.add(val.toString().trim());
        }
        return !valuesList.isEmpty() ? valuesList : null;
    }
}
