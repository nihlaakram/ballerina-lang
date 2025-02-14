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

# Representation of a HTTP Request Filter. This filter will be applied before the request is dispatched to the relevant resource. Any Filter implementation should be structurally similar to the Filter object.

public type Filter abstract object {
    # Request filter function. If a false is returned the response should have been sent from this function as it will
    # not be dispatched to the next filter or the resource.
    #
    # + caller - The http caller
    # + request - An inbound HTTP request message
    # + context - A filter context
    # + return - True if the filter succeeds
    public function filterRequest(Caller caller, Request request, FilterContext context) returns boolean;

    # Response filter function. If a false is returned a 500 Internal Server Error would be sent to the client.
    #
    # + response - An outbound HTTP response message
    # + context - A filter context
    # + return - True if the filter succeeds
    public function filterResponse(Response response, FilterContext context) returns boolean;
};

// TODO: validate
# Representation of request filter Context.
#
# + serviceRef - The service
# + serviceName - Name of the service
# + resourceName - Name of the resource
# + attributes - Attributes to share between filters
public type FilterContext object {

    public service serviceRef;
    public string serviceName = "";
    public string resourceName = "";
    public map<any> attributes = {};

    public function __init(service serviceRef, string serviceName, string resourceName) {
        self.serviceRef = serviceRef;
        self.serviceName = serviceName;
        self.resourceName = resourceName;
    }
};
