# Forum Module - Full CRUD Implementation & File Upload

## Changes Summary

### 1. **Database Access Layer (DAO) - ForumPostDAO**
- **Added `update(ForumPost post)` method**: Allows updating post title, content, image URL, video URL, and tags
- **Added `delete(int postId)` method**: Soft-deletes posts by marking status as 'DELETED'
- **Added `findById(int postId)` method**: Retrieves a specific post with all details

### 2. **Database Access Layer (DAO) - ForumCommentDAO**
- **Added `update(ForumComment comment)` method**: Allows updating comment content with moderation status re-evaluation
- **Added `delete(int commentId)` method**: Soft-deletes comments by marking status as 'DELETED'
- **Added `findById(int commentId)` method**: Retrieves a specific comment with all details
- **Updated `mapResultSet()` method**: Now includes `updated_at` field mapping

### 3. **Entity Enhancement - ForumComment**
- **Added `updatedAt` field**: Tracks when comments are last modified
- **Added getters/setters** for the new field

### 4. **Service Layer - ForumService**
New CRUD methods:
- **`updatePost()`**: Update post details with ownership verification
- **`deletePostByAuthor()`**: Delete post with authorization check
- **`getPost(int postId)`**: Retrieve single post
- **`updateComment()`**: Update comment content with moderation
- **`deleteComment()`**: Delete comment with authorization
- **`getComment(int commentId)`**: Retrieve single comment

All methods verify ownership (author or admin) before allowing modifications.

### 5. **File Upload Service - FileUploadService (NEW)**
Comprehensive file handling:
- **`uploadImage(File)`**: Upload images (jpg, png, gif, webp)
  - Max size: 5 MB
  - Secure filename generation with timestamp
  
- **`uploadVideo(File)`**: Upload videos (mp4, avi, mov, mkv, webm)
  - Max size: 50 MB
  - Secure filename generation with timestamp

- **`deleteFile(String filePath)`**: Remove uploaded files
- **`fileExists(String filePath)`**: Verify file existence
- **Directory management**: Automatic creation of upload directories

Files uploaded to: `forum_uploads/images/` and `forum_uploads/videos/`

### 6. **UI Controller - ForumController**
Enhanced with:
- **Post Management**:
  - Edit button (visible only for post author or admin)
  - Delete button (visible only for post author or admin)
  - Edit dialog for updating post title and content
  - Confirmation dialog for deletion

- **Comment Management**:
  - Edit comment method with authorization check
  - Delete comment method with confirmation
  - Comments list now tracks selected comment

- **File Upload**:
  - Upload Image button with file chooser
  - Upload Video button with file chooser
  - Upload status label showing success/failure
  - File validation with error messages
  - Image and video paths stored for post creation

- **Authorization**:
  - Edit/delete buttons automatically enabled/disabled based on ownership
  - Admin can edit/delete any post or comment
  - Regular users can only modify their own content

### 7. **UI Views - ForumView.fxml**
Updated with:
- Edit/Delete buttons for posts in the post view section
- Edit/Delete buttons for comments in the comments section
- Upload Image button in post creation section
- Upload Video button in post creation section
- Upload status label to show upload results

## Features Implemented

### ✅ Full Post CRUD
- **Create**: With title, content, language, category (already existed)
- **Read**: Search, filter, and view posts with details
- **Update**: Edit post title, content, images, and videos
- **Delete**: Soft-delete posts (mark as DELETED)

### ✅ Full Comment CRUD
- **Create**: Add comments to posts (already existed)
- **Read**: View all comments for a post
- **Update**: Edit comment content with re-moderation
- **Delete**: Soft-delete comments (mark as DELETED)

### ✅ Image & Video Upload
- **Image Upload**: jpg, jpeg, png, gif, webp (max 5MB)
- **Video Upload**: mp4, avi, mov, mkv, webm (max 50MB)
- **File Organization**: Organized in separate directories
- **Secure Naming**: Timestamp + random number to prevent conflicts
- **Validation**: Extension and size checking

### ✅ Authorization & Security
- Ownership verification on all modifications
- Admin override capability
- Soft deletes (data preserved in database)
- Moderation re-evaluation on comment updates

## Usage Guide

### Creating a Post with Media
1. Fill in title and content
2. Click "🖼️ Upload Image" or "🎥 Upload Video" (optional)
3. Select file from your computer
4. See confirmation in status label
5. Click "Publish Discussion"

### Editing a Post
1. Select the post from the list
2. Click "✏️ Edit" (only visible if you're the author or admin)
3. Update title and content
4. Click OK to save changes

### Deleting a Post
1. Select the post from the list
2. Click "🗑️ Delete" (only visible if you're the author or admin)
3. Confirm deletion in the dialog

### Editing a Comment
1. Select the comment from the list
2. Click "✏️ Edit"
3. Update the comment content
4. Click OK to save

### Deleting a Comment
1. Select the comment from the list
2. Click "🗑️ Delete"
3. Confirm deletion

## File Structure
```
src/main/java/tn/esprit/workshopjdbc/
├── Entities/
│   ├── ForumPost.java (unchanged - already had imageUrl, videoUrl)
│   ├── ForumComment.java (enhanced with updatedAt)
├── Services/
│   ├── ForumService.java (added CRUD methods)
│   └── FileUploadService.java (NEW - handles file uploads)
├── dao/
│   ├── ForumPostDAO.java (added update, delete, findById)
│   └── ForumCommentDAO.java (added update, delete, findById)
└── Controllers/
    └── ForumController.java (full CRUD UI integration)

src/main/resources/fxml/forum/
└── ForumView.fxml (updated with edit/delete buttons, upload buttons)

forum_uploads/ (created at runtime)
├── images/ (uploaded images stored here)
└── videos/ (uploaded videos stored here)
```

## Testing Checklist
- [ ] Create a post with image/video
- [ ] Edit your own post
- [ ] Delete your own post
- [ ] Add a comment
- [ ] Edit your own comment
- [ ] Delete your own comment
- [ ] Verify admin can edit/delete any post/comment
- [ ] Verify non-authors cannot edit/delete posts/comments
- [ ] Test file upload validation (size and type limits)

## Security Notes
1. All operations check user ownership before allowing modifications
2. Admins can override and manage any content
3. Files are validated before upload (extension and size)
4. Soft deletes preserve data for audit trails
5. Updated timestamps track when modifications occur
