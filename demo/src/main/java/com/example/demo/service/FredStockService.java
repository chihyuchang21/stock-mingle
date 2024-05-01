package com.example.demo.service;

import com.example.demo.model.stock.StockInformation;
import com.example.demo.repository.StockInformationRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class FredStockService {

    @Autowired
    private StockInformationRepository stockInformationRepository;

    private String crawlAndSaveIndex(String indexName, String url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseBody = response.body().string();
            String indexValue = parseIndexValue(responseBody, url);
            System.out.println(indexName + " " + indexValue);

            saveToDatabase(indexName, indexValue);
        }
        return "Successfully save to DB!";
    }

    private String parseIndexValue(String responseBody, String url) {
        Document doc = Jsoup.parse(responseBody);

        // 找到包含S&P 500指數的元素
        Element sp500Element = doc.selectFirst(".series-meta-observation-value");

        if (sp500Element != null) {
            // 提取S&P 500指數
            String indexText = sp500Element.text();
            // 清理數字中的逗號
            indexText = indexText.replaceAll(",", "");
            // 轉換為double型別
            return indexText;
        }

        // 如果未找到指數，返回0
        return "Can't find index";
    }

    public void getStockGeneralIndex() throws IOException {
        crawlAndSaveIndex("Dow Jones Industrial Average", "https://fred.stlouisfed.org/series/DJIA");
        crawlAndSaveIndex("S&P 500", "https://fred.stlouisfed.org/series/SP500");
        crawlAndSaveIndex("NASDAQ Composite", "https://fred.stlouisfed.org/series/NASDAQCOM");
//        crawlAndSaveIndex("Philadelphia Semiconductor Index", "https://finance.yahoo.com/quote/%5ESOX");
    }

//    public void getStockGeneralIndexFromFred() throws IOException {
//        String url = "https://fred.stlouisfed.org/series/SP500";
//
//        // 使用Jsoup獲取網頁HTML內容
//        String html = fetchHtml(url);
//
//        // 解析HTML並提取S&P 500指數
//        double sp500Index = parseSP500Index(html);
//
//        System.out.println("S&P 500每日指數: " + sp500Index);
//    }

    private void saveToDatabase(String indexName, String indexValue) {
        StockInformation stockInformation = new StockInformation();
        stockInformation.setName(indexName);
        stockInformation.setValue(indexValue);
        stockInformation.setTimestamp(LocalDateTime.now());
        stockInformationRepository.save(stockInformation);
    }

}

