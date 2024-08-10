package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;


public class BagOfWordsAlgorithm {

    private static final Logger logger = LoggerFactory.getLogger(BagOfWordsAlgorithm.class);


    public static double calculateSimilarity(Set<String> set1, Set<String> set2) {
        // Size of the intersection of the two sets
        int intersectionSize = getIntersectionSize(set1, set2);

        // Size of the union of the two sets
        int unionSize = set1.size() + set2.size() - intersectionSize;

        // Calculate the similarity score
        double similarityScore = (double) intersectionSize / unionSize;

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

