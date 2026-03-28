# Avatar Customization Plan - 2026-03-28

## Goal
- Add a new avatar customization flow where each user can generate and manage private character images.
- Keep the current product tone and UI/UX style.
- Support a fixed default avatar plus user-generated avatars.
- Allow using the default avatar or a previously generated avatar as a visual reference when generating a new one.
- Support edit prompts such as hair color or eye color changes.
- Generated avatars should keep roughly the current default avatar size and use a transparent background.

## Current Findings
- Client currently uses static avatar assets from the client repository.
- The current default avatar shown in the app is a bundled image:
  - `/Users/ho/WebstormProjects/worthy-i/assets/images/avatar-girl-transparent.png`
- The default avatar is rendered statically in:
  - `/Users/ho/WebstormProjects/worthy-i/app/(app)/(tabs)/index.tsx`
  - `/Users/ho/WebstormProjects/worthy-i/app/(app)/(tabs)/profile.tsx`
- Backend currently stores avatar metadata only through `Avatar.appearance` and has no per-user avatar image URL model or S3 storage path.
- Backend currently has no Google AI image generation integration.
- Action saving currently depends on template id `1` records for child/adult action templates.

## Requested Product Behavior
- Users can open a separate avatar customization tab.
- Users can choose:
  - the default avatar
  - one of their own previously generated avatars
- Users can generate new avatars privately for themselves.
- Users can delete only their own generated avatars.
- The default avatar cannot be deleted.
- Users can generate with reference to:
  - the default avatar
  - one of their own generated avatars
- Prompt examples:
  - "눈 색을 바꿔줘"
  - "머리색을 바꿔줘"

## Backend Work Checklist
- [x] Confirm AWS access policy needed for project work
- [x] Add secret wiring for `GOOGLE_AI_WORTHYI_KEY`
- [x] Design S3 key structure for per-user avatar images
- [x] Add backend persistence model for avatar image URL, source type, ownership, and delete rules
- [x] Add API for listing selectable avatars per user
- [x] Add API for generating a new avatar from prompt and optional reference image
- [x] Add API for setting the active avatar
- [x] Add API for deleting a user-generated avatar
- [x] Ensure default avatar is system-owned and non-deletable
- [ ] Verify EC2 runtime role has S3 put/get/delete permission for avatar object prefix

## Client Work Checklist
- [x] Confirm how current static avatar is used across tabs
- [x] Add avatar customization tab with current UI tone preserved
- [x] Show default avatar and user-generated avatars in one selector
- [x] Add prompt input and reference-avatar picker
- [x] Add active-avatar switching UX
- [x] Add delete action for generated avatars only
- [x] Replace current static avatar rendering with active avatar URL/default asset resolution
- [ ] Run the feature end-to-end against dev API on device/simulator

## Progress Log
- 2026-03-28:
  - Confirmed the current avatar is still a static client asset and not server-driven.
  - Confirmed backend does not yet have S3 avatar storage or avatar image URL persistence.
  - Confirmed the current action flow depends on hardcoded template id `1` for child/adult action templates.
  - Added backend `avatar_image` persistence and `User.activeAvatarImage` selection model, separate from the existing gameplay `Avatar` entity.
  - Added backend endpoints:
    - `GET /user/me/avatar-images`
    - `POST /user/me/avatar-images`
    - `PUT /user/me/avatar-image`
    - `DELETE /user/me/avatar-images/{avatarImageId}`
  - Wired Google AI image generation through `GOOGLE_AI_WORTHYI_KEY`.
  - Added model preset support for image-to-image generation:
    - `BEST -> gemini-3-pro-image-preview`
    - `BALANCED -> gemini-3.1-flash-image-preview`
    - `FAST -> gemini-2.5-flash-image`
  - Set backend generation flow to use the current default avatar image as the built-in reference image and request a transparent background in the prompt.
  - Added per-user S3 object storage under `avatars/users/{userId}/...` using the current dev bucket configuration.
  - Added a new client avatar customization tab that keeps the existing soft pink UI tone.
  - Replaced static avatar rendering in Home/Profile with active-avatar resolution from `/user/me`, falling back to the bundled default asset.
  - Added client-side controls for:
    - choosing the default avatar
    - choosing a generated avatar
    - picking a reference avatar
    - selecting quality/speed model presets
    - deleting generated avatars only
  - Tightened login state restoration so the app refreshes `/user/me` before treating the session as logged in.

## Implementation Notes
- Default avatar remains a bundled client asset and is not stored as a deletable DB record.
- The backend keeps a copy of the same default image at `src/main/resources/avatar/default-avatar-reference.png` so Google AI image-to-image generation can use it as the default reference.
- Generated avatars are private to the owning user and are returned as presigned S3 URLs.
- If a generated avatar is deleted while active, the app falls back to the default avatar.
- The current default model preset is `BEST`.

## Deployment Follow-ups
- EC2 instance role must be able to `s3:GetObject`, `s3:PutObject`, and `s3:DeleteObject` on the avatar object prefix in the dev bucket.
- Dev Secrets Manager already needs `GOOGLE_AI_WORTHYI_KEY`.
- Because only dev is active right now, the current implementation is configured against the dev API and dev bucket defaults.

## Verification Notes
- Backend compile check:
  - `./gradlew compileJava` passed in `/Users/ho/IdeaProjects/worthyi-back`.
- Client typecheck notes:
  - Project-wide TypeScript check still fails because of existing unrelated errors under `modules/my-module`.
  - No TypeScript errors were reported for the avatar customization files and related auth/rendering files after filtering the changed-file set.
- iOS simulator:
  - `npx expo start --ios` launched Metro and opened the iOS simulator for the dev build.
  - Avatar UI still needs a quick visual pass in the simulator after logging in, because the avatar tab is behind authenticated app navigation.

## 2026-03-28 Handoff Update
- Backend repository:
  - `/Users/ho/IdeaProjects/worthyi-back`
- Client repository:
  - `/Users/ho/WebstormProjects/worthy-i`

### Backend status
- Avatar backend work was committed and pushed on `develop`.
- Commit:
  - `947db60 feat: add avatar image generation flow`
- `origin/develop` now points to the avatar-image backend implementation.
- The pushed scope includes:
  - avatar image persistence/entity/repository/service/controller
  - `/avatar-images` APIs
  - `/user/me/avatar-images` and `/user/me/avatar-image` APIs
  - `User.activeAvatarImage` and `/user/me` response support
  - Google AI + S3 avatar config
  - default reference image resource

### Client status
- Client avatar UI work was committed and pushed on `develop`.
- Commit:
  - `ce4b514 feat: add avatar image customization UI`
- `origin/develop` in `/Users/ho/WebstormProjects/worthy-i` now points to the pushed avatar UI implementation.
- The pushed client scope includes:
  - new `Avatar` tab UI
  - avatar image API hook
  - default/generated avatar rendering component
  - Home/Profile active avatar rendering
  - auth refresh flow updates around `/user/me`

### iOS simulator status
- The iOS simulator app now builds and launches locally for the client repo.
- The simulator was verified with the locally built development app:
  - bundle id: `com.worthyilife.thanks.development`
- The app currently boots to `/login` because the authenticated tab flow is guarded until login succeeds.
- Two local dependency patches were needed for the simulator build on the current Xcode/iOS simulator toolchain:
  - `node_modules/expo-dev-menu/ios/DevMenuViewController.swift`
    - replaced `TARGET_IPHONE_SIMULATOR` usage with `#if targetEnvironment(simulator)`
  - `ios/Pods/Sentry/Sources/Sentry/include/SentryThreadMetadataCache.hpp`
    - changed `std::vector<const ThreadHandleMetadataPair>` to `std::vector<ThreadHandleMetadataPair>`
- These simulator patches are local debug/build patches only.
- They are not committed to the client repository and can disappear after reinstalling `node_modules` or Pods.

### Release / update note
- The client avatar UI changes were published to EAS Update on `2026-03-28`.
- EAS Update details:
  - branch: `share`
  - message: `avatar customization UI`
  - commit: `ce4b5142ad06695cda782f19530785049bdf5b87`
  - update group id: `f7b7e4a6-f0fd-43d5-bfef-e11a87c85acd`
  - dashboard: `https://expo.dev/accounts/worthyi/projects/worthy-i/updates/f7b7e4a6-f0fd-43d5-bfef-e11a87c85acd`
- A fresh EAS build is not required for the current avatar UI changes unless new native/config/dependency changes are intentionally introduced.
- The local simulator-only patches above are not part of the app feature diff and should not be treated as a reason to ship a new native build by themselves.

### Remaining follow-ups
- Verify the dev EC2 runtime role has avatar S3 prefix permissions.
- Log in on the simulator or device and do a real visual pass of:
  - `Avatar` tab
  - `Home` active avatar rendering
  - `Profile` active avatar rendering
- Confirm the `share` audience device pulls the latest update after app relaunch.
