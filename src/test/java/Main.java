import tn.esprit.workshopjdbc.Entities.Event;
import tn.esprit.workshopjdbc.Services.EventService;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        EventService es = new EventService();

        // 1. Create a dummy event
        Event newEvent = new Event();
        newEvent.setTitle("Test Event ");
        newEvent.setDescription("Testing Java to MySQL connection");
        newEvent.setDate(LocalDateTime.now().plusDays(2)); // 2 days from now
        newEvent.setLatitude(36.8065f);
        newEvent.setLongitude(10.1815f);

        // 2. Try to add it
        es.add(newEvent);

        // 3. Try to list all event
        List<Event> list = es.findAll();
        System.out.println("Current Events in DB:");
        for (Event e : list) {
            System.out.println("- " + e.getTitle() + " (at " + e.getDate() + ")");
        }
    }
}