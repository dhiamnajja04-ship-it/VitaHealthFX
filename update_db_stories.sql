-- ============================================
-- VitaHealth Database Update Script
-- Add Stories Table
-- Date: 2026-05-11
-- ============================================

-- Create stories table for social media stories functionality
CREATE TABLE IF NOT EXISTS `stories` (
    `id` INT PRIMARY KEY AUTO_INCREMENT,
    `user_id` INT NOT NULL,
    `image_url` VARCHAR(255) NOT NULL,
    `caption` VARCHAR(500),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `expires_at` DATETIME NOT NULL,
    `views` INT DEFAULT 0,
    
    -- Foreign Key constraint
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE,
    
    -- Indexes for better query performance
    INDEX `idx_expires_at` (`expires_at`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- Verify table creation
-- ============================================
SELECT 'Stories table created successfully' AS status;

-- Display table structure
DESCRIBE `stories`;
