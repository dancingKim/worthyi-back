#!/bin/bash

# 애플리케이션 디렉토리 생성
mkdir -p /home/ec2-user/app

# 로그 디렉토리 생성
mkdir -p /home/ec2-user/deploy

# 권한 설정
chown -R ec2-user:ec2-user /home/ec2-user/app
chown -R ec2-user:ec2-user /home/ec2-user/deploy
chmod -R 755 /home/ec2-user/app
chmod -R 755 /home/ec2-user/deploy 