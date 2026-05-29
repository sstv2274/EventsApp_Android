package Sinisa.Stevanovic.eventsApp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "EventsApp.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "server_id TEXT UNIQUE, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "email TEXT UNIQUE NOT NULL, " +
                    "lozinka TEXT NOT NULL" +
                    ");";

    private static final String CREATE_TABLE_EVENTS =
            "CREATE TABLE events (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "server_id TEXT UNIQUE, " +
                    "naziv TEXT NOT NULL, " +
                    "opis TEXT, " +
                    "lokacija TEXT NOT NULL, " +
                    "datumVreme TEXT NOT NULL, " +
                    "kategorija TEXT NOT NULL, " +
                    "imageResId INTEGER, " +
                    "promoted INTEGER DEFAULT 0 CHECK(promoted IN (0, 1)), " +
                    "kapacitet INTEGER DEFAULT 0 CHECK(kapacitet >= 0), " +
                    "brojPrisutnih INTEGER DEFAULT 0 CHECK(brojPrisutnih >= 0), " +
                    "prosecnaOcena REAL DEFAULT 0 CHECK(prosecnaOcena BETWEEN 0 AND 5), " +
                    "brojOcena INTEGER DEFAULT 0 CHECK(brojOcena >= 0), " +
                    "CHECK(promoted = 0 OR kapacitet > 0), " +
                    "CHECK(promoted = 0 OR brojPrisutnih <= kapacitet)" +
                    ");";

    private static final String CREATE_TABLE_ATTENDANCE =
            "CREATE TABLE attendance (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "userId INTEGER NOT NULL, " +
                    "eventId INTEGER NOT NULL, " +
                    "prisustvo TEXT NOT NULL CHECK(prisustvo IN ('ZAINTERESOVAN', 'PRISUSTVUJE')), " +
                    "FOREIGN KEY(userId) REFERENCES users(id), " +
                    "FOREIGN KEY(eventId) REFERENCES events(id), " +
                    "UNIQUE(userId, eventId)" +
                    ");";

    private static final String CREATE_TABLE_RATINGS =
            "CREATE TABLE ratings (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "userId INTEGER NOT NULL, " +
                    "eventId INTEGER NOT NULL, " +
                    "rating INTEGER NOT NULL CHECK(rating BETWEEN 1 AND 5), " +
                    "FOREIGN KEY(userId) REFERENCES users(id), " +
                    "FOREIGN KEY(eventId) REFERENCES events(id), " +
                    "UNIQUE(userId, eventId)" +
                    ");";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_EVENTS);
        db.execSQL(CREATE_TABLE_ATTENDANCE);
        db.execSQL(CREATE_TABLE_RATINGS);

        addInitialEvents(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS events");
        db.execSQL("DROP TABLE IF EXISTS attendance");
        db.execSQL("DROP TABLE IF EXISTS ratings");
        onCreate(db);
    }

    @Override
    public void onConfigure (SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }


    public long registerUser(String serverId, String username, String email, String password) {

        String hashedPassword = PasswordHasher.hashPassword(password);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("server_id",serverId);
        values.put("username", username);
        values.put("email", email);
        values.put("lozinka", hashedPassword);

        return db.insert("users", null, values);
    }


    public String checkUserLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("users",
                new String[]{"email", "lozinka"},
                "username=?",
                new String[]{username},
                null, null, null);

        String storedEmail = null;

        if (cursor != null && cursor.moveToFirst()) {
            storedEmail = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String storedPasswordData = cursor.getString(cursor.getColumnIndexOrThrow("lozinka"));
            cursor.close();


            if (!PasswordHasher.verifyPassword(password, storedPasswordData)) {
                storedEmail = null;
            }
        } else {
            if (cursor != null) cursor.close();
        }

        if (db != null) {
            db.close();
        }
        if (cursor != null) {
            cursor.close();
        }
        return storedEmail;

    }

    public boolean changePassword(String username, String currentPassword, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();

        //Prvo nalazim korisnika u abzi
        Cursor cursor = db.query("users", new String[]{"lozinka"}, "username=?", new String[]{username}, null, null, null);

        boolean isSuccess = false;
        if (cursor != null && cursor.moveToFirst()) {
            String storedPasswordData = cursor.getString(cursor.getColumnIndexOrThrow("lozinka"));
            //provera sifre
            if (PasswordHasher.verifyPassword(currentPassword, storedPasswordData)) {
                //ako je uneta tacna sifra hash-uje novu sifru i menja je u db
                String newHashedPassword = PasswordHasher.hashPassword(newPassword);

                ContentValues values = new ContentValues();
                values.put("lozinka", newHashedPassword);

                int rowsAffected = db.update("users", values, "username=?", new String[]{username});

                if (rowsAffected == 1) {
                    isSuccess = true;
                }
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return isSuccess;
    }
    public void addInitialEvents(SQLiteDatabase db) {
        // Provera da li je tabela već puna da ne bi duplirali podatke
        Cursor cursor = db.rawQuery("SELECT count(*) FROM events", null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();



        if (count == 0) { // Ako je prazna, ubaci podatke
            insertEvent(db,null, "Klen fishing Cup Loznica", "Varalicarenje klena", "Mali Zvornik, Drina", "20/06/2026 05:00", "FISHING", R.drawable.klen_cup_loznica, 1, 65);
            insertEvent(db,null,  "Zimsko Smudjarenje Cup", "Dzigovanje smudja u ranoj zimi", "Futog,Dunav", "15/12/2025 07:00", "FISHING", R.drawable.zimsko_smudjarenje, 0, 0);
            insertEvent(db,null,  "Skobaljijada Loznica", "Uzivanje na Drini", "Loznica,Drina, Zicina plaza", "10/06/2026 06:00", "FISHING", R.drawable.skobaljijada_loznica, 0, 0);

            insertEvent(db,null,  "Rostilj i pivo u Velikoj reci", "Pecanje i odmor", "Vikendica na Drini", "01/05/2026 10:00", "NAPIVO", R.drawable.velika_reka, 0, 0);
            insertEvent(db,null,  "Beer fest", "Testiranje piva", "Novi Sad,Master hala", "15/05/2026 18:00", "NAPIVO", R.drawable.beer_fest, 0, 0);
            insertEvent(db,null,  "Degustacija Krafta", "Proba novih piva", "Pivoteka 77", "20/05/2026 20:00", "NAPIVO", R.drawable.kraft_piva, 0, 0);
            insertEvent(db,null,  "Oktoberfest NS", "Veliki festival piva", "Novosadski sajam", "10/10/2026 12:00", "NAPIVO", R.drawable.novosadski_oktobarfest, 1, 5000);

            insertEvent(db,null,  "Brucosijada", "Zurka", "Dva Galeba", "15/10/2030 22:00", "PARTY", R.drawable.brucosijada, 0, 0);
            insertEvent(db,null,  "Apsolventsko vece", "Proslava kraja studija", "Hotel Zvezda", "10/06/2027 21:00", "PARTY", R.drawable.apsolventsko_vece, 0, 0);
            insertEvent(db,null,  "Gustiranje Duskove rakije", "Uzivo Sejo kalac i Minela", "Djukin stan", "25/05/2026 20:30", "PARTY", R.drawable.duskova_rakija, 0, 0);
            insertEvent(db,null,  "Zurka 80-te", "Jaka muzika", "Gerila", "20/07/2026 21:00", "PARTY", R.drawable.zurka_80_te, 0, 0);

            insertEvent(db,null,  "EXIT Festival", "Najveći muzički festival", "Petrovaradinska tvrđava", "09/07/2026 20:00", "FESTIVAL", R.drawable.exit, 1, 40000);
            insertEvent(db,null,  "Beer Fest 2025", "Beogradski festival piva", "Ušće", "15/08/2025 18:00", "FESTIVAL", R.drawable.beer_fest2025, 0, 0);
            insertEvent(db,null,  "Arsenal Fest", "Festival u Kragujevcu", "Knežev arsenal", "25/06/2026 19:00", "FESTIVAL", R.drawable.arsenal_fest, 0, 0);
            insertEvent(db,null,  "LoveFest", "Festival elektronske muzike", "Vrnjačka Banja", "05/03/2026 20:00", "FESTIVAL", R.drawable.love_fest, 0, 0);

            insertEvent(db,null,  "Posledni program tvog kompjutera", "Oprostajni kocnert DENIS&DENIS", "SKC Fabrika", "21/03/2026 21:00", "CONCERT", R.drawable.poslednji_program, 0, 0);
            insertEvent(db,null,  "Bajaga i Instruktori", "Mid", "Spens", "30/05/2026 21:00", "CONCERT", R.drawable.bajaga, 1, 10000);
            insertEvent(db,null,  "Tap011", "Negde u daljini jedna reka protice", "Plato Tekstila", "05/05/2026 21:00", "CONCERT", R.drawable.tap011, 0, 0);
        }

    }
    private void insertEvent(SQLiteDatabase db, String server, String naziv, String opis, String lokacija, String datum, String kategorija, int imageId, int promoted, int kapacitet) {
        ContentValues values = new ContentValues();
        values.put("naziv", naziv);
        values.put("server_id", server);
        values.put("opis", opis);
        values.put("lokacija", lokacija);
        values.put("datumVreme", datum);
        values.put("kategorija", kategorija);
        values.put("imageResId", imageId);
        values.put("promoted", promoted);
        values.put("kapacitet", kapacitet);
        db.insert("events", null, values);
    }

    public void syncEventFromServer(String serverId, String naziv, String opis, String lokacija, String datum, String kategorija, int promoted, int kapacitet, int brojPrisutnih, double prosecnaOcena, int brojOcena) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("server_id", serverId);
        values.put("naziv", naziv);
        values.put("opis", opis);
        values.put("lokacija", lokacija);
        values.put("datumVreme", datum);
        values.put("kategorija", kategorija);
        values.put("promoted", promoted);
        values.put("kapacitet", kapacitet);
        values.put("brojPrisutnih", brojPrisutnih);
        values.put("prosecnaOcena", prosecnaOcena);
        values.put("brojOcena", brojOcena);

        Cursor cursor = db.query("events", new String[]{"id"}, "server_id = ?", new String[]{serverId}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            db.update("events", values, "server_id = ?", new String[]{serverId});
            cursor.close();
        } else {
            if (cursor != null) cursor.close();

            Cursor nameCursor = db.query("events", new String[]{"id"}, "naziv = ? AND server_id IS NULL", new String[]{naziv}, null, null, null);
            if (nameCursor != null && nameCursor.moveToFirst()) {
                db.update("events", values, "naziv = ? AND server_id IS NULL", new String[]{naziv});
                nameCursor.close();
            } else {
                if (nameCursor != null) nameCursor.close();
                values.put("imageResId", R.drawable.default_picture);
                db.insert("events", null, values);
            }
        }
        db.close();
    }

    public long addEvent(String server, String naziv, String opis, String lokacija, String datum, String kategorija, int imageId, int promoted, int kapacitet) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("server_id", server);
        values.put("naziv", naziv);
        values.put("opis", opis);
        values.put("lokacija", lokacija);
        values.put("datumVreme", datum);
        values.put("kategorija", kategorija);
        values.put("imageResId", imageId);
        values.put("promoted", promoted);
        values.put("kapacitet", kapacitet);

        long id = db.insert("events", null, values);

        db.close();
        return id;
    }

    // Metoda za dobavljanje svih dogadjaja iz baze
    public List<Event> getAllEvents() {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        //"SELECT * FROM events ORDER BY promoted DESC"
        // Promoted događaji će biti prvi u listi
        Cursor cursor = db.query("events", null, null, null, null, null, "promoted DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                eventList.add(cursorToEvent(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return eventList;
    }



    // Pomoćna metoda koja pretvara jedan red iz baze u Event objekat
    private Event cursorToEvent(Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndexOrThrow("naziv"));
        String desc = cursor.getString(cursor.getColumnIndexOrThrow("opis"));
        String location = cursor.getString(cursor.getColumnIndexOrThrow("lokacija"));
        String dateTime = cursor.getString(cursor.getColumnIndexOrThrow("datumVreme"));
        String category = cursor.getString(cursor.getColumnIndexOrThrow("kategorija"));
        int imageResId = cursor.getInt(cursor.getColumnIndexOrThrow("imageResId"));
        boolean isPromoted = cursor.getInt(cursor.getColumnIndexOrThrow("promoted")) == 1;
        int capacity = cursor.getInt(cursor.getColumnIndexOrThrow("kapacitet"));
        int attendingCount = cursor.getInt(cursor.getColumnIndexOrThrow("brojPrisutnih"));
        double averageRating = cursor.getDouble(cursor.getColumnIndexOrThrow("prosecnaOcena"));
        int ratingCount = cursor.getInt(cursor.getColumnIndexOrThrow("brojOcena"));

        if (isPromoted) {
            return new Event(name, desc, location, dateTime, category, imageResId, true, capacity, attendingCount, averageRating, ratingCount);
        } else {
            return new Event(name, desc, location, dateTime, category, imageResId, attendingCount, averageRating, ratingCount);
        }
    }

    public List<Event> getEventsByCategory(String category) {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("events", null, "UPPER(kategorija) = UPPER(?)", new String[]{category}, null, null, "promoted DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                eventList.add(cursorToEvent(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return eventList;
    }

    public String getServerUserIdByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"server_id"}, "username=?", new String[]{username}, null, null, null);
        String serverId = null;
        if (cursor != null && cursor.moveToFirst()) {
            serverId = cursor.getString(cursor.getColumnIndexOrThrow("server_id"));
            cursor.close();
        }
        db.close();
        return serverId;
    }
    
    public String getServerEventIdByName(String eventName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("events", new String[]{"server_id"}, "naziv=?", new String[]{eventName}, null, null, null);
        String serverId = null;
        if (cursor != null && cursor.moveToFirst()) {
            serverId = cursor.getString(cursor.getColumnIndexOrThrow("server_id"));
            cursor.close();
        }
        db.close();
        return serverId;
    }

    public Event findEventByName(String eventName) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query("events", null, "naziv=?", new String[]{eventName}, null, null, null);

        Event event = null;
        if (cursor != null && cursor.moveToFirst()) {
            event = cursorToEvent(cursor);
            cursor.close();
        }
        db.close();
        return event;
    }

    public int getUserIdByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"id"}, "username=?", new String[]{username}, null, null, null);
        int userId = -1;
        if (cursor != null && cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            cursor.close();
        }
        db.close();
        return userId;
    }

    public boolean handleAttendance(int userId, String eventName, String noviStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean uspeh = false;

        //Pronalazimo ID dogadjaja i proveravamo kapacitet
        Cursor eventCursor = db.query("events", new String[]{"id", "promoted", "kapacitet", "brojPrisutnih"}, "naziv=?", new String[]{eventName}, null, null, null);
        if (eventCursor == null || !eventCursor.moveToFirst()) {
            if (eventCursor != null) eventCursor.close();
            db.close();
            return false;
        }

        int eventId = eventCursor.getInt(eventCursor.getColumnIndexOrThrow("id"));
        int promoted = eventCursor.getInt(eventCursor.getColumnIndexOrThrow("promoted"));
        int kapacitet = eventCursor.getInt(eventCursor.getColumnIndexOrThrow("kapacitet"));
        int brojPrisutnih = eventCursor.getInt(eventCursor.getColumnIndexOrThrow("brojPrisutnih"));
        eventCursor.close();

        // Provera slobodnih mesta ako je dogadjaj promoted i ako zeli korisnik da prisustvuje
        if (noviStatus.equals("PRISUSTVUJE") && promoted == 1 && brojPrisutnih >= kapacitet) {
            db.close();
            return false; //nema slobodnih mesta
        }

        // Provera da li zapis vec postoji
        Cursor attendanceCursor = db.query("attendance", new String[]{"prisustvo"}, "userId=? AND eventId=?", new String[]{String.valueOf(userId), String.valueOf(eventId)}, null, null, null);

        String stariStatus = null;
        if (attendanceCursor != null && attendanceCursor.moveToFirst()) {
            stariStatus = attendanceCursor.getString(attendanceCursor.getColumnIndexOrThrow("prisustvo"));
            attendanceCursor.close();
        }

        if (stariStatus != null) {
            // radimo apdejt akos e status promenio
            if (!stariStatus.equals(noviStatus)) {
                ContentValues values = new ContentValues();
                values.put("prisustvo", noviStatus);
                db.update("attendance", values, "userId=? AND eventId=?", new String[]{String.valueOf(userId), String.valueOf(eventId)});
                uspeh = true;

                //Ako je presao sa zainteresovan an prisustvuje povecaj broj prisutnih u tabeli events.
                if (stariStatus.equals("ZAINTERESOVAN") && noviStatus.equals("PRISUSTVUJE")) {
                    db.execSQL("UPDATE events SET brojPrisutnih = brojPrisutnih + 1 WHERE id = " + eventId);
                }
            } else {
                uspeh = true; // vec je prijavljen sa tim statusom
            }
        } else {
            //Zapis ne postoji pa radimo insert
            ContentValues values = new ContentValues();
            values.put("userId", userId);
            values.put("eventId", eventId);
            values.put("prisustvo", noviStatus);
            db.insert("attendance", null, values);
            uspeh = true;

            //Ako je odma izabrao da ucestvuje inkremetujemo broj pristunih.
            if (noviStatus.equals("PRISUSTVUJE")) {
                db.execSQL("UPDATE events SET brojPrisutnih = brojPrisutnih + 1 WHERE id = " + eventId);
            }
        }

        db.close();
        return uspeh;
    }

    public List<Event> getEventsForUserByStatus(int userId, String status) {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Sada prosledjujem sortiran niz u intrested uvek tako da na vrhu budu promoted tako da sam resio jedan bug
        String query = "SELECT e.* FROM events e " +
                "INNER JOIN attendance a ON e.id = a.eventId " +
                "WHERE a.userId = ? AND a.prisustvo = ? " +
                "ORDER BY e.promoted DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), status});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                eventList.add(cursorToEvent(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return eventList;
    }

    public boolean handleRating(int userId, String eventName, int ratingValue) {
        SQLiteDatabase db = this.getWritableDatabase();

        //Prvo pronalazim id
        Cursor eventCursor = db.query("events", new String[]{"id"}, "naziv=?", new String[]{eventName}, null, null, null);
        if (eventCursor == null || !eventCursor.moveToFirst()) {
            if (eventCursor != null) eventCursor.close();
            db.close();
            return false;
        }
        int eventId = eventCursor.getInt(eventCursor.getColumnIndexOrThrow("id"));
        eventCursor.close();

       //Provera da li je vec ocenio dogadjaj
        Cursor ratingCursor = db.query("ratings", new String[]{"id"}, "userId=? AND eventId=?",
                new String[]{String.valueOf(userId), String.valueOf(eventId)}, null, null, null);

        if (ratingCursor != null && ratingCursor.moveToFirst()) {
            // Ako je vec ocenjivano onda samo radimo update
            ContentValues values = new ContentValues();
            values.put("rating", ratingValue);
            db.update("ratings", values, "userId=? AND eventId=?", new String[]{String.valueOf(userId), String.valueOf(eventId)});
            ratingCursor.close();
        } else {
            // Nije ocenjivao tako da radimo insert
            ContentValues values = new ContentValues();
            values.put("userId", userId);
            values.put("eventId", eventId);
            values.put("rating", ratingValue);
            db.insert("ratings", null, values);
            if (ratingCursor != null) ratingCursor.close();
        }

        // Preracunavanje proseka
        Cursor statsCursor = db.rawQuery("SELECT COUNT(*), AVG(rating) FROM ratings WHERE eventId = ?", new String[]{String.valueOf(eventId)});
        if (statsCursor != null && statsCursor.moveToFirst()) {
            int brojOcena = statsCursor.getInt(0);
            double prosecnaOcena = statsCursor.getDouble(1);
            statsCursor.close();

            //azuriranje tabele
            ContentValues eventValues = new ContentValues();
            eventValues.put("brojOcena", brojOcena);
            eventValues.put("prosecnaOcena", prosecnaOcena);
            db.update("events", eventValues, "id = ?", new String[]{String.valueOf(eventId)});
        }

        db.close();
        return true;
    }

    public int getUserRatingForEvent(int userId, String eventName) {
        SQLiteDatabase db = this.getReadableDatabase();
        //trazimo id iventa
        Cursor eventCursor = db.query("events", new String[]{"id"}, "naziv=?", new String[]{eventName}, null, null, null);
        if (eventCursor == null || !eventCursor.moveToFirst()) {
            if (eventCursor != null) eventCursor.close();
            db.close();
            return 0;
        }
        //UZimam id i zatvaram kursor
        int eventId = eventCursor.getInt(eventCursor.getColumnIndexOrThrow("id"));
        eventCursor.close();

        //Trazenje ocene u tabeli
        Cursor cursor = db.query("ratings", new String[]{"rating"}, "userId=? AND eventId=?",
                new String[]{String.valueOf(userId), String.valueOf(eventId)}, null, null, null);
        //Izvlacenje ocene
        int postojecaOcena = 0;
        if (cursor != null && cursor.moveToFirst()) {
            postojecaOcena = cursor.getInt(cursor.getColumnIndexOrThrow("rating"));
            cursor.close();
        }
        db.close();
        return postojecaOcena;
    }
}