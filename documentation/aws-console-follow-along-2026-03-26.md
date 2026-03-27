# AWS Console Follow-Along - Route53, ACM, ALB - 2026-03-26

## 이 문서의 목적
- AWS 새 계정에서 `Route53`, `ACM`, `ALB`를 다시 만들 때 그대로 따라할 수 있게 정리한다.
- 현재 코드베이스에 맞는 추천값을 같이 적는다.
- 기준은 `dev API`를 먼저 살리는 것이다.
- 순수 복사용 값은 [aws-copy-values-2026-03-27.md](/Users/ho/IdeaProjects/worthyi-back/documentation/aws-copy-values-2026-03-27.md) 에 따로 모아 둔다.

## 복사용 핵심 값
```text
worthyilife.com
api-dev.worthyilife.com
api.worthyilife.com
worthyi-dev-alb
worthyi-dev-tg
worthyi-dev-alb-sg
ALB security group for WorthyI dev
```

## 먼저 알아둘 점

### 이 프로젝트 기준 추천 구조
- DNS: `Route53`
- 인증서: `ACM`
- HTTPS 종단: `ALB`
- 앱 서버: `EC2`
- 앱 포트: `8080`
- ALB 대상 포트: `8080`
- 헬스체크 경로: `/actuator/health`
- 권장 리전: `ap-northeast-2`

### 코드에서 확인된 도메인
- dev API: `api-dev.worthyilife.com`
- prod API 현재 코드: `api.worthyi.com`
- prod API 목표 도메인: `api.worthyilife.com`
- 웹/리다이렉트 관련: `worthyilife.com`, `www.worthyilife.com`

### 제일 중요한 판단
아래 둘 중 무엇을 복구할지 먼저 정해야 한다.

1. `api-dev.worthyilife.com`만 먼저 살린다.
2. `api-dev.worthyilife.com`와 `api.worthyilife.com`를 둘 다 같이 살린다.

처음에는 1번이 안전하다.

## 이 문서에서 만들 것

### dev 기준으로 우선 만들 리소스
- Public Hosted Zone: `worthyilife.com`
- ACM Public Certificate: `api-dev.worthyilife.com`
- Application Load Balancer: 예시 이름 `worthyi-dev-alb`
- Target Group: 예시 이름 `worthyi-dev-tg`
- Route53 Alias Record: `api-dev.worthyilife.com -> ALB`

### prod도 같이 만들 경우 추가
- 같은 Public Hosted Zone: `worthyilife.com`
- ACM SAN 또는 별도 인증서: `api.worthyilife.com`
- Route53 Alias Record: `api.worthyilife.com -> ALB`

## 시작 전에 준비할 것

### 준비물
- 도메인 registrar 로그인 정보
- AWS 콘솔 로그인
- EC2가 들어갈 VPC와 subnet 계획
- EC2 인스턴스 1대 이상
- 백엔드가 8080에서 떠 있는 상태 또는 곧 뜰 수 있는 상태

### 지금 당장 확인할 것
- `worthyilife.com` 도메인을 누가 등록했는지
- `worthyilife.com`을 실제 서비스 표준으로 쓸지 다시 확인
- `worthyi.com`은 legacy로 남길지 아예 안 쓸지 결정

## 추천 복구 순서
1. Hosted Zone 생성
2. ACM 인증서 요청
3. ALB용 보안그룹 생성
4. Target Group 생성
5. ALB 생성
6. EC2를 Target Group에 등록
7. Route53 Alias Record 생성
8. 접속 테스트

---

## 1. Route53 Hosted Zone 만들기

### 무엇을 만들어야 하나
- `api-dev.worthyilife.com`를 쓸 거면 Hosted Zone은 보통 `worthyilife.com`를 만든다.
- `api.worthyilife.com`를 쓸 거면 Hosted Zone도 그대로 `worthyilife.com`를 쓴다.

주의:
- 보통 `api-dev.worthyilife.com` 자체를 Hosted Zone 이름으로 만들지 않는다.
- 부모 도메인인 `worthyilife.com` Hosted Zone을 만들고 그 안에 `api-dev` 레코드를 만든다.

### 콘솔 클릭 순서
1. AWS Console에서 `Route 53`로 이동한다.
2. 왼쪽 메뉴에서 `Hosted zones`를 누른다.
3. `Create hosted zone`을 누른다.
4. `Domain name`에 `worthyilife.com`을 입력한다.
5. `Type`은 `Public hosted zone`으로 둔다.
6. `Create hosted zone`을 누른다.

### 만들고 나면 확인할 것
- NS 레코드 1개
- SOA 레코드 1개

Route53은 Hosted Zone을 만들면 이름서버 4개를 자동으로 준다.

### 도메인 등록처에서 꼭 해야 하는 일
만약 도메인을 Route53이 아니라 다른 업체에서 등록했다면:

1. 등록처 사이트로 간다.
2. `worthyilife.com`의 nameserver 설정 화면을 연다.
3. Route53 Hosted Zone이 보여주는 NS 4개 값으로 교체한다.
4. 저장한다.

이걸 안 하면 Route53에 레코드를 만들어도 인터넷에서 안 보인다.

### prod 도메인도 쓸 경우
- Hosted Zone을 하나 더 만들지 않는다.
- 같은 `worthyilife.com` Hosted Zone 안에 `api` 레코드를 추가한다.

---

## 2. ACM 인증서 만들기

### 어떤 인증서를 만들까
처음에는 가장 단순하게 아래 중 하나로 간다.

#### dev만 먼저 살릴 때
- 인증서 도메인: `api-dev.worthyilife.com`

#### dev + prod 같이 살릴 때
- 인증서 도메인:
- `api-dev.worthyilife.com`
- `api.worthyilife.com`

이 경우 한 장의 인증서에 SAN으로 넣어도 되고, 인증서를 각각 따로 만들어도 된다.
처음엔 한 장으로 묶어도 된다.

### 매우 중요한 규칙
- `ALB`와 `ACM 인증서`는 같은 리전에 있어야 한다.
- 이 프로젝트는 `ap-northeast-2`로 맞추는 것이 안전하다.

### 콘솔 클릭 순서
1. AWS Console 상단 리전을 `Asia Pacific (Seoul)`, 즉 `ap-northeast-2`로 맞춘다.
2. `Certificate Manager`로 이동한다.
3. `Request` 또는 `Request a certificate`를 누른다.
4. `Request a public certificate`를 선택한다.
5. `Next`를 누른다.
6. 도메인 이름을 입력한다.

예시 A:
- `api-dev.worthyilife.com`

예시 B:
- `api-dev.worthyilife.com`
- `api.worthyilife.com`

7. `Validation method`는 `DNS validation`을 선택한다.
8. key algorithm은 기본값을 써도 된다.
9. `Request`를 누른다.

### 발급 완료까지 하는 방법
1. 방금 만든 인증서를 클릭한다.
2. 각 도메인 옆에 `Create records in Route 53` 버튼이 보이면 누른다.
3. 확인 팝업에서 레코드 생성을 진행한다.
4. 상태가 `Pending validation`에서 `Issued`로 바뀔 때까지 기다린다.

### 만약 `Create records in Route 53` 버튼이 안 보이면
- Hosted Zone이 아직 없거나
- 다른 계정에 Hosted Zone이 있거나
- 도메인 DNS가 아직 Route53이 아니거나
- 인증서 리전이 잘못되었을 수 있다.

이 경우 인증서 상세 화면의 CNAME 이름/값을 복사해서 Route53에 직접 레코드로 만든다.

### 성공 기준
- 인증서 상태가 `Issued`

---

## 3. 보안그룹 만들기

처음엔 보안그룹 3개면 충분하다.

### 3-1. ALB 보안그룹
예시 이름 / Description:

```text
worthyi-dev-alb-sg
ALB security group for WorthyI dev
```

#### Inbound
- HTTP 80 from `0.0.0.0/0`
- HTTPS 443 from `0.0.0.0/0`

IPv6도 쓸 거면:
- HTTP 80 from `::/0`
- HTTPS 443 from `::/0`

#### Outbound
- 전체 허용으로 시작해도 된다.

### 3-2. EC2 앱 보안그룹
예시 이름 / Description:

```text
worthyi-dev-app-sg
App security group for WorthyI dev
```

#### Inbound
- Custom TCP 8080 from `worthyi-dev-alb-sg`
- SSH 22 from 내 고정 IP 또는 회사 IP만

주의:
- 8080을 `0.0.0.0/0`로 열지 말고 ALB 보안그룹에서만 들어오게 하는 게 안전하다.

#### Outbound
- 전체 허용으로 시작해도 된다.

### 3-3. RDS 보안그룹
예시 이름 / Description:

```text
worthyi-dev-rds-sg
RDS security group for WorthyI dev
```

#### Inbound
- PostgreSQL 5432 from `worthyi-dev-app-sg`

주의:
- Security Group 이름은 `sg-`로 시작하면 안 된다.
- `sg-`는 AWS가 자동으로 생성하는 Security Group ID 형식이다.

---

## 4. Target Group 만들기

ALB는 직접 EC2로 보내지 않고 Target Group으로 보낸다.

### 추천값
- 이름: `worthyi-dev-tg`
- Target type: `Instances`
- Protocol: `HTTP`
- Port: `8080`
- VPC: EC2가 있는 VPC

### 헬스체크 추천값
- Protocol: `HTTP`
- Path: `/actuator/health`
- Success codes: `200`

이 프로젝트는 `/actuator/health`가 공개 허용되어 있어서 이 경로가 가장 안전하다.

### 콘솔 클릭 순서
1. AWS Console에서 `EC2`로 이동한다.
2. 왼쪽 메뉴에서 `Target Groups`를 누른다.
3. `Create target group`을 누른다.
4. `Choose a target type`는 `Instances`를 선택한다.
5. 이름을 `worthyi-dev-tg`로 입력한다.
6. `Protocol`은 `HTTP`, `Port`는 `8080`.
7. VPC를 선택한다.
8. 아래 `Health checks`에서 path를 `/actuator/health`로 바꾼다.
9. 생성한다.

### 아직 EC2를 등록하지 않아도 되나
된다.
ALB를 먼저 만든 뒤 Target Group에 EC2를 넣어도 된다.

---

## 5. ALB 만들기

### ALB에 필요한 조건
- `internet-facing`
- 서로 다른 AZ의 public subnet 최소 2개
- ALB 보안그룹 연결
- 대상 Target Group 연결

### 추천값
- 이름: `worthyi-dev-alb`
- Scheme: `Internet-facing`
- IP address type: `IPv4`
- VPC: 앱이 있는 VPC
- Subnets: public subnet 2개 이상

### Listener 추천

#### 처음부터 깔끔하게 만들기
- Listener 1: HTTP 80 -> Redirect to HTTPS 443
- Listener 2: HTTPS 443 -> Forward to `worthyi-dev-tg`

단, HTTPS listener를 만들려면 ACM 인증서가 이미 `Issued` 상태여야 한다.

### 콘솔 클릭 순서
1. AWS Console에서 `EC2`로 이동한다.
2. 왼쪽 메뉴에서 `Load Balancers`를 누른다.
3. `Create load balancer`를 누른다.
4. `Application Load Balancer`를 선택하고 `Create`를 누른다.
5. 이름을 `worthyi-dev-alb`로 입력한다.
6. `Scheme`은 `Internet-facing`.
7. `IP address type`은 `IPv4`.
8. VPC를 선택한다.
9. 서로 다른 AZ의 public subnet 2개 이상을 선택한다.
10. Security group은 `worthyi-dev-alb-sg`를 연결한다.

### Listener 설정

#### 80 포트
- Protocol: `HTTP`
- Port: `80`

처음엔 아래 둘 중 하나를 선택한다.

선택지 A:
- 바로 `Redirect to HTTPS`

선택지 B:
- 일단 `Forward to worthyi-dev-tg`
- 나중에 443 listener를 추가하고 80은 redirect로 바꾼다.

처음부터 제대로 할 거면 A가 좋다.

#### 443 포트
- Protocol: `HTTPS`
- Port: `443`
- Default action: `Forward to worthyi-dev-tg`
- Certificate: 방금 발급받은 ACM 인증서 선택
- Security policy: 기본 권장 정책 사용

### 만들고 나면 확인할 것
- ALB DNS 이름이 생성됨
- 예시 형태: `xxx.ap-northeast-2.elb.amazonaws.com`

---

## 6. EC2를 Target Group에 등록하기

### 먼저 확인할 것
- EC2가 `running` 상태인지
- 앱이 `8080`에서 떠 있는지
- EC2 보안그룹이 `8080`을 ALB SG로부터 허용하는지

### 콘솔 클릭 순서
1. `EC2 -> Target Groups`로 이동한다.
2. `worthyi-dev-tg`를 클릭한다.
3. `Targets` 탭을 연다.
4. `Register targets`를 누른다.
5. 해당 EC2 인스턴스를 선택한다.
6. Port가 `8080`인지 확인한다.
7. `Include as pending below`
8. `Register pending targets`

### 건강 상태 확인
1. `Targets` 탭에서 target 상태를 본다.
2. `healthy`가 되면 ALB가 정상 전달할 준비가 된 것이다.

### `unhealthy`가 나오면 제일 먼저 볼 것
- EC2 앱이 진짜 8080에서 떠 있는가
- `/actuator/health`가 200을 반환하는가
- EC2 SG가 ALB SG로부터 8080을 허용하는가
- Target Group health check path가 맞는가

---

## 7. Route53 Alias Record 만들기

이제 도메인을 ALB로 연결한다.

### dev 도메인 연결
목표:
- `api-dev.worthyilife.com -> worthyi-dev-alb`

### 콘솔 클릭 순서
1. `Route 53 -> Hosted zones -> worthyilife.com`으로 이동한다.
2. `Create record`를 누른다.
3. `Record name`에 `api-dev`를 입력한다.
4. `Record type`은 `A - IPv4 address`
5. `Alias`를 켠다.
6. `Route traffic to`에서 `Alias to Application and Classic Load Balancer`를 선택한다.
7. 리전은 `ap-northeast-2`
8. 방금 만든 `worthyi-dev-alb`를 선택한다.
9. 생성한다.

이렇게 하면 최종 주소는 `api-dev.worthyilife.com`이 된다.

### prod 도메인 연결
목표:
- `api.worthyilife.com -> ALB`

1. `Route 53 -> Hosted zones -> worthyilife.com`
2. `Create record`
3. `Record name`에 `api`
4. 나머지는 위와 동일

### apex 도메인일 경우
- `worthyilife.com` 그 자체를 ALB로 붙일 때는 Name을 비워 둔다.
- `@`를 넣지 말고 빈 값으로 두는 것이 Route53 콘솔 방식이다.

---

## 8. 테스트 순서

### 8-1. ALB 자체 DNS 테스트
브라우저 또는 curl로 ALB DNS 이름에 접속한다.

확인:
- 앱 응답이 오거나
- 최소한 health endpoint가 응답하는지

### 8-2. 도메인 테스트
아래 주소를 테스트한다.

#### dev
- `https://api-dev.worthyilife.com/actuator/health`

성공 기준:
- HTTP 200

### 8-3. 80 -> 443 리다이렉트 테스트
- `http://api-dev.worthyilife.com/actuator/health`

성공 기준:
- `https://...`로 이동

### 8-4. 백엔드 앱 테스트
- 클라이언트 앱에서 로그인 시작
- 백엔드 `/oauth2/authorization/...`
- `/auth/token`
- 인증 후 API 호출

---

## 현재 코드에 맞춘 추천 입력값 표

### dev 기준
- Region: `ap-northeast-2`
- Hosted Zone: `worthyilife.com`
- Certificate domain: `api-dev.worthyilife.com`
- ALB name: `worthyi-dev-alb`
- Target Group name: `worthyi-dev-tg`
- Target Group protocol: `HTTP`
- Target Group port: `8080`
- Health check path: `/actuator/health`
- Route53 record: `api-dev`

### prod 기준
- Region: `ap-northeast-2`
- Hosted Zone: `worthyilife.com`
- Certificate domain: `api.worthyilife.com`
- Route53 record: `api`

주의:
- 현재 backend prod 코드의 OAuth redirect URI는 아직 `api.worthyi.com`으로 남아 있다.
- prod를 `api.worthyilife.com`으로 갈 거면 backend 설정과 Google/Apple OAuth console 값을 먼저 같이 맞춰야 한다.

---

## 매우 흔한 실수

### 1. Hosted Zone은 만들었는데 registrar nameserver를 안 바꿈
증상:
- Route53에 레코드는 있는데 외부에서 도메인이 안 보임

### 2. ACM 인증서를 다른 리전에 만듦
증상:
- ALB 443 listener에서 인증서가 선택 목록에 안 나옴

### 3. ALB는 열었는데 EC2 SG가 8080을 안 열음
증상:
- Target이 `unhealthy`

### 4. health check path를 `/`로 둠
증상:
- 앱은 살아 있는데 health check 실패 가능

이 프로젝트는 `/actuator/health`를 쓰는 것이 안전하다.

### 5. `api-dev.worthyilife.com` Hosted Zone을 따로 만듦
증상:
- 구조가 복잡해지고 delegation까지 필요해짐

처음엔 부모 도메인 `worthyilife.com` Hosted Zone 하나로 가는 게 좋다.

### 6. 443 listener는 만들었는데 A Alias 레코드를 안 만듦
증상:
- ALB DNS 이름으로는 되지만 도메인으로는 안 됨

---

## 따라 하기용 체크리스트

### Route53
- [ ] `worthyilife.com` Public Hosted Zone 생성
- [ ] registrar nameserver를 Route53 NS 4개로 교체

### ACM
- [ ] 리전이 `ap-northeast-2`인지 확인
- [ ] `api-dev.worthyilife.com` 인증서 요청
- [ ] 필요 시 `api.worthyilife.com`도 같은 인증서 SAN 또는 별도 인증서로 요청
- [ ] DNS validation 레코드 생성
- [ ] 상태가 `Issued`인지 확인

### 보안그룹
- [ ] ALB SG 생성
- [ ] EC2 App SG 생성
- [ ] RDS SG 생성

### ALB
- [ ] Target Group 생성
- [ ] Health check path를 `/actuator/health`로 설정
- [ ] ALB 생성
- [ ] 80 listener 생성
- [ ] 443 listener 생성
- [ ] ACM 인증서 연결

### 연결
- [ ] EC2를 Target Group에 등록
- [ ] target 상태가 `healthy`
- [ ] Route53 A Alias `api-dev -> ALB` 생성
- [ ] `https://api-dev.worthyilife.com/actuator/health` 확인

---

## 이 프로젝트에서 바로 같이 점검하면 좋은 것

### 1. dev 도메인 우선 복구
클라이언트가 지금 dev 도메인을 많이 바라본다.

### 2. client production 설정 점검
현재 client `production` 설정도 dev API를 보고 있어서, prod를 따로 복구하면 나중에 수정이 필요할 수 있다.

### 3. OAuth redirect URI 점검
백엔드 설정에 아래 값들이 있다.
- `https://api-dev.worthyilife.com/login/oauth2/code/...`
- `https://api.worthyi.com/login/oauth2/code/...`

현재 목표 prod 도메인은 `api.worthyilife.com`이므로, prod를 열기 전에 backend 설정과 Google/Apple 콘솔 값을 같이 정리해야 한다.
Google 쪽 자세한 절차는 [google-oauth-client-follow-along-2026-03-27.md](/Users/ho/IdeaProjects/worthyi-back/documentation/google-oauth-client-follow-along-2026-03-27.md) 참고

---

## 공식 AWS 문서
- Route53 public hosted zone 생성: https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/CreatingHostedZone.html
- 기존 도메인을 Route53 DNS로 사용: https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/MigratingDNS.html
- ACM public certificate 요청: https://docs.aws.amazon.com/acm/latest/userguide/acm-public-certificates.html
- ACM 개요와 리전 주의사항: https://docs.aws.amazon.com/acm/latest/userguide/acm-overview.html
- Application Load Balancer 생성: https://docs.aws.amazon.com/elasticloadbalancing/latest/application/create-application-load-balancer.html
- Target Group 생성: https://docs.aws.amazon.com/elasticloadbalancing/latest/application/create-target-group.html
- HTTPS listener 생성: https://docs.aws.amazon.com/elasticloadbalancing/latest/application/create-https-listener.html
- Target Group에 EC2 등록: https://docs.aws.amazon.com/elasticloadbalancing/latest/application/target-group-register-targets.html
- Route53에서 ALB로 alias 연결: https://docs.aws.amazon.com/Route53/latest/DeveloperGuide/routing-to-elb-load-balancer.html
- ALB target health 확인: https://docs.aws.amazon.com/elasticloadbalancing/latest/application/check-target-health.html

## 참고용 코드 포인트
- backend region / secrets import: `/Users/ho/IdeaProjects/worthyi-back/src/main/resources/application-local.yaml`
- backend dev redirect URI: `/Users/ho/IdeaProjects/worthyi-back/src/main/resources/application-dev.yaml`
- backend prod redirect URI: `/Users/ho/IdeaProjects/worthyi-back/src/main/resources/application-prod.yaml`
- actuator health permit: `/Users/ho/IdeaProjects/worthyi-back/src/main/java/com/worthyi/worthyi_backend/config/SecurityConfig.java`
