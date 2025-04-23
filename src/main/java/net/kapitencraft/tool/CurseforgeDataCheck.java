package net.kapitencraft.tool;

import com.google.gson.stream.JsonReader;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public class CurseforgeDataCheck {


    public static void main(String[] args) {
        try {
            String rawUrl = "https://api.curseforge.com/v1/games/432/versions";
            URL url = new URL(rawUrl);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            //connection.setDoOutput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("x-api-key", "f021e4bc-19b8-4615-8e1b-1f3b44f1890c");

            //response
            int response = connection.getResponseCode();

            InputStream dataStream;
            if (response != HttpsURLConnection.HTTP_OK) {
                System.err.println("failed: " + response);
                dataStream = connection.getErrorStream();
            } else {
                dataStream = connection.getInputStream();
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(dataStream));

            System.out.println("code: " + response);
            while (in.ready()) {
                System.out.println(in.readLine());
            }
            dataStream.close();
        } catch (Exception e) {
            System.err.println("Error accessing API:");
            e.printStackTrace(System.err);
        }
    }
}
