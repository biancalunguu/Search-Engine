CREATE DATABASE IF NOT EXISTS search_engine
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE search_engine;

CREATE TABLE IF NOT EXISTS files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_path VARCHAR(1024) NOT NULL,
    file_name VARCHAR(255)  NOT NULL,
    extension VARCHAR(50),
    size_bytes BIGINT,
    last_modified DATETIME,
    is_text_file BOOLEAN DEFAULT FALSE,
    content LONGTEXT,
    preview TEXT,
    indexed_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    path_score DOUBLE DEFAULT 0,

    UNIQUE KEY uq_file_path (file_path(512)),
    FULLTEXT KEY ft_search (file_name, content)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
