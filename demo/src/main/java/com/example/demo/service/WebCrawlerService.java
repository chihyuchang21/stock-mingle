package com.example.demo.service;

import com.example.demo.model.article.Article;
import com.example.demo.model.article.Category;
import com.example.demo.model.stock.StockInformation;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.StockInformationRepository;
import com.example.demo.repository.WebCrawlerRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class WebCrawlerService {

    private static final Logger logger = LoggerFactory.getLogger(WebCrawlerService.class);
    private final WebCrawlerRepository webCrawlerRepository;
    @Autowired
    private StockInformationRepository stockInformationRepository;

    @Autowired
    private CategoryRepository categoryRepository;


    public WebCrawlerService(WebCrawlerRepository webCrawlerRepository) {
        this.webCrawlerRepository = webCrawlerRepository;
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
                String linkFlairText = childData.getString("link_flair_text");
                // 只提取特定文章的 selftext
                if (isValidLinkFlairText(linkFlairText)) {
                    String selfTextValue = childData.getString("selftext");
                    selfTextValues.add(selfTextValue);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return selfTextValues;
    }

    private static List<String> extractTitle(String responseBody) {
        List<String> categoryValues = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(responseBody);
            JSONObject data = json.getJSONObject("data");
            JSONArray children = data.getJSONArray("children");
            for (int i = 0; i < Math.min(children.length(), 500); i++) {
                JSONObject child = children.getJSONObject(i);
                JSONObject childData = child.getJSONObject("data");
                String linkFlairText = childData.getString("link_flair_text");
                // 只存特定Categories的文章
                if (isValidLinkFlairText(linkFlairText)) {
                    String categoryValue = childData.getString("title");
                    categoryValues.add(categoryValue);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return categoryValues;
    }

    private static List<String> extractCategory(String responseBody) {
        List<String> titleValues = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(responseBody);
            JSONObject data = json.getJSONObject("data");
            JSONArray children = data.getJSONArray("children");
            for (int i = 0; i < Math.min(children.length(), 500); i++) {
                JSONObject child = children.getJSONObject(i);
                JSONObject childData = child.getJSONObject("data");
                String linkFlairText = childData.getString("link_flair_text");
                // 只提取特定文章的 title
                if (isValidLinkFlairText(linkFlairText)) {
                    String titleValue = childData.getString("link_flair_text");
                    titleValues.add(titleValue);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return titleValues;
    }

    // 檢查link_flair_text 是否為特定類型
    private static boolean isValidLinkFlairText(String linkFlairText) {
        return linkFlairText != null && (linkFlairText.equals("Company News") || linkFlairText.equals("Broad market news") ||
                linkFlairText.equals("Company Discussion") || linkFlairText.equals("Advice Request") || linkFlairText.equals("Others"));
    }

    public String getRedditArticles(String subreddit, int limit) throws IOException {
        String url = "https://www.reddit.com/r/" + subreddit + "/new.json?limit=" + limit;

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "Java Reddit Crawler")
                .build();

        Response response = client.newCall(request).execute();

        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }

        String responseBody = response.body().string();
        List<String> selfTexts = extractSelfText(responseBody);
        List<String> titles = extractCategory(responseBody);
        List<String> categories = extractTitle(responseBody);

        // 文章存入DB
        saveArticle(selfTexts, titles, categories);

        response.close();

        return responseBody;
    }

    public void saveArticle(List<String> selfTexts, List<String> categories, List<String> titles) {
        for (int i = 0; i < Math.min(selfTexts.size(), titles.size()); i++) {
            Article article = new Article();
            article.setTitle(titles.get(i));

            Category category = categoryRepository.findByCategory(categories.get(i));
            article.setCategory(category); // 設置 Category 物件到 Article 中

            article.setContent(selfTexts.get(i));

            webCrawlerRepository.save(article);
        }
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
        stockInformationRepository.save(stockInformation);
    }

//    private void retrieveFromDatabase
}


