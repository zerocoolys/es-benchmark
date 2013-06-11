@echo off

SETLOCAL

if NOT DEFINED JAVA_HOME goto err

REM ***** JAVA options *****

if "%MIN_MEM%" == "" (
set MIN_MEM=1g
)

if "%MAX_MEM%" == "" (
set MAX_MEM=1g
)

if NOT "%HEAP_SIZE%" == "" (
set MIN_MEM=%HEAP_SIZE%
set MAX_MEM=%HEAP_SIZE%
)

set JAVA_OPTS=%JAVA_OPTS% -Xms%MIN_MEM% -Xmx%MAX_MEM%

set JAVA_OPTS=%JAVA_OPTS% -Xss256k


SET BENCHMARK_HOME=${project.build.directory}

set CLASSPATH=%CLASSPATH%;%BENCHMARK_HOME%/${project.build.finalName}.jar;%BENCHMARK_HOME%/libs/*;%BENCHMARK_HOME%/conf

"%JAVA_HOME%\bin\java" %JAVA_OPTS% %JAVA_OPTS% %PARAMS% %* -cp "%CLASSPATH%" "com.nelo2.benchmark.BenchmarkSuit"
goto finally

:err
echo JAVA_HOME environment variable must be set!
pause


:finally

ENDLOCAL