// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/auth;
import ballerina/encoding;

function testCreateInboundBasicAuthProvider() returns auth:InboundBasicAuthProvider {
    auth:InboundBasicAuthProvider basicAuthProvider = new(());
    return basicAuthProvider;
}

function testAuthenticationOfNonExistingUser() returns boolean|error {
    string usernameAndPassword = "amila:abc";
    return authenticate(usernameAndPassword);
}

function testAuthenticationOfNonExistingPassword() returns boolean|error {
    string usernameAndPassword = "isuru:xxy";
    return authenticate(usernameAndPassword);
}

function testAuthentication() returns boolean|error {
    string usernameAndPassword = "isuru:xxx";
    return authenticate(usernameAndPassword);
}

function testAuthenticationWithEmptyUsername() returns boolean|error {
    string usernameAndPassword = ":xxx";
    return authenticate(usernameAndPassword);
}

function testAuthenticationWithEmptyPassword() returns boolean|error {
    auth:InboundBasicAuthProvider basicAuthProvider = new(());
    string usernameAndPassword = "isuru:";
    return authenticate(usernameAndPassword);
}

function testAuthenticationWithEmptyPasswordAndInvalidUsername() returns boolean|error {
    auth:InboundBasicAuthProvider basicAuthProvider = new(());
    string usernameAndPassword = "invalid:";
    return authenticate(usernameAndPassword);
}

function testAuthenticationWithEmptyUsernameAndEmptyPassword() returns boolean|error {
    string usernameAndPassword = ":";
    return authenticate(usernameAndPassword);
}

function testAuthenticationSha256() returns boolean|error {
    string usernameAndPassword = "hashedSha256:xxx";
    return authenticate(usernameAndPassword);
}

function testAuthenticationSha384() returns boolean|error {
    string usernameAndPassword = "hashedSha384:xxx";
    return authenticate(usernameAndPassword);
}

function testAuthenticationSha512() returns boolean|error {
    string usernameAndPassword = "hashedSha512:xxx";
    return authenticate(usernameAndPassword);
}

function testAuthenticationPlain() returns boolean|error {
    string usernameAndPassword = "plain:plainpassword";
    return authenticate(usernameAndPassword);
}

function testAuthenticationSha512Negative() returns boolean|error {
    string usernameAndPassword = "hashedSha512:xxx ";
    return authenticate(usernameAndPassword);
}

function testAuthenticationPlainNegative() returns boolean|error {
    string usernameAndPassword = "plain:plainpassword ";
    return authenticate(usernameAndPassword);
}

function authenticate(string usernameAndPassword) returns boolean|error {
    auth:InboundBasicAuthProvider basicAuthProvider = new(());
    string credential = encoding:encodeBase64(usernameAndPassword.toBytes());
    return basicAuthProvider.authenticate(credential);
}
