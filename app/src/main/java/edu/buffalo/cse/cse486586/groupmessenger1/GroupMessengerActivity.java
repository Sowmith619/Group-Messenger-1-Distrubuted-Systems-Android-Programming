package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class GroupMessengerActivity extends Activity {

    //Assign the port numbers for all the avd's
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    //initialized count variable for icrementing the message count
    static int counter=0;
    //Acknowledgement message used to close the port correctly.
    static String ACK_MESSAGE = "ACKNOWLDGEMENT SENT";
    static String[] portnumbers={REMOTE_PORT0,REMOTE_PORT1,REMOTE_PORT2,REMOTE_PORT3,REMOTE_PORT4};
    static final int SERVER_PORT = 10000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        final TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        //We get message from the text box in avd's
        final EditText textedit = (EditText) findViewById(R.id.editText1);

        //To get the port numbers we implement TelephonyManager class
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        Log.e(TAG,portStr);

        //Here we create the server socket

        final  String myPort = String.valueOf((Integer.parseInt(portStr) * 2));


        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);

        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }



        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));




        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */

        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg=textedit.getText().toString();
                textedit.setText("");
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
            }
        });




    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;


    }


   //Client Task implementation

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
        try {
                String msgToSend = msgs[0];
                String senderPort=msgs[1];
                for(int i=0;i<portnumbers.length;i++){
                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),Integer.parseInt(portnumbers[i]));
                        PrintWriter MessageClient = new PrintWriter(socket.getOutputStream(), true);
                    MessageClient.println(msgToSend);
                        BufferedReader client_ack = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        while(!socket.isClosed()){
                            String server_msg = client_ack.readLine();
                            if(server_msg.equals(ACK_MESSAGE)){
                                MessageClient.close();
                                socket.close();
                            }
                        }
                  }


            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }

            return null;
        }
    }

    //Implementing server task
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        private Uri AUri = null;

        private Uri buildUri(String scheme, String authority)
        {
            Uri.Builder uriBuilder = new Uri.Builder();
            uriBuilder.authority(authority);
            uriBuilder.scheme(scheme);
            return uriBuilder.build();
        }

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            AUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger1.provider");
            Socket client = null;

            //we keep accepting messages from client so we use while(true)
            try {

                while (true) {
                    client = serverSocket.accept();
                    BufferedReader msg = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    PrintWriter server_ack = new PrintWriter(client.getOutputStream(), true);
                    String acktestmsg = null;
                    if ((acktestmsg = msg.readLine()) != null) {
                        Log.e(TAG,"Server: "+acktestmsg);
                        server_ack.println(ACK_MESSAGE);
                        ContentValues keyValueToInsert = new ContentValues();
                        keyValueToInsert.put("key", counter++);
                        keyValueToInsert.put("value", acktestmsg);
                        Uri newUri = getContentResolver().insert(AUri,keyValueToInsert);
                        publishProgress(acktestmsg);
                    }

                }


            } catch (Exception f) {
                f.printStackTrace();
            }
            finally {
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        protected void onProgressUpdate(String...strings) {


            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived+"\n");


            return;
        }
    }


        }


