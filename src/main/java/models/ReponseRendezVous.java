package models;

import java.time.LocalDateTime;

public class ReponseRendezVous {

    private int           id;
    private String        message;
    private LocalDateTime dateReponse;
    private int           rendezVousId;
    private String        typeReponse;  // "accepte" | "refuse" | "reporte"
    private String        medecinNom;

    public ReponseRendezVous() {}

    public ReponseRendezVous(int id, String message, LocalDateTime dateReponse,
                             int rendezVousId, String typeReponse, String medecinNom) {
        this.id           = id;
        this.message      = message;
        this.dateReponse  = dateReponse;
        this.rendezVousId = rendezVousId;
        this.typeReponse  = typeReponse;
        this.medecinNom   = medecinNom;
    }

    public int  getId()         { return id; }
    public void setId(int id)   { this.id = id; }

    public String getMessage()              { return message; }
    public void   setMessage(String msg)    { this.message = msg; }

    public LocalDateTime getDateReponse()               { return dateReponse; }
    public void          setDateReponse(LocalDateTime d) { this.dateReponse = d; }

    public int  getRendezVousId()                   { return rendezVousId; }
    public void setRendezVousId(int rendezVousId)   { this.rendezVousId = rendezVousId; }

    public String getTypeReponse()                  { return typeReponse; }
    public void   setTypeReponse(String typeReponse) { this.typeReponse = typeReponse; }

    public String getMedecinNom()               { return medecinNom; }
    public void   setMedecinNom(String medNom)  { this.medecinNom = medNom; }

    @Override
    public String toString() {
        return "Réponse #" + id + " [" + typeReponse + "] pour RDV #" + rendezVousId;
    }
}