package com.andry.gitcompanysearch;

import android.net.Uri;

import com.andry.gitcompanysearch.model.CompanyItem;
import com.andry.gitcompanysearch.model.Repo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GitFetch {
    private static final String BASE_URL = "https://api.github.com/";
    private static final String SEARCH_PATH = "search";
    private static final String USER_PATH = "users";
    private static final String COMPANY_NAME_QUERY = "q";
    private static final String COMPANY_TYPE = " type:org";
    private static final String PAGE_QUERY = "page";
    private static final String PER_PAGE_QUERY = "per_page";
    private static final String ITEMS_PER_PAGE = "10";
    private static final String ORGS_PATH = "orgs";
    private static final String REPOS_PATH = "repos";

    public String getUrlString(String urlSpec) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(urlSpec)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public List<CompanyItem> downloadAllCompaniesInfo(String companyName, int page) throws IOException, JSONException {
        List<CompanyItem> companies = new ArrayList<>();

        String urlString = Uri.parse(BASE_URL)
                .buildUpon()
                .appendPath(SEARCH_PATH)
                .appendPath(USER_PATH)
                .appendQueryParameter(COMPANY_NAME_QUERY, companyName + COMPANY_TYPE)
                .appendQueryParameter(PAGE_QUERY, String.valueOf(page))
                .appendQueryParameter(PER_PAGE_QUERY, ITEMS_PER_PAGE)
                .build().toString();
        String jsonString = getUrlString(urlString);
        parseAllCompanies(companies, jsonString);
        return companies;
    }

    public CompanyItem fetchCompanyItem(CompanyItem item) {
        String url = Uri.parse(BASE_URL)
                .buildUpon()
                .appendPath(ORGS_PATH)
                .appendPath(item.getName())
                .build().toString();
        try {
            String jsonString = getUrlString(url);
            return parseCompany(item, jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return item;
    }

    private void parseAllCompanies(List<CompanyItem> companies, String jsonString) throws JSONException {
        Gson gson = new Gson();
        JSONObject jsonBody = new JSONObject(jsonString);
        JSONArray jsonArray = jsonBody.getJSONArray("items");
        Type companyType = new TypeToken<ArrayList<CompanyItem>>() {
        }.getType();
        List<CompanyItem> companyItemsTemp = gson.fromJson(jsonArray.toString(), companyType);
        companies.addAll(companyItemsTemp);
    }

    private CompanyItem parseCompany(CompanyItem item, String jsonString) {
        Gson gson = new Gson();
        item = gson.fromJson(jsonString, CompanyItem.class);
        return item;
    }

    public List<Repo> fetchRepos(String companyName, int page) throws JSONException, IOException {
        String urlString = Uri.parse(BASE_URL)
                .buildUpon()
                .appendPath(ORGS_PATH)
                .appendPath(companyName)
                .appendPath(REPOS_PATH)
                .appendQueryParameter(PAGE_QUERY, String.valueOf(page))
                .appendQueryParameter(PER_PAGE_QUERY, ITEMS_PER_PAGE)
                .build().toString();
        String jsonString = getUrlString(urlString);
        return parseRepos(jsonString);
    }

    private List<Repo> parseRepos(String jsonString) throws JSONException {
        JSONArray jsonArray = new JSONArray(jsonString);
        Gson gson = new Gson();
        Type reposType = new TypeToken<ArrayList<Repo>>() {
        }.getType();
        List<Repo> reposJson = gson.fromJson(jsonArray.toString(), reposType);
        List<Repo> repos = new ArrayList<>();
        repos.addAll((reposJson));
        return repos;
    }
}
