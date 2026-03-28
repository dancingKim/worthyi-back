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
    - `GET /avatar-images`
    - `POST /avatar-images/generate`
    - `PATCH /avatar-images/active`
    - `DELETE /avatar-images/{avatarImageId}`
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
