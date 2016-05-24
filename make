#! /usr/bin/env bash

echo "Curling sbt runner . . . "
curl -s https://raw.githubusercontent.com/paulp/sbt-extras/master/sbt > sbt-runner \
    && chmod 0755 sbt-runner
echo "Done."

./sbt-runner clean update compile test doc stage it:test
