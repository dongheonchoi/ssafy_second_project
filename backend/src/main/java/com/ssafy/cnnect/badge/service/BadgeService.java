package com.ssafy.cnnect.badge.service;


import com.ssafy.cnnect.badge.dto.BadgeListResponseDto;
import com.ssafy.cnnect.badge.entity.Badge;
import com.ssafy.cnnect.badge.repository.BadgeRepository;
import com.ssafy.cnnect.history.entity.History;
import com.ssafy.cnnect.history.repository.HistoryRepository;
import com.ssafy.cnnect.user.entity.User;
import com.ssafy.cnnect.user.entity.UserBadge;
import com.ssafy.cnnect.user.repository.UserBadgeRepository;
import com.ssafy.cnnect.user.repository.UserRepository;
import com.ssafy.cnnect.user.service.CustomUserDetailsService;
import com.ssafy.cnnect.video.entity.Category;
import com.ssafy.cnnect.video.entity.Video;
import com.ssafy.cnnect.video.repository.CategoryRepository;
import com.ssafy.cnnect.video.repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BadgeService {
    private final CustomUserDetailsService customUserDetailsService;

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final CategoryRepository categoryRepository;
    private final VideoRepository videoRepository;
    private final HistoryRepository historyRepository;

    @Transactional
    public List<BadgeListResponseDto> getMyBadge(){
        User user = customUserDetailsService.getUserByAuthentication();
        System.out.println("badge : " + user.getUserEmail() + " " + user.getUserId());
        List<UserBadge> list = userBadgeRepository.findAllByUser(user);
        List<BadgeListResponseDto> responseList = new ArrayList<>();
        for(UserBadge ub : list){
            BadgeListResponseDto badgeListResponseDto = BadgeListResponseDto.builder()
                    .badgeId(ub.getBadge().getBadgeId())
                    .badgeScore(Optional.ofNullable(ub.getBadge().getBadgeScore()).orElse((long) -1))
                    .badgeCount(ub.getBadge().getBadgeCount())
                    .badgeName(ub.getBadge().getBadgeName())
                    .badgeCondition(String.valueOf(ub.getBadge().getBadgeCondition()))
                    .badgeCategory(Optional.ofNullable(ub.getBadge().getCategory()).map(Category::getCategoryName).orElse(null))
                    .build();
            responseList.add(badgeListResponseDto);
        }
        return responseList;
    }

    @Transactional
    public List<BadgeListResponseDto> getAllBadge(){
        Iterable<Badge> badges = badgeRepository.findAll();

        List<BadgeListResponseDto> list = new ArrayList<>();
        for(Badge b : badges){
            BadgeListResponseDto badgeListResponseDto = BadgeListResponseDto.builder()
                    .badgeId(b.getBadgeId())
                    .badgeCategory(Optional.ofNullable(b.getCategory()).map(Category::getCategoryName).orElse(null))
                    .badgeCondition(String.valueOf(b.getBadgeCondition()))
                    .badgeCount(b.getBadgeCount())
                    .badgeName(b.getBadgeName())
                    .badgeScore(Optional.ofNullable(b.getBadgeScore()).orElse((long) -1))
                    .build();
            list.add(badgeListResponseDto);
        }

        return list;
    }

    @Transactional
    public List<Badge> getNewBadge(Long categoryId, Long newSentence){
        List<Badge> newBadges = new ArrayList<>(); // 새로 획득한 뱃지 list

        // <1> - 1. 방금 학습한 영상의 카테고리 받기 -> 해당 카테고리의 유저가 기존에 학습한 영상 갯수 구하기
        User user = customUserDetailsService.getUserByAuthentication();
        List<History> histories = historyRepository.findAllByUser(user);
        List<Video> videoList = new ArrayList<>();

        for(History h : histories){
            Video v = videoRepository.findByVideoId(h.getVideoId());
            videoList.add(v);
        }

        Long cnt = videoList.stream()
                .filter(video -> video.getCategory_id() == categoryId)
                .count();

        // 2. 해당 카테고리를 갖고 있는 뱃지를 낮은 count부터 돌면서 아직 못얻은 단계의 뱃지 찾기
        List<BadgeListResponseDto> userBadge = getMyBadge();
        List<Badge> categoryBadge = badgeRepository.findAllByCategory(categoryRepository.findById(categoryId).get());
        Optional<Badge> missingBadge = categoryBadge.stream()
                .filter(badge -> userBadge.stream().noneMatch(badgeListResponseDto -> badgeListResponseDto.getBadgeId().equals(badge.getBadgeId())))
                .sorted(Comparator.comparingLong(Badge::getBadgeCount))
                .findFirst();
        if(missingBadge.isPresent() && cnt + 1 == missingBadge.get().getBadgeCount()){
            newBadges.add(missingBadge.get());
        }

        // <2> 방금 학습한 영상에서 8점 이상의 쉐도잉 점수를 받은 문장 갯수 받기


        return newBadges;
    }
}
