// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/config;

# Configurations for managing gRPC client connection pool.
#
# + maxActiveConnections - Max active connections per route(host:port). Default value is -1 which indicates unlimited.
# + maxIdleConnections - Maximum number of idle connections allowed per pool.
# + waitTimeinMillis - Maximum amount of time, the client should wait for an idle connection before it sends an error when the pool is exhausted
# + maxActiveStreamsPerConnection - Maximum active streams per connection. This only applies to HTTP/2.
public type PoolConfiguration record {|
    int maxActiveConnections = config:getAsInt("b7a.http.pool.maxActiveConnections", -1);
    int maxIdleConnections = config:getAsInt("b7a.http.pool.maxIdleConnections", 1000);
    int waitTimeinMillis = config:getAsInt("b7a.http.pool.waitTimeinMillis", 60000);
    int maxActiveStreamsPerConnection = config:getAsInt("b7a.http.pool.maxActiveStreamsPerConnection", 50);
|};

//This is a hack to get the global map initialized, without involving locking.
type ConnectionManager object {
    private PoolConfiguration poolConfig = {};

    public function __init() {
        self.initGlobalPool(self.poolConfig);
    }
    function initGlobalPool(PoolConfiguration poolConfig) = external;

    public function getPoolConfiguration() returns PoolConfiguration {
        return self.poolConfig;
    }
};

ConnectionManager connectionManager = new;
PoolConfiguration globalGrpcClientConnPool = connectionManager.getPoolConfiguration();
