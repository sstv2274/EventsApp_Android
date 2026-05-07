package Sinisa.Stevanovic.eventsApp;

// RA58/2023 Sinisa Stevanovic

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppData {

    //Staticke liste tipa Event
    public static List<Event> allEvents = new ArrayList<>();
    public static List<Event> interestedEvents = new ArrayList<>();
    public static List<Event> attendingEvents = new ArrayList<>();

    //Staticka inicijalizacija pocetnih listi
    static {
        // Kreiram 18 dogadjaja uz pomoc EventFactory
        //!!// Koristimo 0 za imageResId kao placeholder dok ne ubacim prave slike

        // Pecanje
        Event e1 = EventFactory.createPromotedEvent("Klen fishing Cup Loznica", "Varalicarenje klena", "Mali Zvornik, Drina", "20/06/2026 05:00", "Fishing", 0, 65);
        Event e2 = EventFactory.createRegularEvent("Smudjarenje Zimi", "Dzigovanje smudja u ranoj zimi", "Futog,Dunav", "15/12/2025 07:00", "Fishing", 0); // Prosao
        Event e3 = EventFactory.createRegularEvent("Skobaljijada Loznica", "Uzivanje na Drini", "Loznica,Drina, Zicina plaza", "10/06/2026 06:00", "Fishing", 0);

        // Na Pivo
        Event e4 = EventFactory.createRegularEvent("Rostilj i pivo u Velikoj reci", "Pecanje i odmor", "Vikendica na Drini", "01/05/2026 10:00", "NaPivo", 0); // Prosao
        Event e5 = EventFactory.createRegularEvent("Beer fest", "Testiranje piva", "Novi Sad,Master hala Novosadskog sajma", "15/05/2026 18:00", "NaPivo", 0);
        Event e6 = EventFactory.createRegularEvent("Degustacija Krafta", "Proba novih piva", "Pivoteka 77", "20/05/2026 20:00", "NaPivo", 0);
        Event e7 = EventFactory.createPromotedEvent("Oktoberfest NS", "Veliki festival piva", "Novosadski sajam, Master hala Novosadskog sajma", "10/10/2026 12:00", "NaPivo", 0, 5000);

        //Zurke
        Event e8 = EventFactory.createRegularEvent("Brucosijada", "Zurka", "Dva Galeba", "15/10/2030 22:00", "Party", 0);
        Event e9 = EventFactory.createRegularEvent("Apsolventsko vece", "Proslava kraja studija", "Hotel Zvezda", "10/06/2027 21:00", "Party", 0);
        Event e10 = EventFactory.createRegularEvent("Gustiranje Duskove rakije", "Uzivo Sejo kalac i Minela", "Djukin stan", "25/05/2026 20:30", "Party", 0);
        Event e11 = EventFactory.createRegularEvent("Zurka 80-te", "Jaka muzika", "Gerila", "20/07/2026 21:00", "Party", 0);

        // Festivali
        Event e12 = EventFactory.createPromotedEvent("EXIT Festival", "Najveći muzički festival", "Petrovaradinska tvrđava", "09/07/2026 20:00", "Festival", 0, 40000);
        Event e13 = EventFactory.createRegularEvent("Beer Fest 2025", "Beogradski festival piva", "Ušće", "15/08/2025 18:00", "Festival", 0); // Prosao
        Event e14 = EventFactory.createRegularEvent("Arsenal Fest", "Festival u Kragujevcu", "Knežev arsenal", "25/06/2026 19:00", "Festival", 0);
        Event e15 = EventFactory.createRegularEvent("LoveFest", "Festival elektronske muzike", "Vrnjačka Banja", "05/03/2026 20:00", "Festival", 0);// Prosao

        // Koncerti
        Event e16 = EventFactory.createRegularEvent("Posledni program tvog kompjutera", "Oprostajni kocnert DENIS&DENIS", "SKC Fabrika", "21/03/2026 21:00", "Concert", 0);//Prosao
        Event e17 = EventFactory.createPromotedEvent("Bajaga i Instruktori", "Mid", "Spens", "30/05/2026 21:00", "Concert", 0, 10000);
        Event e18 = EventFactory.createRegularEvent("Tap011", "Negde u daljini jedna reka protice", "Plato Tekstila", "05/05/2026 21:00", "Concert", 0);

        allEvents.add(e1);
        allEvents.add(e2);
        allEvents.add(e3);
        allEvents.add(e4);
        allEvents.add(e5);
        allEvents.add(e6);
        allEvents.add(e7);
        allEvents.add(e8);
        allEvents.add(e9);
        allEvents.add(e10);
        allEvents.add(e11);
        allEvents.add(e12);
        allEvents.add(e13);
        allEvents.add(e14);
        allEvents.add(e15);
        allEvents.add(e16);
        allEvents.add(e17);
        allEvents.add(e18);

        //5 dogadjaja u IntrestedEvents. Dodao sam i neke Event-ove koji su prosli ali nisam otisao.
        interestedEvents.add(e1);
        interestedEvents.add(e5);
        interestedEvents.add(e10);
        interestedEvents.add(e11);
        interestedEvents.add(e18);

        //8 dogadjaja u AtenndingEvents
        attendingEvents.add(e2);
        attendingEvents.add(e3);
        attendingEvents.add(e9);
        attendingEvents.add(e16);
        attendingEvents.add(e4);
        attendingEvents.add(e7);
        attendingEvents.add(e12);
        attendingEvents.add(e14);
    }


    // Sortira tako da promoted dogadjaji dolaze prvi
    public static List<Event> getSortedEvents() {
        List<Event> sortedList = new ArrayList<>(allEvents);

        Collections.sort(sortedList, new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                //
                return Boolean.compare(e2.isPromoted(), e1.isPromoted());
            }
        });
        return sortedList;
    }

    // Filtrira samo dogadjaje date kategorije i potom na sortira tako da promoted budu na vrhu
    public static List<Event> getSortedEventsByCategory(String category) {
        List<Event> filteredList = new ArrayList<>();

        //Sortiramo sa prethodno implementiranom metodom
        List<Event> allSorted = getSortedEvents();

        // Iz sortirane liste izvlacimo samo elemente zadate kategorije
        for (Event e : allSorted) {
            if (e.getCategory().equalsIgnoreCase(category)) {
                filteredList.add(e);
            }
        }
        //Vracamo tu listu kao povratnu vrednost
        return filteredList;
    }

    // Ako postoji Event sa imenom isti kao prosledjeni argument vracamo taj Event kao povratnu vrednost.
    public static Event findByName(String name) {
        //Ista logika kao iz proslog zadatka
        for (Event e : allEvents) {
            if (e.getName().equalsIgnoreCase(name)) {
                return e;
            }
        }
        return null;
    }
}