package com.wzu.travelsystem.service;

import com.wzu.travelsystem.entity.*;
import com.wzu.travelsystem.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    @Autowired private RatingMapper ratingMapper;
    @Autowired private FavoriteMapper favoriteMapper;
    @Autowired private BehaviorLogMapper behaviorLogMapper;
    @Autowired private AttractionMapper attractionMapper;

    /**
     * 对齐【表1】：计算多维度用户偏好矩阵
     * 公式：0.4*评分 + 0.3*收藏 + 0.15*时长 + 0.1*点击 + 0.15*关键词
     */
    public Map<Long, Map<Long, Double>> getPreferenceMatrix() {
        Map<Long, Map<Long, Double>> matrix = new HashMap<>();

        // 1. 处理评分 (权重 0.4)
        ratingMapper.selectList(null).forEach(r ->
                updateMatrix(matrix, r.getUserId(), r.getAttractionId(), r.getScore() * 0.4));

        // 2. 处理收藏 (权重 0.3)
        // 收藏视为满分行为(5分)，得分 = 5 * 0.3 = 1.5
        favoriteMapper.selectList(null).forEach(f ->
                updateMatrix(matrix, f.getUserId(), f.getAttractionId(), 1.5));

        // 3. 处理行为日志 (时长 0.15 + 点击 0.1)
        List<BehaviorLog> logs = behaviorLogMapper.selectList(null);
        for (BehaviorLog log : logs) {
            // 时长量化：假设 300秒为满分，得分 = (duration/300)*5 * 0.15
            double durationScore = (Math.min(log.getBrowseDuration(), 300) / 300.0) * 0.75;
            // 点击量化：假设 10次为满分，得分 = (clicks/10)*5 * 0.1
            double clickScore = (Math.min(log.getClickCount(), 10) / 10.0) * 0.5;

            updateMatrix(matrix, log.getUserId(), log.getAttractionId(), durationScore + clickScore);
        }

        return matrix;
    }

    private void updateMatrix(Map<Long, Map<Long, Double>> matrix, Long uId, Long aId, double score) {
        matrix.computeIfAbsent(uId, k -> new HashMap<>());
        Map<Long, Double> userMap = matrix.get(uId);
        userMap.put(aId, userMap.getOrDefault(aId, 0.0) + score);
    }

    /**
     * 对齐【表2】：基于混合特征的景点推荐 (Item-CF)
     * 逻辑：用户重叠度 + 分类相似度 + 地理邻近度
     */
    public List<Long> suggestByHybridItemCF(Long targetUserId, int num) {
        Map<Long, Map<Long, Double>> prefMatrix = getPreferenceMatrix();
        if (!prefMatrix.containsKey(targetUserId)) return new ArrayList<>();

        Map<Long, Double> targetUserPrefs = prefMatrix.get(targetUserId);
        List<Attraction> allAttractions = attractionMapper.selectList(null);
        Map<Long, Attraction> attrMap = allAttractions.stream().collect(Collectors.toMap(Attraction::getId, a -> a));

        Map<Long, Double> recommendScores = new HashMap<>();

        // 遍历用户感兴趣的每个景点
        for (Long myAttrId : targetUserPrefs.keySet()) {
            Attraction myAttr = attrMap.get(myAttrId);
            if (myAttr == null) continue;

            // 计算该景点与其他所有景点的相似度
            for (Attraction otherAttr : allAttractions) {
                if (targetUserPrefs.containsKey(otherAttr.getId())) continue;

                // 计算【表2】定义的混合相似度
                double sim = calculateHybridSimilarity(myAttr, otherAttr, prefMatrix);

                // 推荐得分 = 相似度 * 用户对原景点的偏好得分
                double finalScore = sim * targetUserPrefs.get(myAttrId);
                recommendScores.put(otherAttr.getId(), recommendScores.getOrDefault(otherAttr.getId(), 0.0) + finalScore);
            }
        }

        return recommendScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey).limit(num).collect(Collectors.toList());
    }

    /**
     * 计算混合相似度 (对齐表2)
     */
    private double calculateHybridSimilarity(Attraction a1, Attraction a2, Map<Long, Map<Long, Double>> matrix) {
        // 1. 用户群体重叠度 (Jaccard系数)
        double jaccard = calculateJaccard(a1.getId(), a2.getId(), matrix);

        // 2. 景点分类相似度
        double catSim = Objects.equals(a1.getCategoryId(), a2.getCategoryId()) ? 1.0 : 0.0;

        // 3. 地理位置邻近度 (经纬度欧几里得距离归一化)
        double distance = Math.sqrt(Math.pow(a1.getLongitude() - a2.getLongitude(), 2) +
                Math.pow(a1.getLatitude() - a2.getLatitude(), 2));
        double geoSim = 1.0 / (1.0 + distance);

        // 权重融合 (可根据论文需要调整比例)
        return jaccard * 0.5 + catSim * 0.3 + geoSim * 0.2;
    }

    private double calculateJaccard(Long id1, Long id2, Map<Long, Map<Long, Double>> matrix) {
        Set<Long> users1 = new HashSet<>();
        Set<Long> users2 = new HashSet<>();
        matrix.forEach((uId, prefs) -> {
            if (prefs.containsKey(id1)) users1.add(uId);
            if (prefs.containsKey(id2)) users2.add(uId);
        });
        if (users1.isEmpty() || users2.isEmpty()) return 0.0;

        Set<Long> intersect = new HashSet<>(users1);
        intersect.retainAll(users2);
        Set<Long> union = new HashSet<>(users1);
        union.addAll(users2);

        return (double) intersect.size() / union.size();
    }

    /**
     * 基于用户的协同过滤 (User-CF)
     */
    public List<Long> suggestByUserCF(Long targetUserId, int num) {
        Map<Long, Map<Long, Double>> matrix = getPreferenceMatrix();
        Map<Long, Double> targetUserPrefs = matrix.get(targetUserId);
        if (targetUserPrefs == null) return new ArrayList<>();

        Map<Long, Double> similarities = new HashMap<>();
        for (Long otherId : matrix.keySet()) {
            if (otherId.equals(targetUserId)) continue;
            double sim = calculateCosine(targetUserPrefs, matrix.get(otherId));
            if (sim > 0) similarities.put(otherId, sim);
        }

        Long mostSimilarUser = similarities.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);

        if (mostSimilarUser == null) return new ArrayList<>();

        return matrix.get(mostSimilarUser).keySet().stream()
                .filter(id -> !targetUserPrefs.containsKey(id))
                .limit(num).collect(Collectors.toList());
    }

    private double calculateCosine(Map<Long, Double> u1, Map<Long, Double> u2) {
        Set<Long> common = new HashSet<>(u1.keySet()); common.retainAll(u2.keySet());
        if (common.isEmpty()) return 0.0;
        double dot = 0, n1 = 0, n2 = 0;
        for (double v : u1.values()) n1 += v*v;
        for (double v : u2.values()) n2 += v*v;
        for (Long id : common) dot += u1.get(id) * u2.get(id);
        return dot / (Math.sqrt(n1) * Math.sqrt(n2));
    }
}