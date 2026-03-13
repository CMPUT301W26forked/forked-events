package com.example.lottery.Entrant.Model;

/**
 * entrant invitation model
 */
public class EntrantInvitation {
    private String invitationId;
    private String eventId;
    private String entrantId;
    private String status;


    public EntrantInvitation() {
        // Needed for Firestore
    }

    /**
     * full constructor for entrant invitation
     * @param invitationId unique invitation id
     * @param eventId unique event id
     * @param entrantId unique entrant id
     * @param status current status
     */
    public EntrantInvitation(String invitationId, String eventId, String entrantId, String status) {
        this.invitationId = invitationId;
        this.eventId = eventId;
        this.entrantId = entrantId;
        this.status = status;
    }

    /**
     * gets invitation id
     * @return unique invitation identifier
     */
    public String getInvitationId() {
        return invitationId;
    }

    /**
     * gets event id
     * @return unique event identifier
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * gets entrant id
     * @return unique entrant identifier
     */
    public String getEntrantId() {
        return entrantId;
    }

    /**
     * gets current status
     * @return status string
     */
    public String getStatus() {
        return status;
    }

    /**
     * sets invitation id
     * @param invitationId unique invitation identifier
     */
    public void setInvitationId(String invitationId) {
        this.invitationId = invitationId;
    }

    /**
     * sets event id
     * @param eventId unique event identifier
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * sets entrant id
     * @param entrantId unique entrant identifier
     */
    public void setEntrantId(String entrantId) {
        this.entrantId = entrantId;
    }

    /**
     * sets status
     * @param status current status string
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
