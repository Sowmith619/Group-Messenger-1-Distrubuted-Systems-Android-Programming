package edu.buffalo.cse.cse486586.groupmessenger1;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class GroupMessengerProvider extends ContentProvider {
    static final String TAG = GroupMessengerProvider.class.getSimpleName();

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // You do not need to implement this.
        return null;
    }
    //Insert method inserts a new row into the provider and returns a new row into the provider and returns a content URI
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        String msg_key = values.getAsString("key");
        String msg_value = values.getAsString("value");
//We use the context provider class for writing into a file
        Context con = getContext();
    //Writing into the file we use context provider class for this, we use the mode private
        //we can use the mode append, but it appends onto the prevoius values it doesn't overwrite when we start the app again
        try
        {
            FileOutputStream out = con.openFileOutput(msg_key, Context.MODE_PRIVATE);
            out.write(msg_value.getBytes());
            out.close();
        }
        catch (IOException e)
        {
            Log.e(TAG, "insert method failed");
        }

        Log.v("insert", values.toString());
        return uri;
    }

    @Override
    public boolean onCreate() {
        // If you need to perform any one-time initialization task, please do it here.
        return false;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // You do not need to implement this.
        return 0;
    }
    //reference: https://stackoverflow.com/questions/36476484/android-matrixcursor
    //Using Matrix Cursor to query the messages
    @Override
    public Cursor query(Uri uri, String[] projection, String given_key, String[] selectionArgs,
                        String sortOrder) {

        //

        String write_fields[] = {"key", "value"};
        MatrixCursor mc = new MatrixCursor(write_fields);


        Context context_obj = getContext();

        try
        {
            //using FileInputStream class
            FileInputStream fis = context_obj.openFileInput(given_key);
            InputStreamReader inputStreamReader = new InputStreamReader(fis);
            //BufferedReader b = new BufferedReader(inputStreamReader);
            String retrieved_values =  new BufferedReader(inputStreamReader).readLine();
            Log.e(given_key, retrieved_values);
            Log.e(TAG,retrieved_values);
            //Moves the user input stream into selection arguments
            String arr_content[] = {given_key, retrieved_values};
            //Adds a new row to the end with the given column values, since we are ading to the result
            mc.addRow(arr_content);
            //file close
            fis.close();
            return mc;
        }
        catch (IOException e)
        {
            Log.e(TAG,e.getMessage());
            Log.e(TAG, "Querying failed");
        }

        Log.v("query", given_key);
        return null;
    }
}
