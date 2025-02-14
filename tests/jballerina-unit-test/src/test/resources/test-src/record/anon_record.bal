
function testAnonStructAsFuncParam() returns int {
    return testAnonStructFunc(10, {k:14, s:"sameera"});
}

function testAnonStructFunc(int i, record {int k = 10; string s;} anonSt) returns int {
    return anonSt.k + i;
}


function testAnonStructAsLocalVar() returns int {
    record {int k = 11; string s = "";} anonSt = {};

    return anonSt.k;
}


record {string fname = ""; string lname = ""; int age = 0;} person = {};

function testAnonStructAsPkgVar() returns string {

    person = {fname:"sameera", lname:"jaya"};
    person.lname = person.lname + "soma";
    person.age = 100;
    return person.fname + ":" + person.lname + ":" + person.age.toString();
}

type employee record {
    string fname = "";
    string lname = "";
    int age = 0;
    record { string line01 = "";
             string line02 = "";
             string city = "";
             string state = "";
             string zipcode = "";
    } address;

    record {
        string month = "JAN";
        string day = "01";
        string year = "1970";
    } dateOfBirth;
};

function testAnonStructAsStructField() returns string {

    employee e = {fname:"sam", lname:"json", age:100,
                     address:{line01:"12 Gemba St APT 134", city:"Los Altos", state:"CA", zipcode:"95123"},
                    dateOfBirth:{}};
    return e.dateOfBirth.month + ":" + e.address.line01 + ":" + e.address["state"] + ":" + e.fname;
}

function testRestField() returns person {
    person p = {fname:"John", lname:"Doe", age:20};
    p["location"] = "Colombo";
    p["height"] = 5.5;
    return p;
}

record {| string kind = ""; string name = ""; int...; |} animal = {};

function testAnonRecWithExplicitRestField() returns animal {
    animal a = {kind:"Cat", name:"Miaw"};
    a["legs"] = 4;
    return a;
}
