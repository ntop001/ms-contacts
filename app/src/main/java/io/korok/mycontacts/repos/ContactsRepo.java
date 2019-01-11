package io.korok.mycontacts.repos;

import android.content.Context;
import android.util.DisplayMetrics;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import  java.lang.Math;

import io.korok.mycontacts.model.Contact;

public class ContactsRepo {
    /**
     * Returns fake data from assets.
     * @param context
     * @return
     */
    public static List<Contact> getFakeData(Context context) {
        if (context == null) {
            return Collections.emptyList();
        }
        InputStream in = null;
        try {
            in = context.getAssets().open("contacts.json");
            byte[] buffer = new byte[in.available()];
            in.read(buffer);
            String str = new String(buffer, "UTF-8");
            JSONArray array = new JSONArray(str);

            List<Contact> contacts = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.getJSONObject(i);
                Contact c = new Contact();
                c.firstName = json.optString("first_name");
                c.lastName  = json.optString("last_name");
                c.title     = json.optString("title");
                c.avatar    = getImageByDensity(context, json.optString("avatar_filename"));
                c.introduction = json.optString("introduction");
                contacts.add(c);
            }
            return contacts;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignore){}
            }
        }
        return Collections.emptyList();
    }

    /**
     * Returns the right image name by display density.
     * @param context
     * @param file
     * @return
     */
    static String getImageByDensity(Context context, String file) {
        if (file == null || context == null) {
            return null;
        }
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int level = Math.round(metrics.density);
        String name = file.substring(0, file.length()-4);
        switch (level) {
            case 3:
                name += "@3x.png";
                break;
            case 2:
                name += "@2x.png";
                break;
            case 1:
            default:
                name += ".png";
        }
        return name;
    }
}
