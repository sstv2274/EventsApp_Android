package Sinisa.Stevanovic.eventsApp;

// RA58/2023 Sinisa Stevanovic

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Event {

    private String name;
    private String description;
    private String location;
    private String dateTime;
    private String category;//moguce vrednsoti: Party, Festival, Stand-up & Theatar, Concert, Exhibition
    private int imageResId;
    private boolean isPromoted;
    private int capacity;
    private int attendingCount;
    private double averageRating;
    private int ratingCount;

    //Kontrukstor za Promoted dogadjaje
    public Event(String name, String description, String location, String dateTime,
                 String category, int imageResId, boolean isPromoted, int capacity,
                 int attendingCount, double averageRating, int ratingCount) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.dateTime = dateTime;
        this.category = category;
        this.imageResId = imageResId;
        this.isPromoted = isPromoted;
        this.capacity = capacity;
        this.attendingCount = attendingCount;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
    }

    //Konstruktor za regularne dogadjaje
    public Event(String name, String description, String location, String dateTime,
                 String category, int imageResId, int attendingCount,
                 double averageRating, int ratingCount) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.dateTime = dateTime;
        this.category = category;
        this.imageResId = imageResId;
        this.isPromoted = false; // Bez isPromoted. Postavljeno na podrazumevanu vrednost false
        this.capacity = 0;       // Bez capacity. Postavljeno na podrazumevani kapacitet 0
        this.attendingCount = attendingCount;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
    }

    //Metoda koja vraca da li je dogadjaj prosao
    public boolean isPast() {
        // Format dan/mesec/godina sati:minuti
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        //zastita od pogresnog unosa datuma(Mora biti u formatu dd/MM/yyyy HH:mm kako bi pretvorilo u dobro vreme)
        try {
            Date eventDate = sdf.parse(this.dateTime);
            Date currentDate = new Date(); // Trenutno sistemsko vreme
            if (eventDate != null) {
                return eventDate.before(currentDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();//ispisuje gresku u konzolu. Ako dodam proveru pri unosenju datuma mogu ukloniti try/catch format.
        }
        return false;
    }

    // Metoda addRating() koja ažurira prosečnu ocenu i broj ocena
    public void addRating(int rating) {
        //racunamo sumu svih dosadasnjih ocena
        double totalScore = this.averageRating * this.ratingCount;
        totalScore += rating; //dodajemo novu ocenu na sumu svih dosadasnjih ocena
        this.ratingCount++;   //povecamo ukupan broj ocena za 1(nova ocena)
        this.averageRating = totalScore / this.ratingCount; //Racunamo novi prosek
    }

    //get i set metode.

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public boolean isPromoted() { return isPromoted; }
    public void setPromoted(boolean promoted) { isPromoted = promoted; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getAttendingCount() { return attendingCount; }
    public void setAttendingCount(int attendingCount) { this.attendingCount = attendingCount; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
}