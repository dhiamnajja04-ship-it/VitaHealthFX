package tn.esprit.workshopjdbc.Interfaces;
import tn.esprit.workshopjdbc.Entities.Event;
import java.util.List;

public interface IEvent {
    void add(Event event);
    void update(Event event);
    void delete(int id);
    Event findById(int id);
    List<Event> findAll();

    // 1. For the search bar (q)
    List<Event> searchByTitleOrDescription(String query);

    // 2. For the filters (past/upcoming)
    List<Event> findUpcoming();
    List<Event> findPast();
}