package com.example.cinetrackerbackend.gamification;


import com.example.cinetrackerbackend.common.ApiResponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeService {
    private final com.example.cinetrackerbackend.watchlist.WatchlistRepository watchlistRepository;


    private final GamificationService gamificationService;
    private final UserBadgeRepository userBadgeRepository;
    private final BadgeRepository badgeRepository;
    private final com.example.cinetrackerbackend.user.UserRepository userRepository;

    @jakarta.annotation.PostConstruct
    public void initBadges() {
        seedBadge("Time Bender", "Watch 5 Christopher Nolan masterpieces", "COLLECTION_WATCHED");
        seedBadge("Heart-Throb", "Watch 5 iconic Rom-Coms", "COLLECTION_WATCHED");
        seedBadge("Fearless", "Survive 5 classic Horror movies", "COLLECTION_WATCHED");
    }

    private void seedBadge(String name, String desc, String type) {
        if (badgeRepository.findByName(name).isEmpty()) {
            badgeRepository.save(new Badge(null, name, desc, null, type));
        }
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Challenge {
        private String id;
        private String title;
        private String description;
        private int rewardXp;
        private boolean isCompleted; // This will now mean "ALREADY CLAIMED"
        private boolean canClaim;    // This will mean "REQUIREMENTS MET"
        private List<Long> requiredMovieIds;
        private List<Long> completedMovieIds;
        private String badgeName;
    }


    public List<Challenge> getQuests(Long userId) {
        List<Challenge> quests = new ArrayList<>();
        
        // Quest 1: Nolan's Disciple
        quests.add(new Challenge("Q_NOLAN", "Nolan's Disciple", "Watch 5 Christopher Nolan masterpieces", 300, false, false, 
            Arrays.asList(157336L, 27205L, 155L, 77L, 1124L), new ArrayList<>(), "Time Bender"));

        // Quest 2: Hopeless Romantic
        quests.add(new Challenge("Q_ROMCOM", "Hopeless Romantic", "Watch 5 iconic Rom-Coms", 500, false, false, 
            Arrays.asList(639L, 50646L, 4951L, 508L, 509L), new ArrayList<>(), "Heart-Throb"));

        // Quest 3: Horror Buff
        quests.add(new Challenge("Q_HORROR", "Fearless", "Survive 5 classic Horror movies", 250, false, false, 
            Arrays.asList(9552L, 694L, 948L, 30497L, 377L), new ArrayList<>(), "Fearless"));


        // Check progress for each quest
        for (Challenge quest : quests) {
            checkProgress(userId, quest);
        }
        
        return quests;
    }

    private void checkProgress(Long userId, Challenge quest) {
        // 1. Check if requirements are met (Watched movies)
        List<Long> completedInWatchlist = watchlistRepository.findByUser_IdAndStatus(userId, 
                com.example.cinetrackerbackend.watchlist.WatchlistStatus.COMPLETED, 
                org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .map(com.example.cinetrackerbackend.watchlist.Watchlist::getMovieId)
                .collect(Collectors.toList());

        List<Long> progress = quest.getRequiredMovieIds().stream()
                .filter(completedInWatchlist::contains)
                .collect(Collectors.toList());
        
        quest.setCompletedMovieIds(progress);
        quest.setCanClaim(progress.size() == quest.getRequiredMovieIds().size());

        // 2. Check if already claimed (Badge exists)
        boolean alreadyClaimed = badgeRepository.findByName(quest.getBadgeName())
                .map(badge -> userBadgeRepository.existsById(new UserBadgeId(userId, badge.getId())))
                .orElse(false);
        
        quest.setCompleted(alreadyClaimed);
    }


    @org.springframework.transaction.annotation.Transactional
    public String claimQuest(Long userId, String questId) {
        Challenge quest = getQuests(userId).stream()
                .filter(q -> q.getId().equals(questId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Quest not found"));

        if (!quest.isCanClaim()) {
            return "Quest requirements not met yet. Keep watching!";
        }

        // 1. Find the Badge in DB first

        Badge badge = badgeRepository.findByName(quest.getBadgeName())
                .orElseThrow(() -> new RuntimeException("Badge definition '" + quest.getBadgeName() + "' not found in DB. Please run the SQL seeder."));

        // 2. Check if already claimed
        UserBadgeId badgeId = new UserBadgeId(userId, badge.getId());
        if (userBadgeRepository.existsById(badgeId)) {
            return "Quest already claimed! Check your profile for the badge.";
        }

        // 3. Award XP (Only if not already claimed)
        gamificationService.awardXp(userId, quest.getRewardXp(), "Completed Quest: " + quest.getTitle());

        // 4. Award Badge
        com.example.cinetrackerbackend.user.User user = userRepository.findById(userId).orElseThrow();
        UserBadge userBadge = new UserBadge(user, badge);
        userBadgeRepository.save(userBadge);

        return "Congratulations! Quest claimed and badge awarded.";
    }




    public List<Challenge> getDailyChallenges(Long userId) {
        List<Challenge> challenges = new ArrayList<>();
        
        // 1. Socialite Challenge (Follow 1 person)
        boolean followDone = hasDoneThisWeek(userId, "FOLLOW", 1);
        challenges.add(new Challenge("CH_1", "Socialite", "Follow 1 movie buff this week", 50, followDone, followDone, null, null, null));

        // 2. Explorer Challenge (Add 3 movies)
        boolean addDone = hasDoneThisWeek(userId, "ADD_WATCHLIST", 3);
        challenges.add(new Challenge("CH_2", "Explorer", "Add 3 movies to your watchlist this week", 30, addDone, addDone, null, null, null));

        // 3. Binge Watcher (Complete 3 movies)
        boolean completeDone = hasDoneThisWeek(userId, "COMPLETE", 3);
        challenges.add(new Challenge("CH_3", "Binge Watcher", "Complete 3 movies this week", 100, completeDone, completeDone, null, null, null));

        // 4. Critical Eye (Rate 5 movies)
        boolean rateDone = hasDoneThisWeek(userId, "RATE", 5);
        challenges.add(new Challenge("CH_4", "Critical Eye", "Rate 5 movies this week", 150, rateDone, rateDone, null, null, null));

        // 5. Elite Scout (Add 10 movies)
        boolean eliteAddDone = hasDoneThisWeek(userId, "ADD_WATCHLIST", 10);
        challenges.add(new Challenge("CH_5", "Elite Scout", "Add 10 movies to your watchlist this week", 200, eliteAddDone, eliteAddDone, null, null, null));

        return challenges;
    }

    private boolean hasDoneToday(Long userId, String actionType, int requiredCount) {
        return gamificationService.getDailyActionCount(userId, actionType) >= requiredCount;
    }

    private boolean hasDoneThisWeek(Long userId, String actionType, int requiredCount) {
        return gamificationService.getWeeklyActionCount(userId, actionType) >= requiredCount;
    }
}


