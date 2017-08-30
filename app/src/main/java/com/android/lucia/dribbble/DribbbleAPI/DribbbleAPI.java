package com.android.lucia.dribbble.DribbbleAPI;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.lucia.dribbble.model.Bucket;
import com.android.lucia.dribbble.model.Like;
import com.android.lucia.dribbble.model.Shot;
import com.android.lucia.dribbble.model.User;
import com.android.lucia.dribbble.utils.Utils;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;
import static com.android.lucia.dribbble.R.string.bucket;
import static com.android.lucia.dribbble.authenticate.AuthActivity.KEY_CODE;
import static com.android.lucia.dribbble.view.shot_detail.ShotFragment.KEY_SHOT;

public class DribbbleAPI {

    private static final String API_URL = "https://api.dribbble.com/v1/";

    private static final String USER_END_POINT = API_URL + "user";
    private static final String SHOTS_END_POINT = API_URL + "shots";
    private static final String BUCKETS_END_POINT = API_URL + "buckets";

    private static final String SP_AUTH = "auth";

    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER = "user";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_SHOT_ID = "shot_id";

    public static final int COUNT_PER_PAGE = 12;

    private static OkHttpClient client = new OkHttpClient();

    private static String accessToken;
    private static User user;

    public static boolean LoggedIn(@NonNull Context context) {
        accessToken = readAccessToken(context);

        if (accessToken != null) {
            user = readUser(context);
            return true;
        }
        return false;
    }

    public static void Login(@NonNull Context context, @NonNull String accessToken) throws IOException, JsonSyntaxException {
        DribbbleAPI.accessToken = accessToken;
        saveAccessToken(context, accessToken);

        DribbbleAPI.user = getUser();
        saveUser(context, user);
    }

    public static void Logout(@NonNull Context context) {
        saveAccessToken(context, null);
        saveUser(context, null);

        accessToken = null;
        user = null;
    }

    public static User getCurrentUser() {
        return user;
    }

    public static User getUser() throws IOException {
        Response response = sendGetRequest(USER_END_POINT);
        User user = parseResponse(response, new TypeToken<User>(){});

        return user;
    }

    public static List<Shot> getShots(int page) throws IOException {
        String url = SHOTS_END_POINT + "?page=" + page;
        Response response = sendGetRequest(url);
        List<Shot> shots = parseResponse(response, new TypeToken<List<Shot>>(){});

        return shots;
    }

    public static List<Bucket> getUserBuckets() throws IOException {
        String url = USER_END_POINT + "/buckets?per_page=" + Integer.MAX_VALUE;
        Response response = sendGetRequest(url);
        List<Bucket> buckets = parseResponse(response, new TypeToken<List<Bucket>>(){});

        return buckets;
    }

    public static List<Bucket> getUserBuckets(int page) throws IOException {
        String url = USER_END_POINT + "/buckets?page=" + page;
        Response response = sendGetRequest(url);
        List<Bucket> buckets = parseResponse(response, new TypeToken<List<Bucket>>(){});

        return buckets;
    }

    public static List<Bucket> getShotBuckets(String shotId) throws IOException {
        String url = SHOTS_END_POINT + "/" + shotId + "/buckets?per_page=" + Integer.MAX_VALUE;
        Response response = sendGetRequest(url);
        List<Bucket> buckets = parseResponse(response, new TypeToken<List<Bucket>>(){});

        return buckets;
    }

    public static List<Shot> getBucketShots(String bucketId, int page) throws IOException {
        String url = BUCKETS_END_POINT + "/" + bucketId + "/shots?page=" + page;
        Response response = sendGetRequest(url);
        List<Shot> shots = parseResponse(response, new TypeToken<List<Shot>>(){});

        return shots;
    }

    public static Bucket createBucket(Bucket bucket) throws IOException {

        RequestBody body = new FormBody.Builder()
                .add(KEY_NAME, bucket.name)
                .add(KEY_DESCRIPTION, bucket.description)
                .build();

        Response response = sendPostRequest(BUCKETS_END_POINT, body);

        return parseResponse(response, new TypeToken<Bucket>(){});
    }

    public static void addShotToBucket(String shotId, String bucketId) throws IOException {
        RequestBody body = new FormBody.Builder()
                .add(KEY_SHOT_ID, shotId)
                .build();

        String url = BUCKETS_END_POINT + "/" + bucketId + "/shots";

        Response response = sendPutRequest(url, body);

        if (response.code() != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new IOException(response.message());
        }
    }

    public static void removeShotFromBucket(String shotId, String bucketId) throws IOException {
        RequestBody body = new FormBody.Builder()
                .add(KEY_SHOT_ID, shotId)
                .build();

        String url = BUCKETS_END_POINT + "/" + bucketId + "/shots";

        Response response = sendDeleteRequest(url, body);

        if (response.code() != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new IOException(response.message());
        }
    }

    public static void deleteBucket(String bucketId) throws IOException {
        String url = BUCKETS_END_POINT + "/" + bucketId;
        Response response = sendDeleteRequest(url);
    }

    public static List<Like> getLikes(int page) throws IOException {
        String url = USER_END_POINT + "/likes?page=" + page;
        Response response = sendGetRequest(url);
        List<Like> likes = parseResponse(response, new TypeToken<List<Like>>(){});

        return likes;
    }

    public static List<Shot> getLikedShots(int page) throws IOException {
        List<Like> likes = getLikes(page);

        List<Shot> likedShots = new ArrayList<>();
        for (Like like : likes) {
            likedShots.add(like.shot);
        }

        return likedShots;
    }

    public static Like likeShot(@NonNull String shotId) throws IOException {
        String url = SHOTS_END_POINT + "/" + shotId + "/like";
        Response response = sendPostRequest(url, new FormBody.Builder().build());

        if (response.code() != HttpURLConnection.HTTP_CREATED) {
            throw new IOException(response.message());
        }
        else {
            return parseResponse(response, new TypeToken<Like>(){});
        }
    }

    public static void unlikeShot(@NonNull String shotId) throws IOException {
        String url = SHOTS_END_POINT + "/" + shotId + "/like";
        Response response = sendDeleteRequest(url);

        if (response.code() != HttpURLConnection.HTTP_NO_CONTENT) {
            throw new IOException(response.message());
        }
    }

    public static boolean isLikingShot(@NonNull String shotId) throws IOException {
        String url = SHOTS_END_POINT + "/" + shotId + "/like";
        Response response = sendGetRequest(url);
        int responseCode = response.code();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            return true;
        }
        else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            return false;
        }
        else {
            throw new IOException(response.message());
        }
    }

    private static Response sendGetRequest(String url) throws IOException {
        Request request = new Request.Builder()
                          .addHeader("Authorization", "Bearer " + accessToken)
                          .url(url)
                          .build();

        Response response = client.newCall(request).execute();
        return response;
    }

    private static Response sendPostRequest(String url, RequestBody requestBody) throws IOException {
        Request request = new Request.Builder()
                          .addHeader("Authorization", "Bearer " + accessToken)
                          .url(url)
                          .post(requestBody)
                          .build();

        Response response = client.newCall(request).execute();
        return response;
    }

    private static Response sendPutRequest(String url, RequestBody requestBody) throws IOException {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + accessToken)
                .url(url)
                .put(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        return response;
    }

    private static Response sendDeleteRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + accessToken)
                .url(url)
                .delete()
                .build();

        Response response = client.newCall(request).execute();
        return response;
    }

    private static Response sendDeleteRequest(String url, RequestBody requestBody) throws IOException {
        Request request = new Request.Builder()
                .addHeader("Authorization", "Bearer " + accessToken)
                .url(url)
                .delete(requestBody)
                .build();

        Response response = client.newCall(request).execute();
        return response;
    }

    private static <T> T parseResponse(Response response,
                                       TypeToken<T> typeToken) throws IOException, JsonSyntaxException {
        String responseString = response.body().string();
        return Utils.toObject(responseString, typeToken);
    }

    private static String readAccessToken(@NonNull Context context) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(
                SP_AUTH, Context.MODE_PRIVATE);
        String access_token = sp.getString(KEY_ACCESS_TOKEN, null);

        return access_token;
    }

    private static void saveAccessToken(@NonNull Context context, @Nullable String token) {
        SharedPreferences sp = context.getApplicationContext().getSharedPreferences(
                SP_AUTH, Context.MODE_PRIVATE);
        sp.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    private static User readUser(@NonNull Context context) {
        return Utils.read(context, KEY_USER, new TypeToken<User>(){});
    }

    private static void saveUser(@NonNull Context context, @NonNull User user) {
        Utils.save(context, KEY_USER, user);
    }
}
