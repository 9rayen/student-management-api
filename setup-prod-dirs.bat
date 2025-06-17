@echo off
echo Setting up production directories...
if not exist "data" mkdir data
if not exist "logs" mkdir logs
echo Production directories created successfully!
echo - data: For H2 database files
echo - logs: For application log files
