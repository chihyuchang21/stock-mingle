package com.example.demo.service;
import com.example.demo.model.article.Article;
import com.example.demo.model.stock.StockInformation;
import com.example.demo.repository.StockInformationRepository;
import com.example.demo.repository.WebCrawlerRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class WebCrawlerService {

    private final WebCrawlerRepository webCrawlerRepository;
    public WebCrawlerService(WebCrawlerRepository webCrawlerRepository) {this.webCrawlerRepository = webCrawlerRepository; }

    @Autowired
    private StockInformationRepository stockInformationRepository;


    private static final Logger logger = LoggerFactory.getLogger(WebCrawlerService.class);


    public String getRedditArticles(String subreddit, int limit) throws IOException {
        // Reddit API
        String url = "https://www.reddit.com/r/" + subreddit + "/new.json?limit=" + limit + "&after=t3_1bdzb87";

        // Create OkHttpClient instance
        OkHttpClient client = new OkHttpClient();

        // Create HTTP request object
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Java Reddit Crawler")
                .build();

        // Send HTTP request and get response
        Response response = client.newCall(request).execute();

        // Check if response is successful
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        // Get response body content
        String responseBody = response.body().string();

        // Parse the JSON response to extract the "kind" value
//        String kind = extractKind(responseBody);
//        String distValue = extractDist(responseBody);
        // Parse the JSON response to extract self texts and titles
        List<String> selfTexts = extractSelfText(responseBody);
        List<String> titles = extractTitle(responseBody);

        // Save articles to database
        saveArticle(selfTexts, titles);


        logger.info("SelfText: " + selfTexts);
        logger.info("Title: " + titles);

        // Close the response
        response.close();

        return responseBody;
    }

    private static List<String> extractSelfText(String responseBody) {
        List<String> selfTextValues = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(responseBody);
            JSONObject data = json.getJSONObject("data");
            JSONArray children = data.getJSONArray("children");
            for (int i = 0; i < Math.min(children.length(), 500); i++) {
                JSONObject child = children.getJSONObject(i);
                JSONObject childData = child.getJSONObject("data");
                String selfTextValue = childData.getString("selftext");
                selfTextValues.add(selfTextValue);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return selfTextValues;
    }

    private static List<String> extractTitle(String responseBody) {
        List<String> titleValues = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(responseBody);
            JSONObject data = json.getJSONObject("data");
            JSONArray children = data.getJSONArray("children");
            for (int i = 0; i < Math.min(children.length(), 500); i++) {
                JSONObject child = children.getJSONObject(i);
                JSONObject childData = child.getJSONObject("data");
                String titleValue = childData.getString("link_flair_text"); //暫時改
                titleValues.add(titleValue);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return titleValues;
    }

    public void saveArticle(List<String> selfTexts, List<String> titles) {
        for (int i = 0; i < Math.min(selfTexts.size(), titles.size()); i++) {
            Article article = new Article();
            article.setContent(selfTexts.get(i));
            article.setTitle(titles.get(i));
            webCrawlerRepository.save(article);
        }
    }

    public void getStockGeneralIndex() throws IOException {
        crawlAndSaveIndex("Dow Jones Industrial Average", "https://finance.yahoo.com/quote/%5EDJI");
        crawlAndSaveIndex("S&P 500", "https://finance.yahoo.com/quote/%5EGSPC");
        crawlAndSaveIndex("NASDAQ Composite", "https://finance.yahoo.com/quote/%5EIXIC");
        crawlAndSaveIndex("Philadelphia Semiconductor Index", "https://finance.yahoo.com/quote/%5ESOX");
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
        }
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
        stockInformationRepository.save(stockInformation);
    }
}


