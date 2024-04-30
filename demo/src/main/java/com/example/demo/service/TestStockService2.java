package com.example.demo.service;

import com.example.demo.model.stock.StockInformation;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;

public class TestStockService2 {

    private static final Logger logger = LoggerFactory.getLogger(TestStockService2.class);

    public static void main(String[] args) throws IOException {
        TestStockService2 scraper = new TestStockService2();
        scraper.getStockGeneralIndex();
    }

    public void getStockGeneralIndex() throws IOException {
        crawlAndSaveIndex("Dow Jones Industrial Average", "https://finance.yahoo.com/quote/%5EDJI");
        crawlAndSaveIndex("S&P 500", "https://finance.yahoo.com/quote/%5EGSPC");
        crawlAndSaveIndex("NASDAQ Composite", "https://finance.yahoo.com/quote/%5EIXIC");
        crawlAndSaveIndex("Philadelphia Semiconductor Index", "https://finance.yahoo.com/quote/%5ESOX");
//        crawlAndSaveIndex("Nikkei 225", "https://finance.yahoo.com/quote/%5EN225");
//        crawlAndSaveIndex("TSEC weighted index", "https://finance.yahoo.com/quote/%5ETWII");
    }

    private void crawlAndSaveIndex(String indexName, String url) throws IOException {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String responseBody = response.body().string();
            String indexValue = parseIndexValue(responseBody, url);
            saveToDatabase(indexName, indexValue);
        }
    }

    private String parseIndexValue(String responseBody, String url) {
        Document doc = Jsoup.parse(responseBody);
        Element priceElement = null;
        if (url.contains("%5EDJI")) {
            priceElement = doc.selectFirst("fin-streamer[data-symbol=^DJI]");
        } else if (url.contains("%5EGSPC")) {
            priceElement = doc.selectFirst("fin-streamer[data-symbol=^GSPC]");
        } else if (url.contains("%5EIXIC")) {
            priceElement = doc.selectFirst("fin-streamer[data-symbol=^IXIC]");
        } else if (url.contains("%5ESOX")) {
            priceElement = doc.selectFirst("fin-streamer[data-symbol=^SOX]");
//        } else if (url.contains("%5EN225")) {
//            priceElement = doc.selectFirst("fin-streamer[data-symbol=^N225]");
//        } else if (url.contains("%5ETWII")) {
//            priceElement = doc.selectFirst("fin-streamer[data-symbol=^TWII]");
        }

        logger.info("priceElement: " + priceElement);

        if (priceElement != null) {
            return priceElement.text();
        } else {
            return "Price not found";
        }
    }

    private void saveToDatabase(String indexName, String indexValue) {
        StockInformation stockInformation = new StockInformation();
        stockInformation.setName(indexName);
        stockInformation.setValue(indexValue);
        stockInformation.setTimestamp(LocalDateTime.now());
//        stockInformationRepository.save(stockInformation);
    }
}
