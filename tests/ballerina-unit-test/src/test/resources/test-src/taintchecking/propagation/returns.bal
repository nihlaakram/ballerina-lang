function mapParam(map<string> m) returns int {
    m["key"] = taintThis();
    return 0;
}

function taintThis() returns @tainted string {
    return "val";
}

public function main (string... args) {
    secureFunction(typeReturn("static"), typeReturn("static"));
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

public function typeReturn (string input) returns (string) {
    return input;
}
