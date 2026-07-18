# Session Memory 2026-07-17

## 1. Purpose
- This file is for future AI wake-up, not for human-facing explanation.
- Focus: what was changed, where, current state, known risks, next actions.

## 2. Workspace Topology
- Backend repo cwd:
  - `E:\java_ai_code\EducationCertificationSystem`
- Frontend repo used in this session:
  - `E:\java_ai_code\EducationCertificationSystem_fronted\frontend`
- Frontend repo is outside backend workspace root.
- `apply_patch` can still modify sibling frontend files with paths like:
  - `../EducationCertificationSystem_fronted/frontend/...`
- Do not rely on frontend git status unless safe.directory is configured.

## 3. User Constraints To Preserve
- User is highly sensitive to careless file edits/deletions.
- Do not write temp files to `C:`.
- If build artifacts are generated during verification, remove them afterward.
- Use `apply_patch` for manual file edits.
- If blocked or a risky assumption is required, stop and ask.

## 4. Backend State Reached Before Frontend Work
- F16-F25 backend APIs already exist and were used for frontend integration.
- Important backend `Result<T>` contract:
  - success: `code = 1`
  - error: `code = 0`
- Frontend `src/api/http.js` was updated earlier to treat `code=1` as success.

## 5. D Scope Completed In Frontend
- Member D pages:
  - `F16` `/messages`
  - `F17` `/achievement/model`
  - `F18` `/achievement/course`
  - `F19` `/achievement/graduate`
  - `F20` `/achievement/dashboard`
- D frontend status:
  - Real backend API wiring completed.
  - Static/sample page data removed from D pages.
  - `/notice` proxy added in Vite because notice routes are not under `/api`.
  - Admin top unread badge switched from fake local counter to real unread count API.
  - `src/data/messages.js` removed after layout stopped using it.
- D build status:
  - Passed `npm run build`.

## 6. D Frontend Files Modified
- `../EducationCertificationSystem_fronted/frontend/src/api/http.js`
- `../EducationCertificationSystem_fronted/frontend/src/api/lookups.js`
- `../EducationCertificationSystem_fronted/frontend/src/api/notice.js`
- `../EducationCertificationSystem_fronted/frontend/src/api/teaching.js`
- `../EducationCertificationSystem_fronted/frontend/src/api/eval.js`
- `../EducationCertificationSystem_fronted/frontend/src/views/pages/MessagesView.vue`
- `../EducationCertificationSystem_fronted/frontend/src/views/pages/AchievementModelView.vue`
- `../EducationCertificationSystem_fronted/frontend/src/views/pages/AchievementCourseView.vue`
- `../EducationCertificationSystem_fronted/frontend/src/views/pages/AchievementGraduateView.vue`
- `../EducationCertificationSystem_fronted/frontend/src/views/pages/AchievementDashboardView.vue`
- `../EducationCertificationSystem_fronted/frontend/src/layouts/AdminLayout.vue`
- `../EducationCertificationSystem_fronted/frontend/src/router/index.js`
- `../EducationCertificationSystem_fronted/frontend/vite.config.js`

## 7. E Scope Started And Integrated In Frontend
- Member E pages:
  - `F21` `/survey`
  - `F22` `/survey/fill`
  - `F23` `/improve`
  - `F24` `/report`
  - `F25` `/ai`
- Strategy used:
  - Replace shell/template pages with real backend integration.
  - Keep existing frontend auth/user split as-is unless absolutely required.
  - Prefer business-closed-loop pages, not CRUD shells only.

## 8. New Frontend API Modules Added For E
- `../EducationCertificationSystem_fronted/frontend/src/api/survey.js`
- `../EducationCertificationSystem_fronted/frontend/src/api/improve.js`
- `../EducationCertificationSystem_fronted/frontend/src/api/report.js`
- `../EducationCertificationSystem_fronted/frontend/src/api/ai.js`

## 9. E Frontend Pages Added/Replaced
- Added:
  - `../EducationCertificationSystem_fronted/frontend/src/views/pages/SurveyManagementView.vue`
  - `../EducationCertificationSystem_fronted/frontend/src/views/pages/ImprovePlanView.vue`
- Replaced existing static pages with real-data versions:
  - `../EducationCertificationSystem_fronted/frontend/src/views/pages/SurveyFillView.vue`
  - `../EducationCertificationSystem_fronted/frontend/src/views/pages/ReportView.vue`
  - `../EducationCertificationSystem_fronted/frontend/src/views/pages/AiAssistantView.vue`
- Router updated:
  - `/survey` now uses `SurveyManagementView`
  - `/improve` now uses `ImprovePlanView`
  - `/survey/fill`, `/report`, `/ai` remain same paths but now point to real backend-integrated pages

## 10. E Page Functional Intent Implemented

### F21 Survey Management
- Page supports:
  - questionnaire list
  - questionnaire detail edit
  - scope edit
  - question structure edit
  - publish / retry publish / remind / revoke / end
  - publish task history
  - preview dialog
- Uses real backend:
  - `/api/surveys/questionnaires`
  - detail / preview / publish-tasks / publish / retry-publish / revoke / end / deadline-reminder

### F22 Survey Fill + Stats
- Page supports:
  - load published questionnaires
  - resolve fill view per current user
  - dynamic render of `SINGLE`, `MULTIPLE`, `SCALE`, `TEXT`, `MATRIX`
  - submit answers
  - admin stats tab: overview, question stats, response list, response detail, export
- Uses real backend:
  - fill view
  - submit response
  - response overview
  - question stats
  - response page/detail
  - download responses

### F23 Improve Plan
- Page supports:
  - plan list + detail
  - create/update/delete plan
  - action list edit in form
  - start / complete / verify / remind
  - action progress update dialog
  - record create/update/delete
  - jump to AI page for improve suggestion
- Uses real backend:
  - `/api/improve/plans`
  - `/start`, `/complete`, `/verify`, `/remind`
  - `/actions/{actionId}/progress`
  - `/actions/{actionId}/records`
  - `/records/{recordId}`

### F24 Report Project
- Page supports:
  - report project list
  - create project
  - chapter tree display
  - add root/child chapter
  - chapter lock/unlock
  - chapter draft save
  - progress log save
  - assignment save
  - generate initial drafts
  - merged report export
  - jump to AI page for report chapter polishing/expansion
- Uses real backend:
  - `/api/reports/projects`
  - detail
  - chapter tree save
  - assignment save
  - chapter draft save
  - chapter lock
  - progress board
  - generate drafts
  - merged report preview/download

### F25 AI Assistant
- Page supports:
  - mode switch: `report` / `improve`
  - AI request history page
  - request detail
  - report chapter generate + confirm writeback
  - improve suggestion generate + confirm writeback
  - retry request
  - rebuild knowledge index
  - display retrieved RAG chunks
- Uses real backend:
  - `/api/ai/reports/chapters/{chapterId}/generate`
  - `/api/ai/reports/chapters/{chapterId}/confirm`
  - `/api/ai/improve-suggestions/generate`
  - `/api/ai/improve-suggestions/confirm`
  - `/api/ai/requests`
  - `/api/ai/requests/{requestId}`
  - `/api/ai/requests/{requestId}/retry`
  - `/api/ai/knowledge/rebuild`

## 11. Important Backend Enum/Value Assumptions Confirmed

### Survey
- question types:
  - `SINGLE`
  - `MULTIPLE`
  - `SCALE`
  - `TEXT`
  - `MATRIX`
- questionnaire statuses:
  - `DRAFT`
  - `PUBLISHING`
  - `PUBLISHED`
  - `PUBLISH_FAILED`
  - `REVOKED`
  - `ENDED`
- scope types:
  - `ROLE`
  - `GRADE`
  - `CLASS`
  - `MAJOR`
  - `USER`
- target object types observed:
  - `STUDENT`
  - `IN_SCHOOL_STUDENT`
  - `GRADUATE`
  - `TEACHER`
  - `EMPLOYER`
  - `ALL`

### Improve
- plan/action statuses:
  - `PENDING`
  - `IN_PROGRESS`
  - `COMPLETED`
  - `VERIFIED`

### Report
- project statuses:
  - `DRAFT`
  - `IN_PROGRESS`
  - `COMPLETED`
- chapter statuses:
  - `TODO`
  - `IN_PROGRESS`
  - `COMPLETED`
- assignment statuses:
  - `PENDING`
  - `IN_PROGRESS`
  - `COMPLETED`

### AI
- report operation type:
  - `EXPAND`
  - `POLISH`
- report scenario types in backend:
  - `REPORT_CHAPTER_EXPAND`
  - `REPORT_CHAPTER_POLISH`
- improve scenario type:
  - `IMPROVE_PLAN_SUGGEST`
- report confirm apply mode assumed usable:
  - `REPLACE`
  - `APPEND`

## 12. Known Frontend Risks / Incomplete Areas
- Global auth/user chain is still not fully real:
  - `src/store/user.js` still depends on mock-oriented flow.
  - `src/utils/request.js` still has custom mock adapter.
- D and E pages mostly use new `src/api/http.js`, so page-level backend calls work independently.
- User/assignee selectors in E pages are still manual `userId` inputs because no user-list integration was added in this session.
- Some page source appears as mojibake in terminal output due to encoding, but file edits/build succeeded.
- Report chapter tree add flow currently builds new nodes with `id: null` before save; backend is expected to assign IDs on chapter tree persistence.
- Report assignment save strategy sends collected assignments from currently loaded chapter tree, then appends one new assignment.
- Survey fill page loads published questionnaires and probes fill availability one by one using fill-view API; acceptable for now, but not optimal at scale.

## 13. Build Verification Status
- Frontend build command run successfully:
  - `npm run build`
- Build artifact `frontend/dist` was deleted afterward.
- No temp files intentionally left behind.

## 14. Files Most Relevant For Next Session
- Frontend routing:
  - `../EducationCertificationSystem_fronted/frontend/src/router/index.js`
- E API wrappers:
  - `../EducationCertificationSystem_fronted/frontend/src/api/survey.js`
  - `../EducationCertificationSystem_fronted/frontend/src/api/improve.js`
  - `../EducationCertificationSystem_fronted/frontend/src/api/report.js`
  - `../EducationCertificationSystem_fronted/frontend/src/api/ai.js`
- E views:
  - `../EducationCertificationSystem_fronted/frontend/src/views/pages/SurveyManagementView.vue`
  - `../EducationCertificationSystem_fronted/frontend/src/views/pages/SurveyFillView.vue`
  - `../EducationCertificationSystem_fronted/frontend/src/views/pages/ImprovePlanView.vue`
  - `../EducationCertificationSystem_fronted/frontend/src/views/pages/ReportView.vue`
  - `../EducationCertificationSystem_fronted/frontend/src/views/pages/AiAssistantView.vue`

## 15. Recommended Next Actions For Future AI
1. Start with frontend runtime verification, not code reading only.
2. Run frontend dev server and click through:
   - `/survey`
   - `/survey/fill`
   - `/improve`
   - `/report`
   - `/ai`
3. Check for actual backend data availability issues:
   - empty lists
   - enum mismatch
   - nullable field mismatch
   - date format mismatch
4. Prioritize fixing runtime mismatches in this order:
   - F21 survey management
   - F22 survey fill/statistics
   - F23 improve
   - F24 report
   - F25 ai
5. If user wants deeper completion quality:
   - replace manual `userId` inputs with real user lookup
   - remove mock auth/user chain
   - add better teacher/admin role-specific filtering
   - add file upload flow for report draft upload and improve attachments

## 16. Additional Related Memory Files
- Existing F25 environment/debug log:
  - `ai_read/F25_AI接入与环境排错记录_2026-07-17.md`
- Existing frontend/backend field notes:
  - `ai_read/ai_interface.txt`

## 17. Session End State
- D frontend: integrated and build-verified.
- E frontend: integrated and build-verified.
- Not yet fully runtime click-tested against all real backend data branches.
