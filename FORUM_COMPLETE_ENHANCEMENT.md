# Forum Module - Complete Enhancement Summary
## CRUD Operations + Content Moderation + AI Chatbot + Auto Tag Generation

## ✅ All Features Completed

### 1. **Full CRUD Operations** 
Posts and Comments now have complete Create, Read, Update, Delete functionality with proper authorization checks.

#### Posts CRUD:
- ✅ **Create**: Publish posts with title, content, images, videos, and auto-generated tags
- ✅ **Read**: View all posts with filtering, search, and pagination
- ✅ **Update**: Edit post details (title, content, media) - author or admin only
- ✅ **Delete**: Soft-delete posts - author or admin only

#### Comments CRUD:
- ✅ **Create**: Add comments to posts with moderation
- ✅ **Read**: View all comments with threaded replies
- ✅ **Update**: Edit comment content - author or admin only
- ✅ **Delete**: Soft-delete comments - author or admin only

---

### 2. **Content Moderation with Groq API**
Real-time content moderation using Groq's Mixtral model.

**GroqContentModerationService.java**
- Detects explicit words locally (fuck, shit, asshole, damn, crap, piss, bitch, bastard, hell)
- Uses Groq API for deeper content analysis
- Returns moderation results with:
  - `isClean`: Boolean indicating if content is acceptable
  - `flaggedWords`: List of detected inappropriate words
  - `reason`: Explanation of why content was flagged
  - `aiAnalysis`: Detailed AI analysis

**Features:**
- Quick local word filtering (instant response)
- Fallback to Groq API for sophisticated analysis
- Checks for hate speech, discrimination, violence, harmful instructions
- Detects spam and commercial content
- Identifies medical misinformation
- Flags dangerous medical advice

**Implementation:**
- Posts are automatically moderated on creation
- Comments are automatically moderated on creation
- Users see a modal alert with details about flagged words
- Content with "PUBLISHED" status passes moderation
- Content with issues goes to "PENDING_REVIEW" status

---

### 3. **Automatic Tag Generation with Groq API**
Intelligently generates tags for posts based on content.

**GroqTagGenerationService.java**
- Local keyword matching for common medical terms
- Groq API integration for intelligent categorization
- Fallback to "General" if no tag can be determined

**Features:**
- Recognizes 30+ medical keywords (diabetes, cardiology, oncology, etc.)
- Auto-detects post types: Question, Advice, Experience, Research, Urgent, Treatment, etc.
- Generates 1-5 tags per post
- Considers both title and content
- Timeout protection (5-second max response time)

**Tag Generation Logic:**
1. Check post content for medical keywords
2. Detect common patterns (question, emergency, medication, etc.)
3. If no match, use Groq API for semantic analysis
4. Default to "General" if all methods fail

---

### 4. **AI Chatbot with Groq API**
Interactive chatbot to assist users in the forum.

**GroqChatbotService.java**
- Maintains conversation history across interactions
- Provides context-aware responses
- Integrates content moderation for user messages
- Quick replies for common intents

**Quick Replies** (No API call needed):
- "Hello"/"Hi" → Welcome greeting
- "Help" → List of chatbot capabilities
- "Emergency" → Urgent care warning
- "Thanks"/"Thank you" → Acknowledgment

**Features:**
- Multi-turn conversation support
- Conversation history management (capped at 20 messages)
- Content moderation on all user input
- Shows moderation alerts for inappropriate messages
- 10-second timeout protection
- Fallback behavior on API errors
- System context emphasizes medical limitations

**Conversation Management:**
- Maintains system message and recent history
- Automatically prunes old messages to manage memory
- Reset conversation functionality available

---

### 5. **UI/UX Enhancements**

#### CommunityFeed.fxml
- ✅ Added chatbot panel on right sidebar
- ✅ Integrated send message functionality
- ✅ Added chat history display
- ✅ Clear chat button to reset conversation
- ✅ Posts display with edit/delete buttons (for authors/admins)
- ✅ Media display support (images with placeholder fallback)

#### ForumView.fxml
- ✅ Edit and delete buttons for posts
- ✅ Edit and delete buttons for comments
- ✅ Upload image button with file chooser
- ✅ Upload video button with file chooser
- ✅ Upload status label for feedback
- ✅ All CRUD operations integrated

#### CommunityFeedController.java
- ✅ Chatbot initialization and message handling
- ✅ Send message functionality with threading
- ✅ Moderation alert display
- ✅ Chat message UI rendering
- ✅ Clear chat functionality
- ✅ Post edit/delete handlers
- ✅ Integration with ForumService

#### ForumController.java
- ✅ Post creation with moderation checks
- ✅ Comment creation with moderation checks
- ✅ Moderation alert modals
- ✅ File upload handlers (image/video)
- ✅ Full CRUD button handlers
- ✅ Edit/delete confirmation dialogs

---

### 6. **Service Layer Integration**

#### ForumService.java (Enhanced)
```java
// New methods:
- updatePost(): Update post with authorization check
- deletePostByAuthor(): Delete post with authorization
- updateComment(): Update comment with re-moderation
- deleteComment(): Delete comment with authorization
- getPost(id): Retrieve specific post
- getComment(id): Retrieve specific comment

// Enhanced methods:
- createPost(): Now auto-generates tags and moderates content
- addComment(): Now moderates comment content
```

#### New Services:
1. **GroqContentModerationService**
   - Static methods for content analysis
   - ModerationResult data class
   - Word flagging and AI analysis

2. **GroqTagGenerationService**
   - Tag generation from content
   - Multiple tag generation
   - Medical keyword recognition

3. **GroqChatbotService**
   - Chat message handling
   - Conversation history management
   - Quick reply system
   - ChatbotResponse and ChatMessage classes

4. **FileUploadService** (Already created in previous update)
   - Image upload (5MB max)
   - Video upload (50MB max)
   - File validation and secure naming

---

### 7. **Database Changes**
No schema changes required - existing forum tables support all features:
- `forum_posts`: Already has imageUrl, videoUrl, tag, status columns
- `forum_comments`: Status column supports moderation states
- Both tables have created_at/updated_at for tracking

---

### 8. **API Integration**

**Groq API Details:**
- **API Key**: Set via environment variable `GROQ_API_KEY` (see Configuration section)
- **Model**: mixtral-8x7b-32768 (Open-source, fast, cost-effective)
- **Endpoint**: https://api.groq.com/openai/v1/chat/completions
- **HTTP Client**: Java 11+ built-in HttpClient
- **Timeouts**: 5-10 seconds per request with fallbacks

---

### 9. **Error Handling & Fallbacks**
- Content moderation fails gracefully (allows content if API unavailable)
- Tag generation defaults to "General" on API errors
- Chatbot continues with error message on API failure
- File uploads validated before transfer
- All services have exception logging

---

### 10. **Security Features**
- Authorization checks on all modifications
- Admin override capability
- Soft deletes preserve data
- File validation (type, size, naming)
- Moderation re-evaluation on updates
- User message content moderation before chatbot processing
- Input sanitization for JSON in API calls

---

## 🚀 Usage Guide

### For End Users:

**Creating Posts with Media:**
1. Fill title and content
2. Click "🖼️ Upload Image" or "🎥 Upload Video" (optional)
3. Tag auto-generates based on content
4. Post creation automatically checks moderation
5. If inappropriate content detected, see alert with specific words

**Using the Chatbot:**
1. Type question in chat input box
2. Press Enter or click "Send"
3. Message is moderated before sending
4. Assistant responds with context from conversation history
5. View conversation history in scrollable panel
6. Click "Clear Chat" to reset and start fresh

**Editing Posts:**
1. Click "✏️ Edit" button on your post (visible only if you're author/admin)
2. Update title and content in dialog
3. Click OK to save changes

**Deleting Posts:**
1. Click "🗑️ Delete" button on your post
2. Confirm in dialog
3. Post is soft-deleted (preserved in database)

**Commenting with Moderation:**
1. Type comment in comment area
2. If inappropriate words detected, see warning modal
3. Modify comment and try again
4. Comment appears in list once approved

---

## 📁 File Changes Summary

### New Files:
- `GroqContentModerationService.java`
- `GroqTagGenerationService.java`
- `GroqChatbotService.java`

### Modified Files:
- `ForumService.java` - Added CRUD methods, moderation, tag generation
- `ForumController.java` - Added UI handlers, moderation alerts
- `ForumPostDAO.java` - Added update, delete, findById
- `ForumCommentDAO.java` - Added update, delete, findById
- `ForumComment.java` - Added updatedAt field
- `CommunityFeed.fxml` - Added chatbot UI panel
- `CommunityFeedController.java` - Added chatbot handlers, edit/delete
- `ForumView.fxml` - Already had edit/delete/upload buttons

---

## 🧪 Testing Checklist

- [ ] Create post with images/videos
- [ ] Create post with profanity - see moderation alert
- [ ] Add comment with bad words - see warning
- [ ] Edit your own post
- [ ] Try to edit someone else's post - should fail
- [ ] Delete your own post
- [ ] Admin can edit/delete any post
- [ ] Chatbot responds to greetings
- [ ] Chatbot rejects profane messages
- [ ] Auto-generated tags match content
- [ ] File uploads validate type and size
- [ ] Media displays in post feed
- [ ] Chat history persists across messages
- [ ] Clear chat resets conversation
- [ ] Tag generation includes multiple relevant tags

---

## 🔧 Configuration

**To set your Groq API key:**
1. Set the environment variable `GROQ_API_KEY` with your actual Groq API key
   - **On Windows (CMD)**: `set GROQ_API_KEY=your_api_key_here`
   - **On Windows (PowerShell)**: `$env:GROQ_API_KEY="your_api_key_here"`
   - **On Linux/Mac**: `export GROQ_API_KEY=your_api_key_here`
   - **In IDE**: Add VM option `-DGROQ_API_KEY=your_api_key_here`

**To adjust moderation strictness:**
- Add/remove words from `EXPLICIT_WORDS` list
- Modify Groq prompt instructions
- Change temperature/max_tokens in API calls

**To adjust file upload limits:**
- Modify in `FileUploadService.java`:
   - `MAX_IMAGE_SIZE` (default 5MB)
   - `MAX_VIDEO_SIZE` (default 50MB)
   - `ALLOWED_IMAGE_EXTENSIONS`
   - `ALLOWED_VIDEO_EXTENSIONS`

---

## 📊 Performance Notes

- Local word filtering is instant (milliseconds)
- Groq API calls typically take 2-5 seconds
- With timeouts, worst case is 10 seconds
- Conversation history capped at 20 messages to manage memory
- File uploads handled asynchronously in separate threads
- Content moderation runs synchronously but has graceful fallbacks

---

## 🎯 Future Enhancements

- Email notifications for post moderation decisions
- Batch moderation for bulk post review
- Custom moderation rules per category
- Translation of posts using Groq API
- Advanced analytics on moderation patterns
- User reputation system
- Pin important posts/comments
- Report moderation decisions to users
- Multi-language chatbot support
- Voice chat integration

