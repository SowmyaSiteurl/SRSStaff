package www.siteurl.in.srsstaff.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import www.siteurl.in.srsstaff.R;
import www.siteurl.in.srsstaff.adapters.ChatMessageAdapter;
import www.siteurl.in.srsstaff.api.Constants;
import www.siteurl.in.srsstaff.objects.chatMessage;

public class TicketDetails extends AppCompatActivity {

    private Toolbar mToolbar;
    SharedPreferences loginPref;
    String sessionId, uid, loginName;
    SharedPreferences.Editor editor;
    Dialog alertDialog;
    TextView ticketNme, ticketSub, ticketDes, ticketDte, ticketStts;
    Button submit;
    ListView listOfTickets;
    private MaterialEditText staffMessage;
    String adminReply;
    private ArrayList<chatMessage> chatList = new ArrayList<>();
    ChatMessageAdapter chatMessageAdapter;
    chatMessage chatMessage;
    String ticketIds, ticketName, ticketSubject, ticketDesc, ticketDate, ticketStatus, callBackDate, assignTo, assignedFrom;
    RelativeLayout ticketDetailsLayout;
    Spinner spinner;
    CheckBox internal;
    boolean ticketDetails = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_details);

        alertDialog = new Dialog(this);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //Toolbar
        mToolbar = findViewById(R.id.view_tickets_toolbar);
        mToolbar.setTitle("Ticket Details");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loginPref = getApplicationContext().getSharedPreferences("LoginPref", MODE_PRIVATE);
        sessionId = loginPref.getString("loginSid", null);
        uid = loginPref.getString("loginUserId", null);
        loginName = loginPref.getString("loginName", null);
        editor = loginPref.edit();

        ticketIds = getIntent().getExtras().getString("ticketId");
        ticketName = getIntent().getExtras().getString("ticketName");
        ticketSubject = getIntent().getExtras().getString("ticket_subject");
        ticketDesc = getIntent().getExtras().getString("ticketDesc");
        ticketDate = getIntent().getExtras().getString("ticketDate");
        ticketStatus = getIntent().getExtras().getString("ticket_status");

        //Initializing views
        listOfTickets = findViewById(R.id.staffTicketList);
        ticketNme = findViewById(R.id.staffTicketName);
        ticketSub = findViewById(R.id.staffTicketSub);
        ticketDes = findViewById(R.id.staffTicketDesc);
        ticketDte = findViewById(R.id.staffTicketDate);
        ticketStts = findViewById(R.id.staffTicketstatus);
        submit = findViewById(R.id.staffSubmit);
        staffMessage = findViewById(R.id.staffMessage);
        ticketDetailsLayout = findViewById(R.id.replyLayout);
        internal = (CheckBox) findViewById(R.id.internal);

        ticketNme.setText(ticketName);
        ticketSub.setText(ticketSubject);
        ticketDes.setText(ticketDesc);

        //date format to remove time and display only date.. the data is coming from server
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String inputPattern = "yyyy-MM-dd HH:mm:ss";
        String outputPattern = "dd-MMM-yyyy";
        SimpleDateFormat inputFormat = new SimpleDateFormat(inputPattern);
        SimpleDateFormat outputFormat = new SimpleDateFormat(outputPattern);
        Date datestart = null;
        String newclldatestart = null;
        try {
            datestart = inputFormat.parse(ticketDate);
            newclldatestart = outputFormat.format(datestart);
            ticketDte.setText(newclldatestart);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        ticketStts.setText("Status : " + ticketStatus);

        //Spinner functionality
        spinner = (Spinner) findViewById(R.id.spinner);
        List<String> categories = new ArrayList<String>();
        categories.add("Open");
        categories.add("Hold");
        categories.add("Close");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource
                (android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        getReplyFromServer("ticketlist");

        //onClickListener for Submit
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ticketDetails = true;

                adminReply = staffMessage.getText().toString().trim();
                if (TextUtils.isEmpty(adminReply)) {
                    Snackbar.make(ticketDetailsLayout, "Please Enter your Message", Snackbar.LENGTH_SHORT).show();
                    return;
                }
                sendUserMessageToServer();
            }
        });

    }

    //this is the method for Staff Reply
    private void sendUserMessageToServer() {

        final AlertDialog loadingDialog = new SpotsDialog(TicketDetails.this, R.style.Loading);
        loadingDialog.show();

        StringRequest ticketRequest = new StringRequest(Request.Method.POST,
                Constants.updateTicket, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                loadingDialog.dismiss();

                try {
                    JSONObject ticketObject = new JSONObject(response);
                    String error = ticketObject.getString("Error");
                    String message = ticketObject.getString("Message");
                    getReplyFromServer("ticketlistwithmsg");
                    //  showrDialog(error, message);
                    staffMessage.setText("");
                    if (!message.contains("empty"))
                        Snackbar.make(findViewById(R.id.details), message, Snackbar.LENGTH_LONG).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                loadingDialog.dismiss();
                if (error.networkResponse != null) {
                    parseVolleyError(error);
                }
                if (error instanceof ServerError) {
                    Toast.makeText(TicketDetails.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", String.valueOf(error instanceof ServerError));
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(TicketDetails.this, "Authentication Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Authentication Error");
                    error.printStackTrace();
                } else if (error instanceof ParseError) {
                    Toast.makeText(TicketDetails.this, "Parse Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Parse Error");
                    error.printStackTrace();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(TicketDetails.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "No Connection Error");
                    error.printStackTrace();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(TicketDetails.this, "Please check your connection.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Network Error");
                    error.printStackTrace();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(TicketDetails.this, "Timeout Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Timeout Error");
                    error.printStackTrace();
                } else {
                    Toast.makeText(TicketDetails.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new Hashtable<String, String>();
                String checkbox = "";
                if (internal.isChecked()) checkbox = "1";
                else checkbox = "0";
                params.put("user_id", uid);
                params.put("sid", sessionId);
                params.put("api_key", Constants.APIKEY);
                params.put("ticket_id", ticketIds);
                params.put("call_back_date", "");
                params.put("updated_note", staffMessage.getText().toString().trim());
                params.put("internal", checkbox);
                params.put("assigned_to", "");
                params.put("assigned_by", "");
                params.put("current_status", spinner.getSelectedItem().toString());
                return params;
            }
        };
        ticketRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SRSSingleton.getInstance(getApplicationContext()).addtorequestqueue(ticketRequest);
    }

    //Handling Volley Error
    public void parseVolleyError(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, "utf-8");
            JSONObject data = new JSONObject(responseBody);
            String message = data.getString("Message");
            Toast.makeText(TicketDetails.this, message, Toast.LENGTH_LONG).show();
            android.app.AlertDialog.Builder loginErrorBuilder = new android.app.AlertDialog.Builder(TicketDetails.this);
            loginErrorBuilder.setTitle("Error");
            loginErrorBuilder.setMessage(message);
            loginErrorBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            loginErrorBuilder.show();
        } catch (JSONException e) {
        } catch (UnsupportedEncodingException errorr) {
        }
    }

    //To show staff reply messages in server
    private void showrDialog(final String error, String message) {

        android.app.AlertDialog.Builder resultBuilder = new android.app.AlertDialog.Builder(TicketDetails.this);
        resultBuilder.setTitle("Srs Staff");
        resultBuilder.setMessage(message);
        resultBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (error.equals("false")) {
                    startActivity(new Intent(TicketDetails.this, MainActivity.class));
                    finish();
                    //  dialogInterface.dismiss();
                } else {
                    dialogInterface.dismiss();
                }
            }
        });
        resultBuilder.setCancelable(false);
        resultBuilder.show();

    }

    //this is the method for updatesOnTickets
    private void getReplyFromServer(final String listoftickets) {

        final AlertDialog loadingDialog = new SpotsDialog(TicketDetails.this, R.style.Loading);
        loadingDialog.show();

        StringRequest ticketRequest = new StringRequest(Request.Method.POST,
                Constants.updatesontickets, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                chatList.clear();

                loadingDialog.dismiss();

                try {
                    JSONObject ticketObject = new JSONObject(response);
                    String error = ticketObject.getString("Error");
                    String message = ticketObject.getString("Message");

                    if (error.equals("true")) {
                        if (!message.contains("empty"))
                            Snackbar.make(findViewById(R.id.replyLayout), message, Snackbar.LENGTH_LONG).show();
                    }

                    if (error.equals("false")) {

                        String ticketList = ticketObject.getString("Update on Ticket");

                        JSONArray ticketsArray = new JSONArray(ticketList);
                        for (int i = 0; i < ticketsArray.length(); i++) {

                            JSONObject listOfProducts = ticketsArray.getJSONObject(i);

                            String update_id = listOfProducts.getString("update_id");
                            String ticket_id = listOfProducts.getString("ticket_id");
                            String staff_id = listOfProducts.getString("staff_id");
                            String current_status = listOfProducts.getString("current_status");
                            String call_back_date = listOfProducts.getString("call_back_date");
                            String updated_note = listOfProducts.getString("updated_note");
                            String created_at = listOfProducts.getString("created_at");


                            JSONObject senderName = listOfProducts.getJSONObject("updated_by_user");
                            String name = senderName.getString("name");

                            chatMessage = new chatMessage(ticket_id, updated_note, created_at, name);
                            chatList.add(chatMessage);

                            if (listoftickets.equals("ticketlist")) {
                            } else {
                                if (ticketDetails) {
                                    Collections.reverse(chatList);
                                    ticketDetails = false;
                                }
                            }

                        }
                        Collections.reverse(chatList);
                        chatMessageAdapter = new ChatMessageAdapter(TicketDetails.this, R.layout.view_tickets, chatList);
                        listOfTickets.setAdapter(chatMessageAdapter);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                loadingDialog.dismiss();

                if (error.networkResponse != null) {
                    parseVolleyError(error);
                }
                if (error instanceof ServerError) {
                    Toast.makeText(TicketDetails.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", String.valueOf(error instanceof ServerError));
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(TicketDetails.this, "Authentication Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Authentication Error");
                    error.printStackTrace();
                } else if (error instanceof ParseError) {
                    Toast.makeText(TicketDetails.this, "Parse Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Parse Error");
                    error.printStackTrace();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(TicketDetails.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "No Connection Error");
                    error.printStackTrace();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(TicketDetails.this, "Please check your connection.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Network Error");
                    error.printStackTrace();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(TicketDetails.this, "Timeout Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Timeout Error");
                    error.printStackTrace();
                } else {
                    Toast.makeText(TicketDetails.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new Hashtable<String, String>();
                params.put("user_id", uid);
                params.put("sid", sessionId);
                params.put("api_key", Constants.APIKEY);
                params.put("ticket_id", ticketIds);

                return params;
            }
        };
        ticketRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        SRSSingleton.getInstance(TicketDetails.this).addtorequestqueue(ticketRequest);
    }
}
