# AWS Copy Values - 2026-03-27

## 이 문서 용도
아래 값들은 AWS 콘솔, GitHub, OAuth 설정 화면에 그대로 붙여 넣기 쉽게 모아 둔 값들이다.

## Region
```text
ap-northeast-2
Asia Pacific (Seoul)
```

## VPC
```text
worthyi-main-vpc
172.31.0.0/16
```

## Public Domain
```text
worthyilife.com
```

## API Domains
```text
api-dev.worthyilife.com
api.worthyilife.com
```

## Route 53 Hosted Zone
```text
worthyilife.com
```

## Route 53 Record Names
```text
api-dev
api
```

## ACM Certificate Domains
```text
api-dev.worthyilife.com
api.worthyilife.com
```

## ACM Common Choices
```text
Disable export
DNS validation
RSA 2048
```

## Security Group Names
```text
worthyi-dev-alb-sg
worthyi-dev-app-sg
worthyi-dev-rds-sg
```

## Security Group Descriptions
```text
ALB security group for WorthyI dev
App security group for WorthyI dev
RDS security group for WorthyI dev
```

## Security Group Rule Types
```text
HTTP
HTTPS
Custom TCP
PostgreSQL
SSH
```

## Security Group Rule Values
```text
0.0.0.0/0
::/0
80
443
8080
5432
22
```

## ALB
```text
worthyi-dev-alb
```

## ALB Common Choices
```text
Internet-facing
IPv4
```

## Target Group
```text
worthyi-dev-tg
Instances
HTTP
8080
/actuator/health
200
```

## EC2
```text
worthyi-dev-app-1
Amazon Linux
t3.small
t3.medium
```

## IAM Role Names
```text
worthyi-dev-ec2-role
CodeDeployServiceRole
```

## IAM User Names
```text
github-actions-deployer
```

## IAM Managed Policies
```text
AmazonSSMManagedInstanceCore
AmazonS3ReadOnlyAccess
AWSCodeDeployRole
```

## RDS Subnet Group
```text
worthyi-shared-db-subnet-group
WorthyI shared DB subnet group
```

## RDS Instance
```text
worthyi-dev-postgres
worthyi_dev
worthyi
PostgreSQL
```

## Secrets Manager Secret Names
```text
/WorthyI/dev
/WorthyI/prod
```

## Secrets Manager Keys For Dev
```text
JWT_SECRET_KEY
DB_URL
DB_USERNAME
DB_PASSWORD
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
APPLE_CLIENT_ID
APPLE_CLIENT_SECRET
APPLE_KID
APPLE_TID
```

## Secrets Manager Keys For Prod
```text
JWT_SECRET_KEY
PROD_DB_URL
PROD_DB_USERNAME
PROD_DB_PASSWORD
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
```

## Secrets Manager JSON Template For Dev
```json
{
  "JWT_SECRET_KEY": "replace-with-long-random-secret",
  "DB_URL": "replace-with-rds-endpoint:5432/worthyi_dev",
  "DB_USERNAME": "worthyi",
  "DB_PASSWORD": "replace-with-db-password",
  "GOOGLE_CLIENT_ID": "replace",
  "GOOGLE_CLIENT_SECRET": "replace",
  "APPLE_CLIENT_ID": "replace",
  "APPLE_CLIENT_SECRET": "replace",
  "APPLE_KID": "replace",
  "APPLE_TID": "replace"
}
```

## DB_URL Example
```text
your-rds-endpoint.ap-northeast-2.rds.amazonaws.com:5432/worthyi_dev
```

## S3 Bucket
```text
worthyi-bucket-204098849975-ap-northeast-2-an
```

## CodeDeploy Application
```text
worthyi-back-dev
```

## CodeDeploy Deployment Group
```text
worthyi-back-dev-group
```

## CodeDeploy Deployment Config
```text
CodeDeployDefault.AllAtOnce
```

## EC2 Tags
```text
Project
WorthyI
Environment
dev
DeployGroup
worthyi-back-dev
```

## GitHub Actions Secret Names
```text
AWS_ACCESS_KEY_ID_DEPLOYER
AWS_SECRET_ACCESS_KEY_DEPLOYER
AWS_CODE_DEPLOY_APPLICATION
AWS_CODE_DEPLOY_GROUP
```

## GitHub Actions Secret Values
```text
github-actions-deployer access key ID
github-actions-deployer secret access key
worthyi-back-dev
worthyi-back-dev-group
```

## Google OAuth Client Names
```text
worthyi-dev-google-web
worthyi-prod-google-web
```

## Google OAuth Common Choices
```text
External
Testing
Web application
```

## Google OAuth Redirect URIs - Current Backend Code
```text
https://api-dev.worthyilife.com/login/oauth2/code/google
https://api.worthyi.com/login/oauth2/code/google
```

## Google OAuth Redirect URI - Planned Prod
```text
https://api.worthyilife.com/login/oauth2/code/google
```

## Apple OAuth Redirect URIs - Current Backend Code
```text
https://api-dev.worthyilife.com/login/oauth2/code/apple
```

## App Health Check
```text
/actuator/health
```

## Notes
```text
Security Group 이름은 sg-로 시작하면 안 됨
ACM과 ALB는 같은 리전이어야 함
Google OAuth client secret은 생성 직후에만 전체 값을 볼 수 있음
```
