package www.siteurl.in.srsstaff.objects;

import java.io.Serializable;

/**
 * Created by siteurl on 11/4/18.
 */

public class AssignedTickets implements Serializable {

    private String ticketId;
    private String ticketSubject;
    private String ticketDesc;
    private String prodName;
    private String date;
    private String ticketStatus;

    public AssignedTickets(String ticketId, String ticketSubject, String ticketDesc, String prodName, String date, String ticketStatus) {
        this.ticketId = ticketId;
        this.ticketSubject = ticketSubject;
        this.ticketDesc = ticketDesc;
        this.prodName = prodName;
        this.date = date;
        this.ticketStatus = ticketStatus;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketSubject() {
        return ticketSubject;
    }

    public void setTicketSubject(String ticketSubject) {
        this.ticketSubject = ticketSubject;
    }

    public String getTicketDesc() {
        return ticketDesc;
    }

    public void setTicketDesc(String ticketDesc) {
        this.ticketDesc = ticketDesc;
    }

    public String getProdName() {
        return prodName;
    }

    public void setProdName(String prodName) {
        this.prodName = prodName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTicketStatus() {
        return ticketStatus;
    }

    public void setTicketStatus(String ticketStatus) {
        this.ticketStatus = ticketStatus;
    }
}
