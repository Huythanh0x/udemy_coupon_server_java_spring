package com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.crawler_runner.crawler;

import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.crawler_runner.base.CouponUrlCrawlerBase;
import com.huythanh0x.training_thanhvh_java_spring_jwt_jpa.crawler_runner.fetcher.WebContentFetcher;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RealDiscountCrawler extends CouponUrlCrawlerBase {
    int maxCouponRequest;
    String apiUrl;

    RealDiscountCrawler(@Value("${custom.number-of-real-discount-coupon}") int maxCouponRequest) {
        this.maxCouponRequest = maxCouponRequest;
        this.apiUrl = String.format("https://www.real.discount/api-web/all-courses/?store=Udemy&page=1&per_page=%s&orderby=undefined&free=0&search=&language=&cat=", maxCouponRequest);
    }

    @Override
    public List<String> getAllCouponUrls() {
        var jsonArray = fetchListJsonFromAPI(apiUrl);
        List<String> allUrls = new ArrayList<>();
        for (var jo : jsonArray) {
            JSONObject jsonObject = (JSONObject) jo;
            allUrls.add(extractCouponUrl(jsonObject));
        }
        System.out.println("Realdiscount jsonArray length: " + jsonArray.length() + " maxCouponRequest: " + maxCouponRequest);
        return allUrls;
    }

    String extractCouponUrl(JSONObject jsonObject) {
        return jsonObject.getString("url").replace("http://click.linksynergy.com/fs-bin/click?id=bnwWbXPyqPU&subid=&offerid=323058.1&type=10&tmpid=14537&RD_PARM1=", "");
    }

    public JSONArray fetchListJsonFromAPI(String apiUrl) {
        return new JSONArray(WebContentFetcher.getJsonObjectFrom(apiUrl).getJSONArray("results"));
    }
}
