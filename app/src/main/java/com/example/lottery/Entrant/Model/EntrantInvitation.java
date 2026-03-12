package com.example.lottery.Entrant.Model;

public class EntrantInvitation {
    private String invitationId;
    private String eventId;
    private String entrantId;
    private String status;

    public EntrantInvitation() {
        // Needed for Firestore
    }

    public EntrantInvitation(String invitationId, String eventId, String entrantId, String status) {
        this.invitationId = invitationId;
        this.eventId = eventId;
        this.entrantId = entrantId;
        this.status = status;
    }

    public String getInvitationId() {
        return invitationId;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEntrantId() {
        return entrantId;
    }

    public String getStatus() {
        return status;
    }

    public void setInvitationId(String invitationId) {
        this.invitationId = invitationId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setEntrantId(String entrantId) {
        this.entrantId = entrantId;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}