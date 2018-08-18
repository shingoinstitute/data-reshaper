package org.shingo.insightdatareshaper;

import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SalesforceConnector {
    private String accessToken;
    private String environment;

    SalesforceConnector(String Environment) {
        this.environment = Environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    private String buildSOQLQuery(String sobject, String[] fields, String clause) throws UnsupportedEncodingException {
        String query = "SELECT " + String.join(", ", fields) + " FROM " + sobject +
                (clause != null ? " WHERE " + clause : "");
        return "/services/data/v32.0/query?q=" + URLEncoder.encode(query, "UTF-8");
    }

    private JSONObject readStreamAsJSON(InputStream stream) throws IOException {
        BufferedReader streamReader = new BufferedReader((new InputStreamReader(stream, "UTF-8")));
        StringBuilder responseStrBuilder = new StringBuilder();
        String inputStr;
        while ((inputStr = streamReader.readLine()) != null)
            responseStrBuilder.append(inputStr);
        return new JSONObject(responseStrBuilder.toString());
    }

    private void setAuthorization(HttpURLConnection conn) {
        if (this.accessToken != null && !this.accessToken.equals("")) {
            conn.setRequestProperty("Authorization", "Bearer " + this.accessToken);
        }
    }

    private URL buildURL(String urlString) throws MalformedURLException {
        return new URL(this.environment + urlString);
    }

    private HttpURLConnection getConnection(String urlString, String method) throws IOException {
        URL url = this.buildURL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        return connection;
    }

    private JSONObject post(String urlString, JSONObject data) throws IOException {
        HttpURLConnection connection = getConnection(urlString, "POST");
        connection.setRequestProperty("Accept", "application/json");

        this.setAuthorization(connection);

        if (data != null) {
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);
            byte[] out = data.toString().getBytes(StandardCharsets.UTF_8);
            int length = out.length;
            connection.setFixedLengthStreamingMode(length);
            connection.connect();
            OutputStream os = connection.getOutputStream();
            os.write(out);
        }
        return this.readStreamAsJSON(connection.getInputStream());
    }

    private JSONObject get(String urlString) throws IOException {
        HttpURLConnection connection = getConnection(urlString, "GET");
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        this.setAuthorization(connection);
        return this.readStreamAsJSON(connection.getInputStream());
    }

    public JSONObject retrieve(String sobject, String id) throws IOException {
        String urlString = "/services/data/v32.0/sobjects/" + sobject + (id != null ? "/" + id : "");
        return this.get(urlString);
    }

    public JSONObject query(String sobject, String[] fields, String clause) throws IOException {
        String urlString = buildSOQLQuery(sobject, fields, clause);
        return this.get(urlString);
    }

    public String connect(String clientId,
                              String clientSecret,
                              String username,
                              String password) throws IOException {
        String urlString = String.format(
                "/services/oauth2/token?grant_type=password&client_id=%s&client_secret=%s&username=%s&password=%s",
                clientId, clientSecret, username, password
        );

        JSONObject ret = this.post(urlString, null);
        this.accessToken = ret.getString("access_token");
        System.out.println(this.accessToken);
        return accessToken;
    }
}
