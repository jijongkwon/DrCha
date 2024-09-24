package com.ssafy.drcha.account.entity;

import com.ssafy.drcha.global.basetime.BaseTimeEntity;
import com.ssafy.drcha.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Account extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "bank_name", nullable = false, length = 50)
    private String bankName;

    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;

    @Column(name = "account_holder_name", nullable = false, length = 100)
    private String accountHolderName;

    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(name = "is_primary", nullable = false)
    private boolean isPrimary;
}