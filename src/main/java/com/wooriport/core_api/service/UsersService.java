package com.wooriport.core_api.service;

import com.wooriport.core_api.domain.Users;
import com.wooriport.core_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsersService {
    private final UserRepository userRepository;

    @Transactional
    public void withdraw(UUID userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        if (user.getStatus() == Users.UserStatus.WITHDRAWN) {
            throw new IllegalArgumentException("이미 탈퇴 처리된 계정입니다.");
        }

        // Soft Delete 상태로 변경 (status = WITHDRAWN, deleteAt = 현재시간)
        user.withdraw();
    }

}
