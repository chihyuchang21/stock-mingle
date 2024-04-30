package com.example.demo.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class TestStockService {
    public static void main(String[] args) throws IOException {
        String url = "https://fred.stlouisfed.org/series/SP500";

        // 使用Jsoup獲取網頁HTML內容
        String html = fetchHtml(url);

        // 解析HTML並提取S&P 500指數
        double sp500Index = parseSP500Index(html);

        System.out.println("S&P 500每日指數: " + sp500Index);
    }

    private static String fetchHtml(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            return response.body().string();
        }
    }

    private static double parseSP500Index(String html) {
        Document doc = Jsoup.parse(html);

        // 找到包含S&P 500指數的元素
        Element sp500Element = doc.selectFirst(".series-meta-observation-value");

        if (sp500Element != null) {
            // 提取S&P 500指數
            String indexText = sp500Element.text();
            // 清理數字中的逗號
            indexText = indexText.replaceAll(",", "");
            // 轉換為double型別
            return Double.parseDouble(indexText);
        }

        // 如果未找到指數，返回0
        return 0.0;
    }
}

