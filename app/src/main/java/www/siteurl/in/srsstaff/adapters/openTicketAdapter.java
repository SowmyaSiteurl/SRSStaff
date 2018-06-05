package www.siteurl.in.srsstaff.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import www.siteurl.in.srsstaff.R;
import www.siteurl.in.srsstaff.objects.openTickets;

/**
 * Created by siteurl on 21/4/18.
 */

public class openTicketAdapter extends ArrayAdapter<openTickets> {

private ArrayList<openTickets> ticketList;
        int positionOfList;
        Context mContext;


public openTicketAdapter(Context context, int textViewResourceId, ArrayList<openTickets> tickets) {
        super(context, textViewResourceId, tickets);

        this.ticketList = new ArrayList<openTickets>();
        this.ticketList.addAll(tickets);

        }

private class ViewHolder {
    TextView ticketName;
    TextView ticketSub;
    TextView ticketDesc;
    TextView ticketDate;
}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        final openTickets currentEnquiry = ticketList.get(position);
        if (convertView == null) {

            positionOfList = position;
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.open_tickets, parent, false);
            holder = new ViewHolder();
            holder.ticketName = (TextView) convertView.findViewById(R.id.ticketName);
            holder.ticketSub = (TextView) convertView.findViewById(R.id.ticketSubject);
            holder.ticketDesc = (TextView) convertView.findViewById(R.id.ticketDesc);
            holder.ticketDate = (TextView) convertView.findViewById(R.id.ticketDate);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String ticketName = currentEnquiry.getProdName();
        holder.ticketName.setText(ticketName);
        holder.ticketSub.setText(currentEnquiry.getTicket_subject());
        holder.ticketDesc.setText(currentEnquiry.getTicketDesc());
       // holder.ticketDate.setText(currentEnquiry.getDate().substring(0, 10));

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        String outputPattern = "dd-MMM-yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
        Date datestart = null;
        String newclldatestart = null;
        try {
            datestart = inputFormat.parse(currentEnquiry.getDate());
            newclldatestart = outputFormat.format(datestart);
            holder.ticketDate.setText(newclldatestart);

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return convertView;
    }

}

