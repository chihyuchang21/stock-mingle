package com.example.demo.service;
import com.example.demo.model.article.Article;
import com.example.demo.repository.WebCrawlerRepository;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class WebCrawlerService {

    private final WebCrawlerRepository webCrawlerRepository;
    public WebCrawlerService(WebCrawlerRepository webCrawlerRepository) {this.webCrawlerRepository = webCrawlerRepository; }


    private static final Logger logger = LoggerFactory.getLogger(WebCrawlerService.class);


    public String getRedditArticles(String subreddit, int limit) throws IOException {
        // Reddit API
        String url = "https://www.reddit.com/r/" + subreddit + "/new.json?limit=" + limit + "&after=t3_1buu8k6";

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
                String titleValue = childData.getString("title");
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

    public String getStockGeneralIndex() throws IOException {

        String url = "https://www.marketwatch.com/investing/index/spx";

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
        return "S&P 500 Index" + ": " + parseIndexValue(responseBody);

    }

    private String parseIndexValue(String html) {
        Document doc = Jsoup.parse(html);
        Element indexElement = doc.selectFirst(".intraday__data");
        if (indexElement != null) {
            return indexElement.text();
        } else {
            return "Data not found";
        }
    }

}
