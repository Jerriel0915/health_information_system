@echo off
chcp 65001 >nul
title algo-service-5001

REM 清除系统代理
set HTTP_PROXY=
set HTTPS_PROXY=
set http_proxy=
set https_proxy=

set ALGO_DIR=D:\tools\health_information_system\algorithm_service
set PYTHON=D:\tools\miniconda\envs\pyx\python.exe
set DASHSCOPE_API_KEY=sk-48b823311ea34682a98350393eafa3f1

for /f "tokens=5" %%i in ('netstat -ano ^| findstr ":5001"') do (
    echo kill old PID %%i
    taskkill /PID %%i /F >nul 2>&1
    timeout /t 2 /nobreak >nul
)

echo starting algo service on 5001...
cd /d "%ALGO_DIR%"
%PYTHON% -m uvicorn main:app --host 0.0.0.0 --port 5001 --reload

if errorlevel 1 (
    echo algo service failed
    pause
)
