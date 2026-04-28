package tn.esprit.workshopjdbc.Entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Event {
    // Fields
    private Integer id;
    private String title;
    private String description;
    private LocalDateTime date;
    private Float latitude;
    private Float longitude;
    private List<Participation> participations;
    private String url;
    // Constructor
    public Event() {
        this.participations = new ArrayList<>();
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public List<Participation> getParticipations() {
        return participations;
    }
    public String getUrl() {
        return url;
    }

    // 3. Add the Setter method (This is what fixes the red "setUrl" error)
    public void setUrl(String url) {
        this.url = url;
    }
    // Relationship logic (add/remove)
    public void addParticipation(Participation participation) {
        if (!this.participations.contains(participation)) {
            this.participations.add(participation);
            participation.setEvent(this);
        }
    }
    // Add this inside your Event class
    @Override
    public String toString() {
        return title + " (" + date.toLocalDate() + ")";
    }
    public void removeParticipation(Participation participation) {
        if (this.participations.remove(participation)) {
            if (participation.getEvent() == this) {
                participation.setEvent(null);
            }
        }
    }
}