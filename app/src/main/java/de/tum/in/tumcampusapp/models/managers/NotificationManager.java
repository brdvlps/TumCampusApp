package de.tum.in.tumcampusapp.models.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import de.tum.in.tumcampusapp.models.GCMNotification;
import de.tum.in.tumcampusapp.models.GCMNotificationLocation;

public class NotificationManager {

    private static final String TABLE_NOTIFICATIONS = "notification";
    private static final String[] TABLE_NOTIFICATIONS_COLUMNS = new String[]{
            "notification", "type", "location", "name", "lon", "lat", "rad", "title",
            "description", "signature"};
    private final SQLiteDatabase db;
    private final Context context;

    public NotificationManager(Context context) {
        this.context = context;
        db = DatabaseManager.getDb(context);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NOTIFICATIONS +
                " (notification INTEGER UNIQUE, typ INTEGER, location INTEGER, name TEXT, lon REAL, " +
                "lat REAL, radius INTEGER, title TEXT, description TEXT, signature TEXT)");
    }

    public void replaceInto(GCMNotification note) {
        ContentValues cvs = new ContentValues();
        cvs.put("notification", note.getNotification());
        cvs.put("type", note.getType());
        cvs.put("location", note.getDescription());
        cvs.put("name", note.getLocation().getName());
        cvs.put("lon", note.getLocation().getLon());
        cvs.put("lat", note.getLocation().getLat());
        cvs.put("title", note.getTitle());
        cvs.put("description", note.getDescription());
        cvs.put("signature", note.getSignature());
        db.insertWithOnConflict(TABLE_NOTIFICATIONS, null, cvs, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public void replaceNotificationsInto(List<GCMNotification> notes) {
        for (GCMNotification note : notes) {
            this.replaceInto(note);
        }
    }

    public GCMNotification getNotification(int notificationId) {
        Cursor c = db.query(TABLE_NOTIFICATIONS, TABLE_NOTIFICATIONS_COLUMNS, "notification = ?",
                new String[]{Integer.toString(notificationId)}, null, null, null);
        c.moveToFirst();
        GCMNotification n;
        if (c.getCount() == 0) {

        } else {
            n = new GCMNotification(c.getInt(alarmColumns.id.ordinal()),
                    c.getInt(alarmColumns.type.ordinal()),
                    new GCMNotificationLocation(
                            c.getInt(alarmColumns.location.ordinal()),
                            c.getString(alarmColumns.locationName.ordinal()),
                            c.getDouble(alarmColumns.lon.ordinal()),
                            c.getDouble(alarmColumns.lat.ordinal()),
                            c.getInt(alarmColumns.rad.ordinal())),
                    c.getString(alarmColumns.title.ordinal()),
                    c.getString(alarmColumns.desc.ordinal()),
                    c.getString(alarmColumns.signature.ordinal()));
        }
        c.close();
        return null;
    }

    private enum alarmColumns {
        id, type, location, locationName, lon, lat, rad, title, desc, signature
    }
}
