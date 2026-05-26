package com.picbed.repository;

import com.picbed.entity.EmailDomain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailDomainRepository extends JpaRepository<EmailDomain, Long> {
}
