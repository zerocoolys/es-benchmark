#!/bin/bash

if [ -x "$JAVA_HOME/bin/java" ]; then
    JAVA="$JAVA_HOME/bin/java"
else
    JAVA=java
fi

echo 'using JAVA $JAVA'

JAVA_OPTS=%JAVA_OPTS% -Xms%MIN_MEM% -Xmx%MAX_MEM%

JAVA_OPTS=%JAVA_OPTS% -Xss256k


BENCHMARK_HOME=${project.build.directory}

CLASSPATH=%CLASSPATH%;%BENCHMARK_HOME%/${project.build.finalName}.jar;%BENCHMARK_HOME%/libs/*;

"%JAVA_HOME%\bin\java" %JAVA_OPTS% %JAVA_OPTS% %* -cp "%CLASSPATH%" "com.nelo2.benchmark.BenchmarkSuit"


