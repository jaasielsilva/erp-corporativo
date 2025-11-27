-- Create chat rooms table
CREATE TABLE IF NOT EXISTS `chat_rooms` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `type` VARCHAR(20) NOT NULL,
  `created_by` BIGINT NOT NULL,
  `created_at` DATETIME NOT NULL,
  `active` TINYINT(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create chat messages table
CREATE TABLE IF NOT EXISTS `chat_messages` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `room_id` BIGINT NOT NULL,
  `sender_id` BIGINT NOT NULL,
  `content` TEXT NOT NULL,
  `type` VARCHAR(20) NOT NULL,
  `sent_at` DATETIME NOT NULL,
  `read` TINYINT(1) NOT NULL DEFAULT 0,
  `file_url` VARCHAR(255) NULL,
  `file_name` VARCHAR(255) NULL,
  `file_size` BIGINT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_chat_messages_room_sent` (`room_id`, `sent_at`),
  KEY `idx_chat_messages_unread_count` (`room_id`, `read`, `sender_id`),
  CONSTRAINT `fk_chat_messages_room` FOREIGN KEY (`room_id`) REFERENCES `chat_rooms` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_chat_messages_sender` FOREIGN KEY (`sender_id`) REFERENCES `usuarios` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
