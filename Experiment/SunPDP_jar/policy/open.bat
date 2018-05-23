@echo off
set start=%time%

@for /f "delims=" %%i in ('dir /b E:\XACML_Optimized_Matching\Experiment\SunPDP_jar\request\lms.xml') do @start "" "%%i"&&taskkill /f /im iexplore.exe
@for /f "delims=" %%i in ('dir /b E:\XACML_Optimized_Matching\Experiment\SunPDP_jar\policy\lms.xml') do @start "" "%%i"&&taskkill /f /im iexplore.exe
::@for /f "delims=" %%i in ('dir /b E:\XACML_Optimized_Matching\Experiment\SunPDP_jar\request\vms.xml') do @start "" "%%i"&&taskkill /f /im iexplore.exe
::@for /f "delims=" %%i in ('dir /b E:\XACML_Optimized_Matching\Experiment\SunPDP_jar\policy\vms.xml') do @start "" "%%i"&&taskkill /f /im iexplore.exe
::@for /f "delims=" %%i in ('dir /b E:\XACML_Optimized_Matching\Experiment\SunPDP_jar\request\asms.xml') do @start "" "%%i"&&taskkill /f /im iexplore.exe
::@for /f "delims=" %%i in ('dir /b E:\XACML_Optimized_Matching\Experiment\SunPDP_jar\policy\asms.xml') do @start "" "%%i"&&taskkill /f /im iexplore.exe

set end=%time%

set h1=%start:~0,2%
set m1=%start:~3,2%
set s1=%start:~6,2%
set ms1=%start:~9,2%
if "%h1:~0,1%"=="0" set h1=%h1:~1,1%
if "%m1:~0,1%"=="0" set m1=%m1:~1,1%
if "%s1:~0,1%"=="0" set s1=%s1:~1,1%
if "%ms1:~0,1%"=="0" set ms1=%ms1:~1,1%
set /a t1=%h1%*3600*1000+%m1%*60*1000+%s1%*1000+%ms1%*10
::echo %t1%

set h2=%end:~0,2%
set m2=%end:~3,2%
set s2=%end:~6,2%
set ms2=%end:~9,2%
if "%h2:~0,1%"=="0" set h2=%h2:~1,1%
if "%m2:~0,1%"=="0" set m2=%m2:~1,1%
if "%s2:~0,1%"=="0" set s2=%s2:~1,1%
if "%ms2:~0,1%"=="0" set ms2=%ms2:~1,1%
set /a t2=%h2%*3600*1000+%m2%*60*1000+%s2%*1000+%ms2%*10
::echo %t2%

set /a t=%t2%-%t1%
@echo on
echo %t%ms