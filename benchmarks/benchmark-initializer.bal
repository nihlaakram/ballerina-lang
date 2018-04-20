import ballerina/io;
import ballerina/file;

function main(string... args) {

    if (lengthof args < 3) {
        io:println("ERROR: Please specify the number of warm-up iterations and benchmark iterations.");
        return;
    }

    var warmupIterations = check <int>args[0];
    var benchmarkIterations = check <int>args[1];
    string resultFileName = untaintedReturn(<string>args[2]);

    prepare(warmupIterations, benchmarkIterations, resultFileName);
}

function prepare(int warmupIterations, int benchmarkIterations, string resultsFileName) {

    // create results folder
    string resultsFolderName = "results";
    file:Path dirPath = new(resultsFolderName);
    if (!file:exists(dirPath)){
        var dir = file:createDirectory(dirPath);
    }

    // write ReadMe
    string fileReadMeLocation = resultsFolderName + "/" + resultsFileName + ".txt";
    file:Path reameMePath = new(fileReadMeLocation);

    if (file:exists(reameMePath)){
        var result = file:delete(reameMePath);
    }
    io:ByteChannel channelR = io:openFile(fileReadMeLocation, "w");
    io:CharacterChannel charChannelR = new io:CharacterChannel(channelR, "UTF-8");
    int resultWriteR = check charChannelR.write("This test carried out with warmupIterations of " +
            warmupIterations + " and benchmarkIterations of " + benchmarkIterations + ".", 0);

    // write results file
    string resultsFileLocation = resultsFolderName + "/" + resultsFileName + ".csv";
    file:Path resultsFile = new(resultsFileLocation);
    if (file:exists(resultsFile)) {
        var result = file:delete(resultsFile);
    }
    io:ByteChannel channel = io:openFile(resultsFileLocation, "w");
    io:CharacterChannel charChannel = new io:CharacterChannel(channel, "UTF-8");
    int resultWrite = check charChannel.write("Function_Name,Total Time (ms),Average Latency (ns),Throughput (operations/second) ", 0);
    var result = channel.close();
}

public function untaintedReturn(string input) returns @untainted string {
    return input;
}

