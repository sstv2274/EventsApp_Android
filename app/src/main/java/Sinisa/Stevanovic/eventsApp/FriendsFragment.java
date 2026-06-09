package Sinisa.Stevanovic.eventsApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment {

    private ListView lvFriendsActivity;
    private TextView tvEmptyFriends;
    private ArrayAdapter<String> adapter;
    private List<String> activityList;

    private DBHelper dbHelper;
    private String serverUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);

        lvFriendsActivity = view.findViewById(R.id.lvFriendsActivity);
        tvEmptyFriends = view.findViewById(R.id.tvEmptyFriends);

        dbHelper = new DBHelper(getActivity());
        activityList = new ArrayList<>();


        lvFriendsActivity.setEmptyView(tvEmptyFriends);

        SharedPreferences sp = getActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        String loggedInUser = sp.getString("LOGGED_IN_USER", "");
        serverUserId = dbHelper.getServerUserIdByUsername(loggedInUser);

        preuzmiAktivnostiPrijatelja();

        return view;
    }

    private void preuzmiAktivnostiPrijatelja() {
        if (serverUserId == null) {
            Toast.makeText(getActivity(), R.string.no_user_id, Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection urlConnection = null;
                try {
                    URL url = new URL("http://192.168.0.14:3000/friends-activity/" + serverUserId);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Content-Type", "application/json");

                    int responseCode = urlConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        br.close();

                        JSONArray jsonArray = new JSONArray(sb.toString());
                        activityList.clear();

                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            String username = obj.getString("username");
                            String eventName = obj.getString("eventName");
                            String commitment = obj.getString("commitment");

                            if ("PRISUSTVUJE".equals(commitment)) {
                                activityList.add(username + " is also attending " + eventName);
                            } else if ("ZAINTERESOVAN".equals(commitment)) {
                                activityList.add(username + " is also interested in " + eventName);
                            }
                        }

                        if (isAdded() && getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, activityList) {
                                        @NonNull
                                        @Override
                                        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                                            View view = super.getView(position, convertView, parent);
                                            TextView tvText = view.findViewById(android.R.id.text1);
                                            if (tvText != null) {
                                                tvText.setTextColor(androidx.core.content.ContextCompat.getColor(getContext(), R.color.text_dark_gray));

                                            }

                                            return view;
                                        }
                                    };
                                    lvFriendsActivity.setAdapter(adapter);
                                }
                            });
                        }
                    } else {
                        prikaziGresku(getString(R.string.server_error));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    prikaziGresku(getString(R.string.server_conn_error));
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                }
            }
        }).start();
    }

    private void prikaziGresku(final String poruka) {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), poruka, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}