thermometer
===========

```
A micro-test framework for scalding pipes to make sure you don't get burnt
```

The `thermometer` library has a few goals:
 - Be explicit in expected outcomes, whilst not being concerned with irrelevant details.
 - Provide exceptional feedback in the face of failure:
   - Good error messages.
   - Clear mapping into actual files on disk where appropriate.
 - To allow testing of end-to-end pipelines which is impossible with `JobTest`.
 - To just work (no race-conditions, data to clean-up etc...), and work fast.


Thermometer tests can be declared in two ways, as `facts` or as traditional specs2
checks. The `facts` api should be preferred, it generally provides better contextual
error messages and composition for lower effort.


getting started
---------------

Import everything:

```
import com.cba.omnia.thermometer.core._, Thermometer._
```

Then create a spec that extends `ThermometerSpec`. This sets up appropriate scalding,
cascading and hadoop related things as well as ensuring that specs2 is run in a
way that won't break hadoop.


thermometer facts
-----------------

Facts can be asserted on cascading `Pipe` objects or scalding `TypedPipe` objects.

To verify some pipeline, you add a withFacts call. For example:

```
  def pipeline =
    ThermometerTestSource(List("hello", "world"))
      .map(c => (c, "really" + c + "!"))
      .write(TypedPsv[(String, String)]("output"))
      .withFacts(
        "cars" </> "_ERROR"      ==> missing
      , "cars" </> "_SUCCESS"    ==> exists
      , "cars" </> "part-00000"  ==> (exists, records(data.size))
      )
```

Breaking this down, `withFacts` takes a sequence of `Fact`s, these
can be construted in a number of ways, the most supported form are `PathFact`s,
which are built using the `==>` operation added to hdfs `Path`s and `String`s.
The right hand side of `==>` specifies a sequences of facts that should hold
true given the specified path.


thermometer expectations
------------------------

Thermometer expectations allow you to fall back to specs2, this may be because
of missing functionality from the facts api, or for optimisation of special
cases.

To verify some pipeline, you add a withExpectations call. For example:

```
  def pipeline =
    ThermometerTestSource(List("hello", "world"))
      .map(c => (c, "really" + c + "!"))
      .write(TypedPsv[(String, String)]("output"))
      .withExpectations(context => {
         context.exists("output" </> "_SUCCESS") must beTrue
         context.lines("output" </> "part-*").toSet must_== Set(
           "hello" -> "really hello!",
           "world" -> "really world!"
         )
      })

```

Breaking this down, `withExpectations` takes a function `Context => Unit`.
`Context` is a primitive (unsafe) API over hdfs operations that will allow you
to make assertions. The `Context` handles unexpected failures by failing the
test with a nice error message, but there is no way to do manual error handling
at this point.


thermometer source
------------------

A `ThermometerSource` is a thin wrapper around an in-memory scalding source
that is specialized so that it can be immediately treated as a TypedPipe without
corner cases (and better inference).

Usage:

```

  def pipeline =
    ThermometerSource(List("hello", "world"))            // : TypedPipe[String]
      .map(c => (c, "really" + c + "!"))
      .write(TypedPsv[(String, String)]("output"))

```


ongoing work
------------

 - Built-in support for more facts
   - Support for streaming comparisons
   - Support for statistical comparisons
   - Support for testing in-memory pipes without going to disk
 - A ThermometerSink that would allow in memory fact checking
 - General support for reading different file formats
 - Support for running full jobs in the same fact framework
 - Support for re-running tests with different scalding modes
 - Add the ability for Context to not depend on hdfs, via some
   sort of in memory representation for assertions.
