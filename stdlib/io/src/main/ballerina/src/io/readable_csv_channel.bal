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

# Represents a ReadableCSVChannel which could be used to read records from CSV file.
public type ReadableCSVChannel object {
    private ReadableTextRecordChannel? dc;

    # Constructs a CSV channel from a CharacterChannel to read/write CSV records.
    #
    # + byteChannel - The CharacterChannel, which will represent the content in the CSV file
    # + fs - Field separator, which will separate between the records in the CSV file
    # + nHeaders - Number of headers, which should be skipped prior to reading records
    public function __init(ReadableCharacterChannel byteChannel, Separator fs = ",", int nHeaders = 0) {
        if (fs == TAB) {
            self.dc = new ReadableTextRecordChannel(byteChannel, fmt = "TDF");
        } else if (fs == COLON) {
            self.dc = new ReadableTextRecordChannel(byteChannel, FS_COLON, CSV_RECORD_SEPARATOR);
        } else {
            self.dc = new ReadableTextRecordChannel(byteChannel, fmt = "CSV");
        }
        self.skipHeaders(nHeaders);
    }

    # Skips the given number of headers.
    #
    # + nHeaders - Number of headers which should be skipped
    function skipHeaders(int nHeaders) {
        int count = MINIMUM_HEADER_COUNT;
        while (count < nHeaders) {
            var result = self.getNext();
            count = count + 1;
        }
    }

    # Indicates whether there's another record which could be read.
    #
    # + return - True if there's a record
    public function hasNext() returns boolean {
        var recordChannel = self.dc;
        if (recordChannel is ReadableTextRecordChannel) {
            return recordChannel.hasNext();
        } else {
            GenericError e = error(GENERIC_ERROR, message = "Channel not initialized");
            panic e;
        }
    }

    # Gets the next record from the CSV file.
    #
    # + return - List of fields in the CSV or `Error` if any error occurred
    public function getNext() returns @tainted string[]|Error? {
        if(self.dc is ReadableTextRecordChannel){
            var result = <ReadableTextRecordChannel> self.dc;
            return result.getNext();
        }
        return ();
    }

    # Closes a given CSVChannel.
    #
    # + return - Returns `Error` if any error occurred
    public function close() returns Error? {
        if(self.dc is ReadableTextRecordChannel){
            var result = <ReadableTextRecordChannel> self.dc;
            return result.close();
        }
        return ();
    }

    # Returns a table which corresponds to the CSV records.
    #
    # + structType - The object the CSV records should be deserialized
    # + return - Table which represents CSV records or `Error` if any error occurred
    public function getTable(typedesc<record {}> structType) returns @tainted table<record {}>|Error = external;
};
