package com.krishna.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.krishna.domain.EmailNotification;

public interface MailnotificationRepository extends JpaRepository<EmailNotification,Long>{

}
