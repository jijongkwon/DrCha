package com.ssafy.drcha.account.repository;

import com.ssafy.drcha.account.entity.Account;
import com.ssafy.drcha.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByMember(Member member);
    List<Account> findAllByMember(Member member);
    Optional<Account> findByAccountNumber(String accountNumber);
}
