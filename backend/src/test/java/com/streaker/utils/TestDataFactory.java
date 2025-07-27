package com.streaker.utils;

import com.streaker.model.Category;
import com.streaker.model.Habit;
import com.streaker.model.Log;
import com.streaker.model.Reward;
import com.streaker.model.Streak;
import com.streaker.model.User;
import com.streaker.utlis.enums.Frequency;
import com.streaker.utlis.enums.Role;

import java.time.LocalDate;

public class TestDataFactory {

    public static User createUser() {
        return createUser("testuser", "test@example.com", "SecurePass123!");
    }

    public static User createUser(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(Role.USER);
        return user;
    }

    public static Category createCategory(User user) {
        Category category = new Category();
        category.setName("Health");
        category.setColor("#FF5733");
        category.setUser(user);
        return category;
    }

    public static Category createCategory(User user, String name, String color) {
        Category category = new Category();
        category.setName(name);
        category.setColor(color);
        category.setUser(user);
        return category;
    }

    public static Habit createHabit(User user, Streak streak, Category category) {
        Habit habit = new Habit();
        habit.setName("Morning Workout");
        habit.setDescription("Complete 30min cardio");
        habit.setFrequency(Frequency.DAILY);
        habit.setUser(user);
        habit.setStreak(streak);
        habit.setCategory(category);
        return habit;
    }

    public static Streak createStreak(User user) {
        Streak streak = new Streak();
        streak.setName("No Sugar");
        streak.setDescription("Avoid sugary food");
        streak.setCurrentCount(5);
        streak.setLongestStreak(7);
        streak.setIsActive(true);
        streak.setStartDate(LocalDate.now().minusDays(5));
        streak.setLastCheck(LocalDate.now().minusDays(1));
        streak.setUser(user);
        return streak;
    }

    public static Log createLog(Habit habit, boolean completed) {
        Log log = new Log();
        log.setDate(LocalDate.now());
        log.setCompleted(completed);
        log.setHabit(habit);
        return log;
    }

    public static Reward createReward(User user) {
        Reward reward = new Reward();
        reward.setName("Protein Shake");
        reward.setDescription("Reward yourself after a workout");
        reward.setPointsRequired(10);
        reward.setUser(user);
        reward.setUnlocked(false);
        reward.setUnlockedAt(null);
        return reward;
    }
}
