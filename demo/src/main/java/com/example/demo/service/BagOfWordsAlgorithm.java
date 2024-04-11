package com.example.demo.service;

import java.util.Set;

import com.example.demo.controller.WebCrawlerController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BagOfWordsAlgorithm {

    private static final Logger logger = LoggerFactory.getLogger(BagOfWordsAlgorithm.class);


    public static double calculateSimilarity(Set<String> set1, Set<String> set2) {
        // 兩個set的交集大小
        int intersectionSize = getIntersectionSize(set1, set2);

        logger.info("intersectionSize: " + intersectionSize);

        // 兩個set的聯集大小
        int unionSize = set1.size() + set2.size() - intersectionSize;

        logger.info("unionSize: " + unionSize);

        // 計算相似度得分
        double similarityScore = (double) intersectionSize / unionSize;

        logger.info("similarityScore: " + similarityScore);

        return similarityScore;

    }

    private static int getIntersectionSize(Set<String> set1, Set<String> set2) {
        int count = 0;
        for (String word : set1) {
            if (set2.contains(word)) {
                count++;
            }
        }
        return count;
    }
}

