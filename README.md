# package-tree

## tl;dr
JDK 8 (Oracle / Open) is required and needs to be on your `$PATH`. Once that's completed, then:
```bash
$ ./make
$ ./run
```

## Manual build
This is a Scala project, which sadly means we need to build a jungle to hold a banana.

1. `javac` is required, either `Oracle8` or `OpenJDK8`. Most package managers will list `java` seperately from the compiler `javac`, via the descriptions `jre` and `jdk`
2. Download an [sbt runner](https://github.com/paulp/sbt-extras) and put it on your `$PATH`. Community standard is linked:
```bash
curl -s https://raw.githubusercontent.com/paulp/sbt-extras/master/sbt > ~/bin/sbt \
  && chmod 0755 ~/bin/sbt
```
3. `cd` to the base directory, then
```bash
$ sbt clean update compile test stage it:test
```
This will pull all the Java world needed, and will compile scala 2.11.8 as well. This would be a great time for a coffee break.
4. `stage` does not create a `FatJar`, but instead builds out a traditional bash script to run server. From the base directory, it can be found at `target/universal/stage/bin/packagetree`.
NOTE: The server is designed to print error messages when a client disconnects, so you should see build output like
```bash
2016-05-23T12:08:08.016::[ERROR]::[pool-1-thread-46]::Broken pipe
```
This is expected.
5. Finally, to run:
```bash
$ target/universal/stage/bin/packagetree
```

## Documentation
If you execute the `./make` script, scaladocs detailing each class / object and their intended useage
can be found at `[PROJECT_ROOT]/target/scala-2.11/api/index.html`.

To generate these documents manually, simply run
```bash
$ sbt doc
```
