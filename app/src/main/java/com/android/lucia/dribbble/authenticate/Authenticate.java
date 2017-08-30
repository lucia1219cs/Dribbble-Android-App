package com.android.lucia.dribbble.authenticate;

import android.net.Uri;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Authenticate {

    private static final String KEY_CODE = "code";
    private static final String KEY_CLIENT_ID = "client_id";
    private static final String KEY_CLIENT_SECRET = "client_secret";
    private static final String KEY_REDIRECT_URI = "redirect_uri";
    private static final String KEY_SCOPE = "scope";
    private static final String KEY_ACCESS_TOKEN = "access_token";

    private static final String CLIENT_ID = "f39f108ce85ea2019d60880a4393f05325fd386ed0181306d1622a250c13adbf";
    private static final String CLIENT_SECRET = "31721450a39ed1ba6c9b779b22d135b73fb9c75ea1cf4c853ffaacc0ca14053e";
    private static final String SCOPE = "public+write";
    private static final String URI_AUTHORIZE = "https://dribbble.com/oauth/authorize";
    private static final String URI_TOKEN = "https://dribbble.com/oauth/token";
    protected static final String REDIRECT_URI = "http://www.dribbo.com";


    protected static String requestAuthorizeUrl() {
        String url = Uri.parse(URI_AUTHORIZE)
                .buildUpon()
                .appendQueryParameter(KEY_CLIENT_ID, CLIENT_ID)
                .build()
                .toString();

        // fix encode issue
        url += "&" + KEY_REDIRECT_URI + "=" + REDIRECT_URI;
        url += "&" + KEY_SCOPE + "=" + SCOPE;

        return url;
    }

    protected static String requestAccessToken(String accessCode) throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new FormBody.Builder()
                .add(KEY_CLIENT_ID, CLIENT_ID)
                .add(KEY_CLIENT_SECRET, CLIENT_SECRET)
                .add(KEY_CODE, accessCode)
                .add(KEY_REDIRECT_URI, REDIRECT_URI)
                .build();

        Request request = new Request.Builder()
                .url(URI_TOKEN)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();

        String responseString = response.body().string();

        try {
            JSONObject obj = new JSONObject(responseString);
            return obj.getString(KEY_ACCESS_TOKEN);
        }
        catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }
}
