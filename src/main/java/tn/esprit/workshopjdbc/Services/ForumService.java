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
        if (author == null || category == null || isBlank(title) || isBlank(content)) {
            return false;
        }

        ForumPost post = new ForumPost();
        post.setAuthorId(author.getId());
        post.setCategoryId(category.getId());
        post.setTitle(title.trim());
        post.setContent(content.trim());
        post.setLanguage(isBlank(language) ? "fr" : language.trim().toLowerCase());
        post.setStatus(moderationService.evaluateStatus(title, content));
        return postDAO.create(post);
    }

    public boolean addComment(User author, ForumPost post, String content) {
        if (author == null || post == null || isBlank(content)) {
            return false;
        }

        ForumComment comment = new ForumComment();
        comment.setAuthorId(author.getId());
        comment.setPostId(post.getId());
        comment.setContent(content.trim());
        comment.setStatus(moderationService.evaluateStatus("", content));
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

    public String translatePreview(ForumPost post, String targetLanguage) {
        if (post == null) return "";
        String language = isBlank(targetLanguage) ? "en" : targetLanguage.toLowerCase();
        return "[Traduction automatique vers " + language + " - simulation]\n\n" + post.getContent();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isAdmin(User user) {
        return user != null && "ADMIN".equalsIgnoreCase(user.getRole());
    }
}
