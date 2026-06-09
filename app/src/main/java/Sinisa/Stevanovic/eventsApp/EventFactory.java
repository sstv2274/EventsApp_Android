package Sinisa.Stevanovic.eventsApp;
// RA58/2023 Sinisa Stevanovic


public class EventFactory {
    //Kreira i zatim vraca RegularEvent
    public static Event createRegularEvent(String name, String description, String location, String dateTime, String category, int imageResId) {
        //Koristimo kontruktor Event koji ima podrazumevane vrednost za isPromoted i capacity.
        //!!//Posto u EventFactory konstruktoru ne prosledjujemo attendingCount,averageRatin i ratingCount stavio sam ih da budu 0
        return new Event(name, description, location, dateTime, category, imageResId, 0, 0.0, 0,false,"");//menjano
    }
    //Kreira i zatim kao povratnu vrednost prosledjuje konstruktor za promoted dogadjaje(Zbog toga prosledjujemo promoted kao true)
    public static Event createPromotedEvent(String name, String description, String location, String dateTime, String category, int imageResId, int capacity) {

        return new Event(name, description, location, dateTime, category, imageResId,
                true, capacity, 0, 0.0, 0,false,"");//menjano
    }
}