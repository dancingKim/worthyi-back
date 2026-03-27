# Google OAuth Client Follow-Along - 2026-03-27

## 이 문서의 목적
- Google OAuth2 Client가 없어졌을 때 다시 만드는 순서를 남긴다.
- 기준은 현재 백엔드 코드와 맞는 `dev` 로그인 복구다.
- 콘솔 UI는 2026-03-27 기준 `Google Auth Platform` 메뉴명을 따른다.

## 먼저 결론
- 이 프로젝트는 Google에서 `Web application` 타입 OAuth client를 만들어야 한다.
- dev redirect URI는 정확히 아래 값이다.

```text
https://api-dev.worthyilife.com/login/oauth2/code/google
```

- 생성 후 받은 `client ID`, `client secret`은 `/WorthyI/dev` secret에 넣는다.
- 현재 prod 코드는 아직 `api.worthyi.com`을 기대하므로, prod client는 dev 복구 후 별도로 만들거나 코드부터 수정하고 만드는 것이 안전하다.

## 현재 코드 기준 값

### dev
- 파일: `/Users/ho/IdeaProjects/worthyi-back/src/main/resources/application-dev.yaml`
- Google redirect URI:

```text
https://api-dev.worthyilife.com/login/oauth2/code/google
```

### prod
- 파일: `/Users/ho/IdeaProjects/worthyi-back/src/main/resources/application-prod.yaml`
- 현재 코드의 Google redirect URI:

```text
https://api.worthyi.com/login/oauth2/code/google
```

### prod 목표값
사용자 결정 기준으로는 아래로 바꾸고 싶은 상태다.

```text
https://api.worthyilife.com/login/oauth2/code/google
```

주의:
- 아직 백엔드 prod 설정은 위 목표값으로 바뀌지 않았다.
- 그래서 prod client를 먼저 만들 거면 `현재 코드값`을 기준으로 만들지, `코드를 먼저 수정한 뒤 목표값`으로 만들지 먼저 정해야 한다.

## 추천 전략

### 가장 안전한 방식
- dev client와 prod client를 분리한다.
- dev client 이름 예시:

```text
worthyi-dev-google-web
```

- prod client 이름 예시:

```text
worthyi-prod-google-web
```

### 왜 분리하나
- redirect URI를 환경별로 섞지 않아도 된다.
- secret 교체 시 dev/prod가 서로 덜 영향을 준다.
- 나중에 prod 도메인을 바꿀 때 dev client를 건드리지 않아도 된다.

## 콘솔에서 만드는 순서

### 1. Google Cloud Console에서 프로젝트 선택
1. `https://console.cloud.google.com/` 에 로그인한다.
2. 상단 프로젝트 선택기에서 기존 프로젝트를 선택하거나 새 프로젝트를 만든다.
3. 프로젝트가 선택된 상태에서 `Google Auth Platform`으로 들어간다.

직접 들어가는 경로:
- `Google Cloud Console -> Google Auth Platform -> Overview`

### 2. 앱 기본 등록
프로젝트에서 Google Auth Platform을 처음 쓰는 경우 `GET STARTED`를 누른다.

추천 입력값:

```text
App name: WorthyI
User support email: 실제 확인 가능한 메일
Developer contact information: 실제 확인 가능한 메일
Audience: External
```

설명:
- 일반 사용자 Google 계정으로 로그인 받을 가능성이 있으므로 보통 `External`이 맞다.
- 조직 내부 전용 Google Workspace 앱이 아니면 `Internal`은 보통 맞지 않는다.

### 3. Branding 점검
`Branding`에서 아래를 확인한다.

- App name
- User support email
- Developer contact information

나중에 prod 공개 운영까지 할 거면 추가로 준비할 것:
- Homepage URL
- Privacy Policy URL
- Terms of Service URL
- Authorized domains

도메인 관련 주의:
- Google은 Branding 페이지와 Client 페이지에서 쓰는 도메인을 `Authorized domains`에 미리 등록하라고 안내한다.
- `worthyilife.com`을 공식 도메인으로 쓸 거면 Authorized domain에 `worthyilife.com`을 넣는 편이 안전하다.

### 4. Audience 설정
`Audience` 페이지에서 아래처럼 잡는다.

#### dev만 먼저 살릴 때 추천

```text
User type: External
Publishing status: Testing
```

이 프로젝트는 현재 코드상 Google scope가 `email`, `profile`뿐이다.
Google 공식 문서는 이름, 이메일, 프로필 같은 identity scope만 쓰는 경우 Testing 모드의 일부 제한이 예외 적용될 수 있다고 설명한다. 그래도 dev 복구 단계에서는 `Testing`으로 시작하는 편이 안전하다.

### 5. Data Access 확인
`Data Access` 페이지가 보이면, 현재 프로젝트에 필요한 scope만 유지한다.

현재 코드 기준 Google scope:

```text
email
profile
```

권장:
- Gmail, Drive 같은 추가 scope는 넣지 않는다.
- 현재 서비스가 실제로 안 쓰는 scope는 요청하지 않는다.

### 6. Clients에서 OAuth client 생성
1. `Clients` 페이지로 이동한다.
2. `CREATE CLIENT`를 누른다.
3. Application type은 `Web application`을 선택한다.

dev 추천 입력값:

```text
Name: worthyi-dev-google-web
Application type: Web application
```

#### Authorized JavaScript origins
현재 프로젝트는 브라우저 JavaScript가 Google OAuth endpoint를 직접 때리는 구조가 아니라, 백엔드 Spring Security가 서버에서 OAuth flow를 처리한다.

그래서 dev client는 보통 아래처럼 시작하면 된다.

```text
Authorized JavaScript origins: 비워 둠
```

#### Authorized redirect URIs
반드시 정확히 아래 값을 넣는다.

```text
https://api-dev.worthyilife.com/login/oauth2/code/google
```

주의:
- 끝 `/google`까지 정확히 일치해야 한다.
- 오타, http/https 차이, 마지막 슬래시 차이만 있어도 `redirect_uri_mismatch`가 난다.

### 7. Create 후 즉시 저장
클라이언트를 생성하면 `Client ID`와 `Client secret`이 나온다.

바로 해야 할 일:
1. `Client ID` 복사
2. `Client secret` 복사
3. `/WorthyI/dev` secret에 아래 키로 저장

```text
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
```

중요:
- Google 공식 문서는 새 client secret이 생성 시점에만 전체 값이 보이고, 이후에는 전체 값을 다시 볼 수 없다고 안내한다.
- 잃어버리면 새 secret을 추가 생성해서 교체해야 한다.

### 8. Secrets Manager에 넣을 값
`/WorthyI/dev` 안에서 Google 관련 값은 아래 두 개다.

```json
{
  "GOOGLE_CLIENT_ID": "replace-with-google-client-id",
  "GOOGLE_CLIENT_SECRET": "replace-with-google-client-secret"
}
```

### 9. 동작 확인
아래 주소로 로그인 시작이 되는지 확인한다.

```text
https://api-dev.worthyilife.com/oauth2/authorization/google
```

확인 포인트:
- Google 로그인 화면으로 이동하는지
- 로그인 후 `https://api-dev.worthyilife.com/login/oauth2/code/google`로 다시 돌아오는지
- 백엔드에서 정상 토큰 발급/리다이렉트가 되는지

## prod를 만들 때

### 지금 바로 만들지 말고 먼저 확인할 것
현재 prod backend 설정은 아래 URI를 기대한다.

```text
https://api.worthyi.com/login/oauth2/code/google
```

하지만 앞으로는 아래를 쓰고 싶다고 결정한 상태다.

```text
https://api.worthyilife.com/login/oauth2/code/google
```

### 안전한 선택지
1. dev만 먼저 복구한다.
2. 이후 backend prod 설정을 `api.worthyilife.com`로 바꾼다.
3. 그 다음 prod Google client를 만든다.

### 급하면 가능한 임시 선택지
같은 prod client에 redirect URI를 여러 개 넣을 수 있으면 아래 둘을 같이 등록해 두고, 코드 전환이 끝나면 옛 URI를 지우는 방식도 가능하다.

```text
https://api.worthyi.com/login/oauth2/code/google
https://api.worthyilife.com/login/oauth2/code/google
```

그래도 권장은 환경 분리다.

## 왜 Google client가 사라졌을 수 있나
Google 공식 문서 기준:
- OAuth client가 6개월 동안 사용도 안 되고 설정 변경도 없으면 자동 삭제될 수 있다.
- 삭제 30일 전 이메일 알림이 간다.
- 삭제 후 보통 최소 30일은 복구 가능하다.

즉 이번 경우는 아래 가능성이 있다.
- 정말 삭제되었음
- 비활성 client 자동 삭제
- 프로젝트를 잘못 보고 있음

확인 경로:
- `Google Auth Platform -> Clients`
- `Deleted credentials`

## 자주 나는 오류

### `redirect_uri_mismatch`
- Google Console의 Authorized redirect URI와 백엔드 설정값이 정확히 다르다.
- 제일 흔한 원인이다.

### `deleted_client`
- client가 삭제되었거나 자동 삭제되었다.
- `Deleted credentials`에서 복구 가능한지 먼저 본다.

### `invalid_client`
- `GOOGLE_CLIENT_ID` 또는 `GOOGLE_CLIENT_SECRET`가 틀렸거나 다른 프로젝트의 값이다.

### `origin_mismatch`
- JavaScript origin을 잘못 넣고 프런트 직접 호출 구조처럼 만들었을 때 난다.
- 현재 backend OAuth flow에서는 보통 redirect URI 쪽을 먼저 보면 된다.

### `app_not_configured_for_user`
- Audience, Publishing status, test user, verification 상태가 꼬였을 가능성이 있다.

## 복사용 핵심 값
```text
WorthyI
External
Testing
worthyi-dev-google-web
worthyi-prod-google-web
Web application
https://api-dev.worthyilife.com/login/oauth2/code/google
https://api.worthyi.com/login/oauth2/code/google
https://api.worthyilife.com/login/oauth2/code/google
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
```

## 공식 Google 문서
- Google Auth Platform 시작: https://support.google.com/cloud/answer/15544987
- OAuth client 관리: https://support.google.com/cloud/answer/6158849
- App audience 관리: https://support.google.com/cloud/answer/15549945
- App data access 관리: https://support.google.com/cloud/answer/15549135
- App branding 관리: https://support.google.com/cloud/answer/10311615
- OAuth app verification help: https://support.google.com/cloud/answer/13463073
