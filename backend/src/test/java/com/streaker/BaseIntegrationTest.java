package com.streaker;

import com.streaker.repository.CategoryRepository;
import com.streaker.repository.HabitRepository;
import com.streaker.repository.LogRepository;
import com.streaker.repository.RewardRepository;
import com.streaker.repository.StreakRepository;
import com.streaker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseIntegrationTest {

    protected static final String USER_TEST_USERNAME = "john";
    protected static final String USER_TEST_EMAIL = "john@example.com";
    protected static final String PASSWORD = "securePassword1";

    @Autowired
    protected HabitRepository habitRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected StreakRepository streakRepository;

    @Autowired
    protected RewardRepository rewardRepository;

    @Autowired
    protected LogRepository logRepository;

    @Autowired
    protected UserRepository userRepository;

    protected void cleanDatabase() {
        // Order matters!
        logRepository.deleteAll();
        habitRepository.deleteAll();
        streakRepository.deleteAll();
        categoryRepository.deleteAll();
        rewardRepository.deleteAll();
        userRepository.deleteAll();
    }
}
