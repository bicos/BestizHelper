package com.pockru.bestizhelper.imgur.login;

public class ImgurAuthorization {

    private static final String TAG = ImgurAuthorization.class.getSimpleName();

    private static ImgurAuthorization INSTANCE;

    static final String SHARED_PREFERENCES_NAME = "imgur_example_auth";
    
    private ImgurAuthorization() {}

    public static ImgurAuthorization getInstance() {
        if (INSTANCE == null)
            INSTANCE = new ImgurAuthorization();
        return INSTANCE;
    }

//    public boolean isLoggedIn() {
//        Context context = BestizBoxApplication.getAppContext();
//        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
//        return !TextUtils.isEmpty(prefs.getString("access_token", null));
//    }

//    public void addToHttpURLConnection(HttpURLConnection conn) {
//        Context context = BestizBoxApplication.getAppContext();
//        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
//        String accessToken = prefs.getString("access_token", null);
//
//        if (!TextUtils.isEmpty(accessToken)) {
//            conn.setRequestProperty("Authorization", "Bearer " + accessToken);
//        }
//        else {
//            conn.setRequestProperty("Authorization", "Client-ID " + Constants.MY_IMGUR_CLIENT_ID);
//        }
//    }

//    public void saveRefreshToken(String refreshToken, String accessToken, long expiresIn) {
//        Context context = BestizBoxApplication.getAppContext();
//        context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0)
//                .edit()
//                .putString("access_token", accessToken)
//                .putString("refresh_token", refreshToken)
//                .putLong("expires_in", expiresIn)
//                .commit();
//    }

//    public String requestNewAccessToken() {
//        Context context = BestizBoxApplication.getAppContext();
//        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0);
//        String refreshToken = prefs.getString("refresh_token", null);
//
//        if (refreshToken == null) {
//            Log.w(TAG, "refresh token is null; cannot request access token. login first.");
//            return null;
//        }
//
//        // clear previous access token
//        prefs.edit().remove("access_token").commit();
//
//        HttpURLConnection conn = null;
//        try {
//            conn = (HttpURLConnection) new URL("https://api.imgur.com/oauth2/token").openConnection();
//            conn.setDoOutput(true);
//            conn.setRequestProperty("Authorization", "Client-ID " + Constants.MY_IMGUR_CLIENT_ID);
//
//            ArrayList<NameValuePair> nvps = new ArrayList<NameValuePair>();
//            nvps.add(new BasicNameValuePair("refresh_token", refreshToken));
//            nvps.add(new BasicNameValuePair("client_id", Constants.MY_IMGUR_CLIENT_ID));
//            nvps.add(new BasicNameValuePair("client_secret", Constants.MY_IMGUR_CLIENT_SECRET));
//            nvps.add(new BasicNameValuePair("grant_type", "refresh_token"));
//
//            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nvps);
//
//            OutputStream out = conn.getOutputStream();
//            entity.writeTo(out);
//            out.flush();
//            out.close();
//
//            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                InputStream in = conn.getInputStream();
//                handleAccessTokenResponse(in);
//                in.close();
//            }
//            else {
//                Log.i(TAG, "responseCode=" + conn.getResponseCode());
//                InputStream errorStream = conn.getErrorStream();
//                StringBuilder sb = new StringBuilder();
//                Scanner scanner = new Scanner(errorStream);
//                while (scanner.hasNext()) {
//                    sb.append(scanner.next());
//                }
//                scanner.close();
//                Log.i(TAG, "error response: " + sb.toString());
//                errorStream.close();
//            }
//
//            return prefs.getString("access_token", null);
//
//        } catch (Exception ex) {
//            Log.e(TAG, "Could not request new access token", ex);
//            return null;
//        } finally {
//            try {
//                conn.disconnect();
//            } catch (Exception ignore) {}
//        }
//    }

//    private void handleAccessTokenResponse(InputStream in) throws JSONException {
//        StringBuilder sb = new StringBuilder();
//        Scanner scanner = new Scanner(in);
//        while (scanner.hasNext()) {
//            sb.append(scanner.next());
//        }
//        scanner.close();
//
//        JSONObject root = new JSONObject(sb.toString());
//        String accessToken      = root.getString("access_token");
//        String refreshToken     = root.getString("refresh_token");
//        long expiresIn          = root.getLong("expires_in");
//        String tokenType        = root.getString("token_type");
//        String accountUsername  = root.getString("account_username");
//
//        Context context = BestizBoxApplication.getAppContext();
//        context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0)
//                .edit()
//                .putString("access_token", accessToken)
//                .putString("refresh_token", refreshToken)
//                .putLong("expires_in", expiresIn)
//                .putString("token_type", tokenType)
//                .putString("account_username", accountUsername)
//                .commit();
//    }

//    public void logout() {
//        Context context = BestizBoxApplication.getAppContext();
//        context.getSharedPreferences(SHARED_PREFERENCES_NAME, 0)
//                .edit()
//                .clear()
//                .commit();
//    }

}
