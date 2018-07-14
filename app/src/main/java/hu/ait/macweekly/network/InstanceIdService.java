package hu.ait.macweekly.network;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Sammy F on 6/7/2018.
 */

public class InstanceIdService extends FirebaseInstanceIdService {

    private final static String API_URL = "https://fcm.googleapis.com/fcm/send";
    public final static String API_KEY = ""; //TODO: Add API Key

    public InstanceIdService() {
        super();
    }

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String token = FirebaseInstanceId.getInstance().getToken();

        sendToServer(token);
    }

    private void sendToServer(String token) {

        try {

            URL url = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setDoInput(true);

            connection.setRequestMethod("POST");

            DataOutputStream stream = new DataOutputStream(connection.getOutputStream());

            stream.writeBytes("token=" + token);

            connection.connect();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream.close();
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();

        } catch (IOException ee) {
            ee.printStackTrace();

        }
    }
}
