#!/bin/bash

# 애플리케이션의 상태를 확인하는 간단한 HTTP 요청
curl -f http://localhost:8080/health || exit 1 