@echo off
echo Creating backup of H2 production database...
set BACKUP_DIR=backups
set TIMESTAMP=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%

if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

if exist "data\proddb.mv.db" (
    copy "data\proddb.mv.db" "%BACKUP_DIR%\proddb_%TIMESTAMP%.mv.db"
    echo Backup created: %BACKUP_DIR%\proddb_%TIMESTAMP%.mv.db
) else (
    echo No database file found to backup!
)
