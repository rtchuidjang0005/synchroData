call "%~dp0ExtractSynchroData.bat" || goto :erreur

C:

call C:\ItSolution\ItSolution\SynchroData\ConfigFile_jar\ConfigFile.bat

REM la tache peut exister d'une installation precedente
SCHTASKS /DELETE /TN "ItSolution\UpdateData task" /f >nul 2>&1
SCHTASKS /CREATE /XML "C:\ItSolution\ItSolution\SynchroData\UpdateData Task.xml" /RU "NT AUTHORITY\SYSTEM" /TN "ItSolution\UpdateData task"

C:\ItSolution\ItSolution\SynchroData\UpdateDatabase_jar\UpdateDatabase.bat

set /p DUMMY=Hit ENTER to continue...

goto :eof

:erreur
echo.
echo Installation interrompue.
set /p DUMMY=Hit ENTER to continue...
exit /b 1
