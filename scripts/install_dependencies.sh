#!/bin/bash

# 시스템 패키지 업데이트
sudo yum update -y

# Java 설치 여부 확인
if type -p java; then
    echo "Java is already installed."
else
    echo "Java is not installed. Installing Amazon Corretto JDK..."
    sudo yum install -y java-23-amazon-corretto
fi