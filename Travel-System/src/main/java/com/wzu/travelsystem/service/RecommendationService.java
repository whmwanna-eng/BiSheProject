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
     * ==========================================
     * 核心重构：对齐【表1】计算多维度用户偏好矩阵
     * 公式：0.4*评分 + 0.3*收藏 + 0.15*时长 + 0.1*点击 + 0.15*关键词
     * ==========================================
     */
    public Map<Long, Map<Long, Double>> getPreferenceMatrix() {
        Map<Long, Map<Long, Double>> matrix = new HashMap<>();

        // 1. 获取基础数据
        List<Attraction> allAttractions = attractionMapper.selectList(null);
        Map<Long, Attraction> attrMap = allAttractions.stream().collect(Collectors.toMap(Attraction::getId, a -> a));

        // 2. 处理评分 (权重 0.4)
        ratingMapper.selectList(null).forEach(r ->
                updateMatrix(matrix, r.getUserId(), r.getAttractionId(), r.getScore() * 0.4));

        // 3. 处理收藏 (权重 0.3)
        // 收藏视为满分行为(5分)，为了和评分的1-5分制统一量纲，得分 = 5 * 0.3 = 1.5
        favoriteMapper.selectList(null).forEach(f ->
                updateMatrix(matrix, f.getUserId(), f.getAttractionId(), 1.5));

        // 4. 处理行为日志 (时长 0.15 + 点击 0.1 + 关键词 0.15)
        List<BehaviorLog> logs = behaviorLogMapper.selectList(null);

        // 核心修正：必须按照 "用户ID_景点ID" 进行分组聚合，避免由于前端多次上报导致得分无限累加
        Map<String, List<BehaviorLog>> groupedLogs = logs.stream()
                .filter(log -> log.getUserId() != null && log.getAttractionId() != null)
                .collect(Collectors.groupingBy(log -> log.getUserId() + "_" + log.getAttractionId()));

        for (Map.Entry<String, List<BehaviorLog>> entry : groupedLogs.entrySet()) {
            List<BehaviorLog> userAttrLogs = entry.getValue();
            Long userId = userAttrLogs.get(0).getUserId();
            Long attractionId = userAttrLogs.get(0).getAttractionId();
            Attraction attraction = attrMap.get(attractionId);

            int totalDuration = 0;
            int totalClicks = 0;
            Set<Character> searchChars = new HashSet<>();

            // 聚合该用户对该景点的所有行为数据
            for (BehaviorLog log : userAttrLogs) {
                if ("BROWSE".equals(log.getActionType()) && log.getBrowseDuration() != null) {
                    totalDuration += log.getBrowseDuration();
                }
                if ("CLICK".equals(log.getActionType()) && log.getClickCount() != null) {
                    totalClicks += log.getClickCount();
                }
                if ("SEARCH".equals(log.getActionType()) && log.getSearchKeyword() != null) {
                    // 将搜索关键词打散为字符集，用于后续的 Jaccard 计算
                    for (char c : log.getSearchKeyword().toCharArray()) {
                        searchChars.add(c);
                    }
                }
            }

            // 时长归一化：假设 300秒 为满分，映射到5分制，得分 = (duration/300) * 5 * 0.15
            double durationScore = (Math.min(totalDuration, 300) / 300.0) * 5 * 0.15;

            // 点击归一化：假设 10次 为满分，映射到5分制，得分 = (clicks/10) * 5 * 0.1
            double clickScore = (Math.min(totalClicks, 10) / 10.0) * 5 * 0.1;

            // 搜索关键词 Jaccard 相似度匹配 (特征重合度)
            double jaccardScore = 0.0;
            if (!searchChars.isEmpty() && attraction != null) {
                String targetText = (attraction.getName() != null ? attraction.getName() : "") +
                        (attraction.getDescription() != null ? attraction.getDescription() : "");
                Set<Character> attrChars = new HashSet<>();
                for (char c : targetText.toCharArray()) {
                    attrChars.add(c);
                }

                // 计算 Jaccard 相似度 = 交集大小 / 并集大小
                Set<Character> intersection = new HashSet<>(searchChars);
                intersection.retainAll(attrChars);
                Set<Character> union = new HashSet<>(searchChars);
                union.addAll(attrChars);

                double jaccard = union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
                jaccardScore = jaccard * 5 * 0.15; // 映射到5分制
            }

            // 将计算出的综合行为得分累加到矩阵中
            updateMatrix(matrix, userId, attractionId, durationScore + clickScore + jaccardScore);
        }

        return matrix;
    }

    private void updateMatrix(Map<Long, Map<Long, Double>> matrix, Long uId, Long aId, double score) {
        matrix.computeIfAbsent(uId, k -> new HashMap<>());
        Map<Long, Double> userMap = matrix.get(uId);
        userMap.put(aId, userMap.getOrDefault(aId, 0.0) + score);
    }

    /**
     * 基于用户的协同过滤 (User-CF)
     */
    public List<Long> suggestByUserCF(Long targetUserId, int num) {
        Map<Long, Map<Long, Double>> matrix = getPreferenceMatrix();
        Map<Long, Double> targetUserPrefs = matrix.get(targetUserId);
        if (targetUserPrefs == null || targetUserPrefs.isEmpty()) return new ArrayList<>();

        Map<Long, Double> similarities = new HashMap<>();
        for (Long otherId : matrix.keySet()) {
            if (otherId.equals(targetUserId)) continue;
            double sim = calculateCosine(targetUserPrefs, matrix.get(otherId));
            if (sim > 0) similarities.put(otherId, sim);
        }

        // 寻找最相似的用户（或者取 Top-K 综合推荐，这里取最相似的一个用户进行简化）
        Long mostSimilarUser = similarities.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);

        if (mostSimilarUser == null) return new ArrayList<>();

        // 过滤掉当前用户已经交互过的景点，推荐新的景点
        return matrix.get(mostSimilarUser).keySet().stream()
                .filter(id -> !targetUserPrefs.containsKey(id))
                .limit(num).collect(Collectors.toList());
    }

    private double calculateCosine(Map<Long, Double> u1, Map<Long, Double> u2) {
        Set<Long> common = new HashSet<>(u1.keySet());
        common.retainAll(u2.keySet());
        if (common.isEmpty()) return 0.0;
        double dot = 0, n1 = 0, n2 = 0;
        for (double v : u1.values()) n1 += v*v;
        for (double v : u2.values()) n2 += v*v;
        for (Long id : common) dot += u1.get(id) * u2.get(id);
        return dot / (Math.sqrt(n1) * Math.sqrt(n2));
    }

    
    /**
     * ==========================================
     * 对齐【表2】：基于混合特征的景点推荐 (Item-CF)
     * 逻辑：用户重叠度(0.4) + 分类(0.2) + 地理(0.2) + 价格区间(0.1) + 评分特征(0.1)
     * ==========================================
     */
    public List<Long> suggestByHybridItemCF(Long targetUserId, int num) {
        Map<Long, Map<Long, Double>> prefMatrix = getPreferenceMatrix();
        if (!prefMatrix.containsKey(targetUserId)) return new ArrayList<>();

        Map<Long, Double> targetUserPrefs = prefMatrix.get(targetUserId);
        List<Attraction> allAttractions = attractionMapper.selectList(null);
        Map<Long, Attraction> attrMap = allAttractions.stream().collect(Collectors.toMap(Attraction::getId, a -> a));

        // 【新增优化】在循环外提前计算好所有景点的“平均评分”，避免在双重 for 循环中频繁查询数据库 (N+1问题)
        List<Rating> allRatings = ratingMapper.selectList(null);
        Map<Long, List<Rating>> ratingsByAttr = allRatings.stream()
                .filter(r -> r.getAttractionId() != null)
                .collect(Collectors.groupingBy(Rating::getAttractionId));

        Map<Long, Double> avgRatingMap = new HashMap<>();
        for (Attraction attr : allAttractions) {
            List<Rating> attrRatings = ratingsByAttr.getOrDefault(attr.getId(), new ArrayList<>());
            double avg = attrRatings.isEmpty() ? 0.0 : attrRatings.stream().mapToDouble(Rating::getScore).average().orElse(0.0);
            avgRatingMap.put(attr.getId(), avg);
        }

        Map<Long, Double> recommendScores = new HashMap<>();

        // 遍历目标用户感兴趣的每一个景点
        for (Long myAttrId : targetUserPrefs.keySet()) {
            Attraction myAttr = attrMap.get(myAttrId);
            if (myAttr == null) continue;

            // 计算该景点与系统中其他未访问景点的相似度
            for (Attraction otherAttr : allAttractions) {
                // 如果用户已经交互过这个景点，则跳过不推荐
                if (targetUserPrefs.containsKey(otherAttr.getId())) continue;

                // 计算五维混合相似度
                double sim = calculateHybridSimilarity(myAttr, otherAttr, prefMatrix, avgRatingMap);

                // 推荐得分 = 相似度 * 用户对原景点的偏好得分
                double finalScore = sim * targetUserPrefs.get(myAttrId);
                recommendScores.put(otherAttr.getId(), recommendScores.getOrDefault(otherAttr.getId(), 0.0) + finalScore);
            }
        }

        // 根据综合得分倒序排序，截取 Top-N
        return recommendScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(num)
                .collect(Collectors.toList());
    }

    /**
     * 计算五维混合特征相似度
     */
    private double calculateHybridSimilarity(Attraction a1, Attraction a2,
                                             Map<Long, Map<Long, Double>> matrix,
                                             Map<Long, Double> avgRatingMap) {

        // 1. 用户群体重叠度 (Jaccard系数) - 权重 0.4
        double jaccard = calculateJaccard(a1.getId(), a2.getId(), matrix);

        // 2. 景点分类相似度 - 权重 0.2
        double catSim = Objects.equals(a1.getCategoryId(), a2.getCategoryId()) ? 1.0 : 0.0;

        // 3. 地理位置邻近度 (经纬度欧几里得距离归一化) - 权重 0.2
        double distance = 999.0;
        if (a1.getLongitude() != null && a1.getLatitude() != null &&
                a2.getLongitude() != null && a2.getLatitude() != null) {
            distance = Math.sqrt(Math.pow(a1.getLongitude() - a2.getLongitude(), 2) +
                    Math.pow(a1.getLatitude() - a2.getLatitude(), 2));
        }
        double geoSim = 1.0 / (1.0 + distance); // 距离越近，相似度越趋近于1

        // 4. 门票价格区间相似度 (分段离散化处理) - 权重 0.1
        double priceSim = calculatePriceSimilarity(a1.getPrice(), a2.getPrice());

        // 5. 评分分布特征相似度 (均分差值归一化) - 权重 0.1
        double r1 = avgRatingMap.getOrDefault(a1.getId(), 0.0);
        double r2 = avgRatingMap.getOrDefault(a2.getId(), 0.0);
        // 分数差值越小，相似度越高；最大分差为5分
        double ratingSim = 1.0 - (Math.abs(r1 - r2) / 5.0);

        // 综合权重融合返回
        return (jaccard * 0.4) + (catSim * 0.2) + (geoSim * 0.2) + (priceSim * 0.1) + (ratingSim * 0.1);
    }

    /**
     * 门票价格分段离散化计算相似度
     * 逻辑：同一区间得1分，相邻区间得0.5分，跨度更大得0分
     */
    private double calculatePriceSimilarity(java.math.BigDecimal p1, java.math.BigDecimal p2) {
        if (p1 == null || p2 == null) return 0.0;

        int level1 = getPriceLevel(p1.doubleValue());
        int level2 = getPriceLevel(p2.doubleValue());

        int diff = Math.abs(level1 - level2);
        if (diff == 0) return 1.0;
        if (diff == 1) return 0.5;
        return 0.0;
    }

    /**
     * 将价格划分为 4 个离散区间
     */
    private int getPriceLevel(double price) {
        if (price <= 50) return 1;       // 便宜/免费
        if (price <= 100) return 2;      // 适中
        if (price <= 200) return 3;      // 较贵
        return 4;                        // 昂贵
    }

    /**
     * 计算 Jaccard 用户重叠系数
     */
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
}