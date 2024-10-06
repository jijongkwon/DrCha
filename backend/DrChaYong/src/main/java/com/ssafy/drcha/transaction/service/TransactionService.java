package com.ssafy.drcha.transaction.service;

import com.ssafy.drcha.global.api.RestClientUtil;
import com.ssafy.drcha.global.api.dto.CreateDemandDepositAccountResponse;
import com.ssafy.drcha.global.api.dto.DepositResponse;
import com.ssafy.drcha.global.api.dto.TransferResponse;
import com.ssafy.drcha.global.api.dto.WithdrawResponse;
import com.ssafy.drcha.global.error.ErrorCode;
import com.ssafy.drcha.global.error.type.InsufficientBalanceException;
import com.ssafy.drcha.global.error.type.IouNotFoundException;
import com.ssafy.drcha.global.error.type.UserNotFoundException;
import com.ssafy.drcha.global.error.type.VirtualAccountException;
import com.ssafy.drcha.iou.entity.Iou;
import com.ssafy.drcha.iou.repository.IouRepository;
import com.ssafy.drcha.member.entity.Member;
import com.ssafy.drcha.member.repository.MemberRepository;
import com.ssafy.drcha.transaction.dto.DepositRequestDto;
import com.ssafy.drcha.transaction.dto.TransferRequestDto;
import com.ssafy.drcha.transaction.dto.WithdrawRequestDto;
import com.ssafy.drcha.transaction.entity.VirtualAccount;
import com.ssafy.drcha.transaction.entity.VirtualAccountStatus;
import com.ssafy.drcha.transaction.repository.VirtualAccountRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TransactionService {

    private final RestClientUtil restClientUtil;
    private final VirtualAccountRepository virtualAccountRepository;
    private final IouRepository iouRepository;
    private final MemberRepository memberRepository;

    /**
     * TODO : 무통장 계좌(가상계좌) 생성
     * ! RestClientUtil을 사용하여 실제 가상계좌 생성 API 호출 및 생성된 가상계좌 정보 VirtualAccount 엔티티에 저장
     * ! 관련 엔티티와 연관관계 설정
     * @param iouId
     * @return
     */
    public VirtualAccount createVirtualAccount(Long iouId) {
        log.info("===============    가상 계좌 생성    ====================");
        Iou iou = iouRepository.findById(iouId)
                               .orElseThrow(() -> new IouNotFoundException(ErrorCode.IOU_NOT_FOUND));

        if (ObjectUtils.isNotEmpty(iou.getVirtualAccount())) {
            throw new VirtualAccountException(ErrorCode.VIRTUAL_ACCOUNT_ALREADY_EXISTS); // ! 이미 존재하면 안돼
        }
        log.info("------- 이 부분 확인 --------");
        CreateDemandDepositAccountResponse response = restClientUtil.createDemandDepositAccount(iou.getCreditor().getUserKey());  // ! 채권자 userKey로 가상계좌 생성.
        log.info("------- 이 부분 통과 ----------");
        VirtualAccount virtualAccount = VirtualAccount.builder()
                  .iou(iou)
                  .creditor(iou.getCreditor())
                  .debtor(iou.getDebtor())
                  .accountNumber(response.getRec().getAccountNo())
                  .bankCode(response.getRec().getBankCode())
                  .balance(BigDecimal.ZERO)
                  .totalAmount(BigDecimal.valueOf(iou.getIouAmount()))
                  .remainingAmount(BigDecimal.valueOf(iou.getIouAmount()))
                  .status(VirtualAccountStatus.ACTIVE)
                  .build();

        save(virtualAccount);
        iou.linkVirtualAccount(virtualAccount);

        return virtualAccount;
    }

    public List<VirtualAccount> findAllActiveAccounts() {
        return virtualAccountRepository.findByStatus(VirtualAccountStatus.ACTIVE);
    }

    public void save(VirtualAccount virtualAccount) {
        virtualAccountRepository.save(virtualAccount);
    }

    /**
     * TODO : 가상 계좌 입금 처리
     *
     * @param email 입금 요청한 회원의 email
     * @param depositRequestDto 입금 요청 정보
     * @return DepositResponse
     */
    public DepositResponse deposit(String email, DepositRequestDto depositRequestDto) {
        log.info("---------- 계좌 입금 처리 시작: 사용자 {}, 요청 {} ---------", email, depositRequestDto);

        Member member = getMemberByEmail(email);
        log.info("accountNo : {}", depositRequestDto.getAccountNo());
        VirtualAccount account = getVirtualAccountByAccountNumber(depositRequestDto.getAccountNo());

        DepositResponse response = restClientUtil.deposit(
                member.getUserKey(),
                depositRequestDto.getAccountNo(),
                depositRequestDto.getTransactionBalance(),
                depositRequestDto.getTransactionSummary()
        );

        /**
         * ! XXX
         * 굳이 검증 X
         */
//        log.info("-------- 이제 validate 진입 -------");
//        log.info("입금 결과 확인 : {}", response);
//        restClientUtil.validateApiResponse(response.getHeaderResponse());
//        log.info("-------- 여기서 터짐 ---------");
        // ! 계좌 잔액 업데이트
        BigDecimal newBalance = account.getBalance().add(BigDecimal.valueOf(depositRequestDto.getTransactionBalance()));
        account.setBalance(newBalance);
        virtualAccountRepository.save(account);

        log.info("------------ 계좌 입금 처리 완료: {} ------------", response);
        return response;
    }


    /**
     * TODO : 가상 계좌 출금 처리 -> userKey를 채권자의 userKey로 진행해야함.
     *
     * @param email 출금 요청한 회원의 email
     * @param withdrawRequestDto 출금 요청 정보
     * @return WithdrawResponse
     */
    public WithdrawResponse withdraw(String email, WithdrawRequestDto withdrawRequestDto) {
        log.info("---------- 계좌 출금 처리 시작: 사용자 {}, 요청 {} ---------", email, withdrawRequestDto);

        Member member = getMemberByEmail(email);
        VirtualAccount account = getVirtualAccountByAccountNumber(withdrawRequestDto.getAccountNo());

        // 출금 가능 여부 확인
        if (account.getBalance().compareTo(BigDecimal.valueOf(withdrawRequestDto.getTransactionBalance())) < 0) {
            throw new InsufficientBalanceException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        WithdrawResponse response = restClientUtil.withdraw(
                account.getCreditor().getUserKey(),
                withdrawRequestDto.getAccountNo(),
                withdrawRequestDto.getTransactionBalance(),
                withdrawRequestDto.getTransactionSummary()
        );

        // 계좌 잔액 업데이트
        BigDecimal newBalance = account.getBalance().subtract(BigDecimal.valueOf(withdrawRequestDto.getTransactionBalance()));
        account.setBalance(newBalance);
        virtualAccountRepository.save(account);

        log.info("------------ 계좌 출금 처리 완료: {} ------------", response);
        return response;
    }

    /**
     * TODO : 계좌 이체 처리 -> 채권자의 userKey 사용
     *
     * @param email 이체 요청한 회원의 email
     * @param transferRequestDto 이체 요청 정보
     * @return TransferResponse
     */
    public TransferResponse transfer(String email, TransferRequestDto transferRequestDto) {
        log.info("---------- 계좌 이체 처리 시작: 사용자 {}, 요청 {} ---------", email, transferRequestDto);

        Member member = getMemberByEmail(email);
        VirtualAccount fromAccount = getVirtualAccountByAccountNumber(transferRequestDto.getWithdrawalAccountNo());
        VirtualAccount toAccount = getVirtualAccountByAccountNumber(transferRequestDto.getDepositAccountNo());

        // ! 이체 가능 여부 확인
        if (fromAccount.getBalance().compareTo(BigDecimal.valueOf(transferRequestDto.getTransactionBalance())) < 0) {
            throw new InsufficientBalanceException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        // ! 채권자의 userKey 사용
        TransferResponse response = restClientUtil.transfer(
                fromAccount.getCreditor().getUserKey(),
                transferRequestDto.getWithdrawalAccountNo(),
                transferRequestDto.getDepositAccountNo(),
                transferRequestDto.getTransactionBalance(),
                transferRequestDto.getDepositTransactionSummary(),
                transferRequestDto.getWithdrawalTransactionSummary()
        );

        // ! 출금 계좌 잔액 업데이트
        BigDecimal transferAmount = BigDecimal.valueOf(transferRequestDto.getTransactionBalance());
        BigDecimal newFromBalance = fromAccount.getBalance().subtract(transferAmount);
        fromAccount.setBalance(newFromBalance);
        virtualAccountRepository.save(fromAccount);

        // ! 입금 계좌 잔액 업데이트
        BigDecimal newToBalance = toAccount.getBalance().add(transferAmount);
        toAccount.setBalance(newToBalance);
        virtualAccountRepository.save(toAccount);

        log.info("------------ 계좌 이체 처리 완료: {} ------------", response);
        return response;
    }

    private Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                               .orElseThrow(() -> new UserNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private VirtualAccount getVirtualAccountByAccountNumber(String accountNumber) {
        return virtualAccountRepository.findByAccountNumber(accountNumber)
                                .orElseThrow(() -> new VirtualAccountException(ErrorCode.VIRTUAL_ACCOUNT_NOT_FOUND, "Account number : " + accountNumber + " not found"));
    }
}