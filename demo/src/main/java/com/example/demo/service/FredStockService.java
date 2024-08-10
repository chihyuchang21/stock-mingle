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

        // Find the element containing the S&P 500 index
        Element sp500Element = doc.selectFirst(".series-meta-observation-value");

        if (sp500Element != null) {
            // Extract the S&P 500 index
            String indexText = sp500Element.text();
            // Remove commas from the number
            indexText = indexText.replaceAll(",", "");
            // Convert to double type
            return indexText;
        }

        // Return a message if the index is not found
        return "Can't find index";
    }

    public void getStockGeneralIndex() throws IOException {
        crawlAndSaveIndex("Dow Jones Industrial Average", "https://fred.stlouisfed.org/series/DJIA");
        crawlAndSaveIndex("S&P 500", "https://fred.stlouisfed.org/series/SP500");
        crawlAndSaveIndex("NASDAQ Composite", "https://fred.stlouisfed.org/series/NASDAQCOM");
    }


    private void saveToDatabase(String indexName, String indexValue) {
        StockInformation stockInformation = new StockInformation();
        stockInformation.setName(indexName);
        stockInformation.setValue(indexValue);
        stockInformation.setTimestamp(LocalDateTime.now());
        stockInformationRepository.save(stockInformation);
    }

}

