# Testing Guide - Stories & Translation Features

## 🚀 PRE-TEST CHECKLIST

Before testing, ensure:
- [ ] MySQL database is running
- [ ] VitaHealth project builds successfully (`mvn clean compile`)
- [ ] Application starts without errors
- [ ] User is logged in (any role)

---

## 📖 FEATURE 1: Create & View Stories

### Test 1.1: Create a New Story
**Steps:**
1. In Community Feed, locate the "+" circle at the beginning of the stories strip (top of posts area)
2. Click on the "+" circle
3. A file picker dialog should open titled "Select Story Image"
4. Select any image file from your computer (PNG, JPG, JPEG, or GIF)
5. After selection, a dialog appears titled "Create Story" asking for optional caption
6. Type something like: "Great day at the clinic! 🏥"
7. Click "OK"
8. You should see a success message: "Story created successfully!"
9. Verify the story card appears in the stories strip with your name

**Expected Result:** ✅ Story visible immediately with your avatar/name

### Test 1.2: View Story Modal
**Steps:**
1. Click on any story card in the stories strip
2. A large modal dialog opens showing:
   - Your story image (large preview)
   - Your caption text below the image (if you added one)
3. The view counter should increment (check database later)
4. Close the modal

**Expected Result:** ✅ Modal shows full story with image and caption

### Test 1.3: Multiple Stories
**Steps:**
1. Create 3-4 different stories (different images)
2. All should appear in horizontal strip, scrollable left-to-right
3. View each story

**Expected Result:** ✅ All stories visible and viewable

---

## ⏰ FEATURE 2: Story Auto-Expiration (24 Hours)

### Test 2.1: Verify Story Database Schema
**Steps:**
1. Open MySQL client/Workbench
2. Connect to VitaHealth database
3. Run query: `DESC stories;`
4. Verify columns:
   - `id` (INT, AUTO_INCREMENT, PRIMARY KEY)
   - `user_id` (INT, FK to user)
   - `image_url` (VARCHAR 255)
   - `caption` (VARCHAR 500, nullable)
   - `created_at` (TIMESTAMP, DEFAULT CURRENT_TIMESTAMP)
   - `expires_at` (DATETIME)
   - `views` (INT, DEFAULT 0)

**Expected Result:** ✅ All columns present with correct types

### Test 2.2: Verify Expiration Logic
**Steps:**
1. In MySQL, create a test story manually (or find the one you just created):
   ```sql
   INSERT INTO stories (user_id, image_url, caption, expires_at)
   VALUES (1, '/path/to/image.jpg', 'Test', NOW());
   ```
2. This story expires immediately
3. Refresh the VitaHealth Community Feed
4. This test story should NOT appear in stories strip

**Expected Result:** ✅ Expired stories don't appear in UI

### Test 2.3: Verify Active Stories Query
**Steps:**
1. In MySQL, run:
   ```sql
   SELECT id, user_id, caption, expires_at FROM stories WHERE expires_at > NOW();
   ```
2. Should show only non-expired stories
3. Verify story you created earlier is listed

**Expected Result:** ✅ Only non-expired stories in results

---

## 🌐 FEATURE 3: Post Translation

### Test 3.1: Translate to English
**Steps:**
1. Find any forum post in Community Feed
2. Click the "🌐 Translate" button in the post's interaction bar (next to Like, Comment, Share)
3. A modal appears: "Translate Post - Select target language"
4. Dropdown shows: "English (EN)", "Français (FR)", "العربية (AR)"
5. Select "English (EN)" (keep it if already selected)
6. Click "OK"
7. Wait 1-2 seconds for translation API call
8. A new modal appears showing:
   - Original Title (black text)
   - Translated Title (blue text, same as original if already English)
   - Original Content (black text)
   - Translated Content (blue text, same as original if already English)

**Expected Result:** ✅ Translation modal appears with original & translated text

### Test 3.2: Translate to French
**Steps:**
1. Find another post (or same post)
2. Click "🌐 Translate"
3. Select "Français (FR)"
4. Click "OK"
5. Wait for API call
6. Verify:
   - Original text in English (or original language)
   - French translation appears in blue below

**Example (if post title is "Medical Update"):**
- Original Title: "Medical Update"
- Translated Title: "Mise à jour médicale"

**Expected Result:** ✅ French translation appears correctly

### Test 3.3: Translate to Arabic
**Steps:**
1. Click "🌐 Translate" on a post
2. Select "العربية (AR)"
3. Click "OK"
4. Wait 1-2 seconds
5. Verify Arabic text appears in blue (right-to-left if your system supports it)

**Expected Result:** ✅ Arabic translation displays

### Test 3.4: Translation Error Handling
**Steps:**
1. Disconnect internet (or block API in firewall temporarily)
2. Click "🌐 Translate" on a post
3. Select language and OK
4. Should see error message after timeout (10 seconds max)
5. Reconnect internet

**Expected Result:** ✅ Graceful error handling with user message

---

## ✅ FEATURE 4: Post Approval Workflow

### Test 4.1: Create New Post (Shows PENDING_REVIEW)
**Steps (as Regular User):**
1. Click "Create Post" or "New Entry" button
2. Fill title: "Dental Checkup Tips"
3. Fill content: "Regular checkups are important for oral health"
4. Click "SUBMIT FOR REVIEW"
5. See message: "Entry added for review successfully!"
6. Look at posts in feed - your post should appear with status badge showing "PENDING_REVIEW" or "Under Review"

**Expected Result:** ✅ New post shows PENDING_REVIEW status

### Test 4.2: Admin Approves Post
**Steps (as Admin User):**
1. Login as admin user (or open new session with admin account)
2. In Community Feed, find the PENDING_REVIEW post
3. Click "Approve" or "Publish" button on that post
4. See success message
5. Verify post status changes to "PUBLISHED"

**Expected Result:** ✅ Post status changes to PUBLISHED and is visible to all

### Test 4.3: Verify Posted Status in Database
**Steps:**
1. Open MySQL client
2. Run: `SELECT id, title, status FROM forum_posts ORDER BY id DESC LIMIT 5;`
3. Verify your test post shows `status = 'PUBLISHED'`

**Expected Result:** ✅ Database reflects correct status

---

## 🎨 FEATURE 5: UI/Navigation

### Test 5.1: Stories Strip Displays Correctly
**Steps:**
1. Open Community Feed
2. Stories strip should be at top of posts, before first post
3. Verify:
   - "+" (Create Story) button is first
   - Existing stories appear after it
   - Strip is horizontally scrollable if many stories

**Expected Result:** ✅ Stories strip visible and properly positioned

### Test 5.2: Translate Button on All Posts
**Steps:**
1. Scroll through Community Feed posts
2. Each post should have in its interaction bar:
   - "Like X" button
   - "Comment X" button
   - "Share X" button
   - "🌐 Translate" button (NEW)
3. Translate button should have same styling as other buttons

**Expected Result:** ✅ Translate button visible on all posts

---

## 📊 COMPLETE TEST RESULT MATRIX

| Feature | Test Case | Expected | Status |
|---------|-----------|----------|--------|
| Stories | Create story | Story appears in feed | [ ] ✅ / ⚠️ / ❌ |
| Stories | View modal | Image + caption display | [ ] ✅ / ⚠️ / ❌ |
| Stories | Multiple stories | All visible, scrollable | [ ] ✅ / ⚠️ / ❌ |
| Expiration | Database schema | Correct columns & types | [ ] ✅ / ⚠️ / ❌ |
| Expiration | Expired story | Doesn't appear in feed | [ ] ✅ / ⚠️ / ❌ |
| Expiration | Query filter | Only non-expired shown | [ ] ✅ / ⚠️ / ❌ |
| Translation | EN translation | English version appears | [ ] ✅ / ⚠️ / ❌ |
| Translation | FR translation | French version appears | [ ] ✅ / ⚠️ / ❌ |
| Translation | AR translation | Arabic version appears | [ ] ✅ / ⚠️ / ❌ |
| Translation | Error handling | Graceful error display | [ ] ✅ / ⚠️ / ❌ |
| Workflow | New post status | PENDING_REVIEW shown | [ ] ✅ / ⚠️ / ❌ |
| Workflow | Admin approval | Status changes to PUBLISHED | [ ] ✅ / ⚠️ / ❌ |
| Workflow | DB status | Correct value in database | [ ] ✅ / ⚠️ / ❌ |
| UI | Stories position | Top of feed, before posts | [ ] ✅ / ⚠️ / ❌ |
| UI | Translate button | Visible on all posts | [ ] ✅ / ⚠️ / ❌ |

---

## 🐛 TROUBLESHOOTING

### Issue: "+" button doesn't work
**Solution:**
- Check browser console for errors
- Verify `CommunityFeedController` is loaded
- Verify FXML has `fx:id="createStoryBox"` with `onMouseClicked="#openCreateStoryDialog"`

### Issue: Stories not appearing in feed
**Solution:**
- Check database: `SELECT COUNT(*) FROM stories WHERE expires_at > NOW();`
- Verify `loadStories()` is called in `initialize()`
- Check console for database connection errors

### Issue: Translation button does nothing
**Solution:**
- Check internet connection (uses external MyMemory API)
- Try different post with different language
- Check browser console for errors
- Verify `TranslationService` class exists at: `tn/esprit/workshopjdbc/Services/TranslationService.java`

### Issue: Translation returns same text
**Solution:**
- This is normal if post is already in target language
- Try translating post with different source language
- Check API response in debug logs

---

## 📝 NOTES

- **Story file size**: No limit enforced in code; add file size validation if needed
- **Translation speed**: Depends on internet speed (typically 1-2 seconds)
- **Story expiration**: Set to exactly 24 hours from creation; can be modified in `Story.java`
- **Supported languages**: EN, FR, AR (easily expandable in `CommunityFeedController.java`)

---

**Good luck testing! Report any issues with exact error messages and steps to reproduce.**
