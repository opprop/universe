Continuous integration status of master:
![CFI status](https://github.com/opprop/universe/workflows/CI/badge.svg)

# Generic Universe Types checker and inference

This project implements the type checker and whole-program inference described
in the ECOOP 2011 paper [Tunable Static Inference for Generic Universe
Types](https://ece.uwaterloo.ca/~wdietl/publications/pubs/DietlErnstMueller11-abstract.html).

Comments, pull requests, and issues are always welcome!


## Setup

Follow these steps to clone, build, and test this project and all dependencies:

````
git clone https://github.com/opprop/universe
cd universe
./.ci-build.sh
````

## Supported Options

Linter options:
* `checkOaM` Enables encapsulation checks (Owner-as-Modifier). Without this option, only topology checks are performed,
  so make sure to enable set this option if it should be checked that `@Any` references are not used for writing.

Linter options can be passed via `-Alint=<option>`. Note that everything except the last option given in that way is
ignored (see https://checkerframework.org/manual/#alint for more details)!

## Type checking example

````
$ ./scripts/check.sh tests/typecheck/topol/SimpleNew.java
tests/typecheck/topol/SimpleNew.java:14: error: [uts.new.ownership] Object creation needs a @Peer or @Rep modifier for non-implicitly immutable types and @Bottom for implicitly immutable types!
    @Any Object a = new @Any Object();
                    ^
1 error
````


## Type inference example

````
$ ./scripts/infer.sh tests/inference/Person.java
````


## Contact

Please address your questions and comments to
[Werner Dietl](https://ece.uwaterloo.ca/~wdietl/contact.html).
