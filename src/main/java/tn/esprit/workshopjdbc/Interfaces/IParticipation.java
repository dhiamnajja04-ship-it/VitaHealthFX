package tn.esprit.workshopjdbc.Interfaces;
import tn.esprit.workshopjdbc.Entities.Participation;
import tn.esprit.workshopjdbc.Entities.User;
import tn.esprit.workshopjdbc.Entities.Event;
import java.util.List;

public interface IParticipation {

    void add(Participation participation);


    void delete(int id);

    // 3. Used in myCalendar() - findBy(['user' => $user])
    List<Participation> findByUser(User user);

    // 4. Used in join() to prevent duplicates - findOneBy(['user' => $user, 'event' => $event])
    Participation findByUserAndEvent(User user, Event event);

    List<Participation> findByEvent(Event event);
}