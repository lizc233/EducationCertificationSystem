# Session Memory 2026-07-18

## 1. Purpose
- This file is for future AI wake-up.
- Keep only state that matters for continuing work safely.

## 2. Actual Repo Topology
- Backend workspace:
  - `E:\java_ai_code\EducationCertificationSystem`
- Actual running frontend used by user:
  - `E:\java_ai_code\EducationCertificationSystem_fronted\frontend`
- Important:
  - The live frontend is outside the backend workspace.
  - User repeatedly required fixes to be applied to the live frontend path, not only the backend repo copy.

## 3. User Constraints That Must Be Preserved
- Do not delete whole files and recreate them.
- Use targeted edits only.
- Do not casually change UI appearance.
- Customer-facing empty states must say effectively "no data", not "backend not implemented".
- Do not write temp files to `C:`.
- If verification generates build artifacts, remove them afterward.
- User is highly sensitive to careless edits and regressions.

## 4. Files Read For Prior Context
- Prior conversation memory used:
  - `ai_read/SESSION_MEMORY_2026-07-17_AI.md`

## 5. Major Situation Reached In This Session
- Many frontend pages were throwing uncaught Axios 404/400 errors on mount.
- A major source of confusion was that:
  - backend code existed,
  - but the running frontend had wrong endpoints, missing fallback handling, or stale mock-oriented logic.
- Another major source of confusion:
  - survey publish / save / submit issues were partly backend-side business logic issues, not just proxy wiring.

## 6. Backend Fixes Applied In Source
- File:
  - `src/main/java/com/educationcertificationsystem/survey/service/impl/SurveyQuestionnaireServiceImpl.java`
- Effective fixes:
  - questionnaire save path now clears child rows before reinserting, reducing duplicate/dirty child-data issues.
  - publish event no longer hard-fails the whole flow just because no recipient users matched the current scope.

- File:
  - `src/main/java/com/educationcertificationsystem/survey/controller/SurveyResponseController.java`
- Effective fix:
  - removed controller-layer `@Transactional` from submit endpoint.

- File:
  - `src/main/java/com/educationcertificationsystem/survey/service/impl/SurveyResponseServiceImpl.java`
- Effective fixes:
  - added pre-checks for `respondentUserId`.
  - added pre-checks that the current respondent user actually exists.
  - error wording was made more business-readable instead of surfacing low-level insert/FK style failures.

## 7. Frontend Infrastructure Fixes Applied To Live Frontend
- File:
  - `E:\java_ai_code\EducationCertificationSystem_fronted\frontend\src\utils\request.js`
- Effective fix:
  - response handling now treats only `code === 1` or `code === 200` as success.
  - `code === 0` is no longer incorrectly treated as success.

## 8. Live Frontend Page Fixes Already Applied

### 8.1 User Management
- File:
  - `E:\java_ai_code\EducationCertificationSystem_fronted\frontend\src\views\UserManage.vue`
- Effective fix:
  - page init now tolerates missing old role-related endpoints.
  - when the endpoint is absent, page falls back to empty/default state instead of crashing on mount.

### 8.2 Course Selection
- File:
  - `E:\java_ai_code\EducationCertificationSystem_fronted\frontend\src\views\pages\CourseSelectionManagementView.vue`
- Effective fix:
  - list load and roster load now have 404-safe fallback handling.
  - page should no longer throw uncaught mount-time 404 merely because data/endpoint path is absent.

### 8.3 Survey Fill Workbench
- Files:
  - `E:\java_ai_code\EducationCertificationSystem_fronted\frontend\src\views\pages\SurveyFillWorkbench.logic.js`
  - `E:\java_ai_code\EducationCertificationSystem_fronted\frontend\src\views\pages\SurveyFillWorkbenchView.vue`
- Effective fixes:
  - replaced Element Plus radio `label` usage with `value` where needed, removing the deprecation warning on this page.
  - wired frontend submit availability to backend `canSubmit`.
  - submit button now uses `selectedSubmitBlocked`.
  - fill controls are disabled when the selected survey is not actually submittable.
  - logic now blocks submit if:
    - no selected survey,
    - already submitted,
    - `canSubmit !== 1`.
  - when blocked by backend state, frontend shows the backend-provided `submitMessage` instead of continuing to call submit.

## 9. Critical Runtime Finding About Survey Submit
- Runtime inspection against the running app showed:
  - questionnaire `id=1` was `PUBLISHED`,
  - but `startTime = 2026-07-28T00:00:00`,
  - `endTime = 2026-07-31T00:00:00`.
- Fill-view result for current student user showed:
  - `alreadySubmitted = 0`
  - `canSubmit = 0`
  - `submitMessage = "Questionnaire has not started yet"`
- Conclusion:
  - the survey submit failure seen by user was not only a frontend wiring problem.
  - the specific questionnaire was not open yet.
  - the correct immediate frontend behavior is "cannot submit now", not "send POST anyway".

## 10. Remaining Backend Uncertainty On Submit
- Even after source-side guard fixes, manual POST to:
  - `/api/surveys/questionnaires/1/responses/submit`
  still produced raw HTTP 500 during this session.
- Most likely explanations:
  - running backend process was not using the latest compiled code, or
  - exception occurred outside the expected controller catch path.
- Important:
  - after the frontend `canSubmit` gating fix, the user-facing page should stop triggering that invalid submit path for not-yet-open questionnaires.

## 11. Build Verification Performed
- Verified live frontend with:
  - `npm run build`
  - in `E:\java_ai_code\EducationCertificationSystem_fronted\frontend`
- Build passed.
- `dist` was deleted afterward.

## 12. Backend Verification Limitation Hit
- Maven compile / wrapper verification was not cleanly completed in this environment.
- Observed environment issue:
  - `AccessDeniedException` involving `target\classes\application-dev.example.yml`
- Do not assume full backend runtime was rebuilt from the edited source unless that is explicitly re-verified.

## 13. High-Risk Practical Lessons For Next AI
- Always confirm which frontend path the user is actually running.
- For this project, do not assume the workspace copy of frontend is the live one.
- On customer pages:
  - if backend returns empty data, show empty state.
  - do not expose internal implementation commentary.
- When a page throws on mount:
  - first inspect the actual requested URL and current proxy target.
- For survey issues:
  - inspect questionnaire time window and fill-view flags before assuming submit endpoint is broken.

## 14. Most Relevant Files For Immediate Continuation
- Backend:
  - `src/main/java/com/educationcertificationsystem/survey/service/impl/SurveyQuestionnaireServiceImpl.java`
  - `src/main/java/com/educationcertificationsystem/survey/controller/SurveyResponseController.java`
  - `src/main/java/com/educationcertificationsystem/survey/service/impl/SurveyResponseServiceImpl.java`
- Live frontend:
  - `E:\java_ai_code\EducationCertificationSystem_fronted\frontend\src\utils\request.js`
  - `E:\java_ai_code\EducationCertificationSystem_fronted\frontend\src\views\pages\SurveyFillWorkbench.logic.js`
  - `E:\java_ai_code\EducationCertificationSystem_fronted\frontend\src\views\pages\SurveyFillWorkbenchView.vue`
  - `E:\java_ai_code\EducationCertificationSystem_fronted\frontend\src\views\UserManage.vue`
  - `E:\java_ai_code\EducationCertificationSystem_fronted\frontend\src\views\pages\CourseSelectionManagementView.vue`

## 15. Recommended Next Actions
1. If survey submit still needs backend validation, test again only with a questionnaire whose start time is already open.
2. If POST submit still returns 500 for an actually open questionnaire, inspect the running backend logs immediately at submit time.
3. Continue replacing crash-on-mount behavior page by page with real empty/error states, but do not redesign UI.
4. Before touching more frontend code, verify whether the running backend process has actually been restarted from latest source.
