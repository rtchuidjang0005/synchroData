call "%~dp0ExtractSynchroData.bat" || goto :erreur

c: 

C:\ItSolution\ItSolution\SynchroData\ConfigFile_jar\ConfigFile.bat

set /p DUMMY=Hit ENTER to continue...

goto :eof

:erreur
echo.
echo Installation interrompue.
set /p DUMMY=Hit ENTER to continue...
exit /b 1
