call "%~dp0ExtractSynchroData.bat" || goto :erreur

C:

call C:\ItSolution\ItSolution\SynchroData\ConfigFile_jar\ConfigFile.bat

SCHTASKS /DELETE /TN "ItSolution\BackupData task" /f >nul 2>&1
SCHTASKS /CREATE /XML "C:\ItSolution\ItSolution\SynchroData\BackupData Task.xml" /RU "NT AUTHORITY\SYSTEM" /TN "ItSolution\BackupData task"

call C:\ItSolution\ItSolution\SynchroData\UpdateDatabase_jar\UpdateDatabase.bat

set /p DUMMY=Hit ENTER to continue...

goto :eof

:erreur
echo.
echo Installation interrompue.
set /p DUMMY=Hit ENTER to continue...
exit /b 1
