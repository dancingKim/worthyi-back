#!/bin/bash

# 로그 파일 경로 설정
LOG_FILE="/home/ec2-user/deploy/install_dependencies.log"
ERROR_LOG_FILE="/home/ec2-user/deploy/install_dependencies_error.log"

# 로그 디렉토리 생성
mkdir -p /home/ec2-user/deploy

# 로그 시작
echo "=== Installation Start: $(date) ===" >> $LOG_FILE

# 시스템 패키지 업데이트
{
    echo "Updating system packages..." >> $LOG_FILE
    sudo yum update -y
} >> $LOG_FILE 2>> $ERROR_LOG_FILE

# Java 설치 여부 확인
if type -p java; then
    echo "Java is already installed." >> $LOG_FILE
else
    echo "Java is not installed. Installing Amazon Corretto JDK..." >> $LOG_FILE
    {
        sudo yum install -y java-23-amazon-corretto
    } >> $LOG_FILE 2>> $ERROR_LOG_FILE
fi

# 설치 결과 확인
if type -p java; then
    echo "Java installation successful" >> $LOG_FILE
    java -version >> $LOG_FILE 2>&1
else
    echo "Java installation failed" >> $ERROR_LOG_FILE
    exit 1
fi

echo "=== Installation End: $(date) ===" >> $LOG_FILE