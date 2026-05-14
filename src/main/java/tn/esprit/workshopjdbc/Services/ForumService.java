package tn.esprit.workshopjdbc.Services;

import tn.esprit.workshopjdbc.Entities.ForumCategory;
import tn.esprit.workshopjdbc.Entities.ForumComment;
import tn.esprit.workshopjdbc.Entities.ForumPost;
import tn.esprit.workshopjdbc.Entities.ForumReport;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.dao.ForumCategoryDAO;
import tn.esprit.workshopjdbc.dao.ForumCommentDAO;
import tn.esprit.workshopjdbc.dao.ForumPostDAO;
import tn.esprit.workshopjdbc.dao.ForumReportDAO;
import tn.esprit.workshopjdbc.dao.ForumSchemaInitializer;

import java.util.List;

public class ForumService {
    private final ForumCategoryDAO categoryDAO = new ForumCategoryDAO();
    private final ForumPostDAO postDAO = new ForumPostDAO();
    private final ForumCommentDAO commentDAO = new ForumCommentDAO();
    private final ForumReportDAO reportDAO = new ForumReportDAO();
    private final ForumModerationService moderationService = new ForumModerationService();

    public List<ForumCategory> getCategories() {
        return categoryDAO.findAll();
    }

    public List<ForumPost> searchPosts(String keyword, ForumCategory category, User currentUser) {
        boolean includePending = currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
        Integer categoryId = category != null && category.getId() > 0 ? category.getId() : null;
        return postDAO.search(keyword, categoryId, includePending);
    }

    public boolean createPost(User author, ForumCategory category, String title, String content, String language) {
        return createPost(author, category, title, content, language, null, null, null);
    }

    public boolean createPost(User author, ForumCategory category, String title, String content, String language,
                              String imageUrl, String videoUrl, String tag) {
        if (author == null || category == null || isBlank(title) || isBlank(content)) {
            return false;
        }

        // Check content moderation - reject if inappropriate
        GroqContentModerationService.ModerationResult moderation = GroqContentModerationService.moderateContent(title + " " + content);
        if (!moderation.isClean) {
            throw new IllegalArgumentException("Inappropriate content detected: " + moderation.reason);
        }

        ForumPost post = new ForumPost();
        post.setAuthorId(author.getId());
        post.setCategoryId(category.getId());
        post.setTitle(title.trim());
        post.setContent(content.trim());
        post.setLanguage(isBlank(language) ? "fr" : language.trim().toLowerCase());
        post.setStatus("PENDING_REVIEW");
        post.setImageUrl(isBlank(imageUrl) ? null : imageUrl.trim());
        post.setVideoUrl(isBlank(videoUrl) ? null : videoUrl.trim());
        
        // Always auto-generate tag using Groq
        post.setTag(GroqTagGenerationService.generateTag(title, content));
        
        return postDAO.create(post);
    }

    public boolean addComment(User author, ForumPost post, String content) {
        if (author == null || post == null || isBlank(content)) {
            return false;
        }

        // Check content moderation - reject if inappropriate
        GroqContentModerationService.ModerationResult moderation = GroqContentModerationService.moderateContent(content);
        if (!moderation.isClean) {
            throw new IllegalArgumentException("Inappropriate content detected: " + moderation.reason);
        }

        ForumComment comment = new ForumComment();
        comment.setAuthorId(author.getId());
        comment.setPostId(post.getId());
        comment.setContent(content.trim());
        comment.setStatus("PUBLISHED");
        return commentDAO.create(comment);
    }

    public List<ForumComment> getComments(ForumPost post) {
        if (post == null) return List.of();
        return commentDAO.findByPost(post.getId());
    }

    public boolean markUseful(ForumPost post) {
        return post != null && postDAO.incrementUsefulCount(post.getId());
    }

    public boolean reportPost(User reporter, ForumPost post, String reason) {
        if (reporter == null || post == null || isBlank(reason)) {
            return false;
        }

        ForumReport report = new ForumReport();
        report.setPostId(post.getId());
        report.setReporterId(reporter.getId());
        report.setReason(reason.trim());
        report.setStatus("OPEN");
        return reportDAO.create(report);
    }

    public List<ForumPost> getModerationQueue(User moderator) {
        if (!isAdmin(moderator)) return List.of();
        return postDAO.findModerationQueue();
    }

    public boolean publishPost(User moderator, ForumPost post) {
        if (!isAdmin(moderator) || post == null) return false;
        boolean updated = postDAO.updateStatus(post.getId(), "PUBLISHED");
        if (updated) reportDAO.closeReportsForPost(post.getId());
        return updated;
    }

    public boolean hidePost(User moderator, ForumPost post) {
        if (!isAdmin(moderator) || post == null) return false;
        boolean updated = postDAO.updateStatus(post.getId(), "HIDDEN");
        if (updated) reportDAO.closeReportsForPost(post.getId());
        return updated;
    }

    public boolean lockPost(User moderator, ForumPost post) {
        if (!isAdmin(moderator) || post == null) return false;
        boolean updated = postDAO.updateStatus(post.getId(), "LOCKED");
        if (updated) reportDAO.closeReportsForPost(post.getId());
        return updated;
    }

    public boolean deletePost(User moderator, ForumPost post) {
        if (!isAdmin(moderator) || post == null) return false;
        boolean updated = postDAO.updateStatus(post.getId(), "DELETED");
        if (updated) reportDAO.closeReportsForPost(post.getId());
        return updated;
    }

    public boolean updatePost(User author, ForumPost post, String title, String content, String imageUrl, String videoUrl, String tag) {
        if (author == null || post == null || isBlank(title) || isBlank(content)) {
            return false;
        }
        // Verify ownership - only the author can edit their post (or admin)
        if (post.getAuthorId() != author.getId() && !"ADMIN".equalsIgnoreCase(author.getRole())) {
            return false;
        }

        post.setTitle(title.trim());
        post.setContent(content.trim());
        post.setImageUrl(isBlank(imageUrl) ? null : imageUrl.trim());
        post.setVideoUrl(isBlank(videoUrl) ? null : videoUrl.trim());
        post.setTag(isBlank(tag) ? null : tag.trim());
        return postDAO.update(post);
    }

    public boolean deletePostByAuthor(User author, ForumPost post) {
        if (author == null || post == null) {
            return false;
        }
        // Verify ownership - only the author can delete their post (or admin)
        if (post.getAuthorId() != author.getId() && !"ADMIN".equalsIgnoreCase(author.getRole())) {
            return false;
        }
        return postDAO.delete(post.getId());
    }

    public ForumPost getPost(int postId) {
        return postDAO.findById(postId);
    }

    public boolean updateComment(User author, ForumComment comment, String content) {
        if (author == null || comment == null || isBlank(content)) {
            return false;
        }
        // Verify ownership - only the author can edit their comment (or admin)
        if (comment.getAuthorId() != author.getId() && !"ADMIN".equalsIgnoreCase(author.getRole())) {
            return false;
        }

        comment.setContent(content.trim());
        comment.setStatus(moderationService.evaluateStatus("", content));
        return commentDAO.update(comment);
    }

    public boolean deleteComment(User author, ForumComment comment) {
        if (author == null || comment == null) {
            return false;
        }
        // Verify ownership - only the author can delete their comment (or admin)
        if (comment.getAuthorId() != author.getId() && !"ADMIN".equalsIgnoreCase(author.getRole())) {
            return false;
        }
        return commentDAO.delete(comment.getId());
    }

    public ForumComment getComment(int commentId) {
        return commentDAO.findById(commentId);
    }

    public String translatePreview(ForumPost post, String targetLanguage) {
        if (post == null) return "";
        String language = isBlank(targetLanguage) ? "en" : targetLanguage.toLowerCase();
        return "[Traduction automatique vers " + language + " - simulation]\n\n" + post.getContent();
    }

    public boolean toggleLike(User user, ForumPost post) {
        if (user == null || post == null) return false;
        ForumSchemaInitializer.ensureSchema();
        return postDAO.toggleLike(post.getId(), user.getId());
    }

    public boolean incrementShareCount(ForumPost post) {
        if (post == null) return false;
        ForumSchemaInitializer.ensureSchema();
        return postDAO.incrementShareCount(post.getId());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isAdmin(User user) {
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
}
