package dk.aau.cs.psylog.analysis.stepcountaggregator;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import dk.aau.cs.psylog.module_lib.DBAccessContract;
import dk.aau.cs.psylog.module_lib.IScheduledTask;


public class Aggregator implements IScheduledTask{
    Uri resulturi = Uri.parse(DBAccessContract.DBACCESS_CONTENTPROVIDER + "stepcountaggregator_result");
    ContentResolver contentResolver;
    public Aggregator(Context context)
    {
        contentResolver = context.getContentResolver();
    }

    Date date = null;
    int stepcount = 0;
    public void Aggregate()
    {
        //Anskaf cursor
        Cursor data = getData();
        if(data == null)
            return;

        do {
            int steps = data.getInt(data.getColumnIndex("steps"));
            String time = data.getString(data.getColumnIndex("time"));
            // reportRow(Date date, int stepCount)
        }while(data.moveToNext());
        data.close();
    }

    private void reportRow(Date date, int stepCount)
    {
        ContentValues values = new ContentValues();
        SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd");
        values.put("date", sdf.format(date));
        values.put("stepcount", stepCount);
        contentResolver.insert(resulturi, values);
    }
    private Cursor getData()
    {
        Uri uri = Uri.parse(DBAccessContract.DBACCESS_CONTENTPROVIDER + "stepcounter_steps");
        //_id stepcount time
        Cursor cursor = contentResolver.query(uri, new String[]{"_id", "stepcount", "time"}, null, null, "_id");

        if(cursor.moveToFirst())
        {
            return cursor;
        }
        return null;
    }

    private Date convertTimeStringToDate(String s){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date convertedTime = new Date();
        try {
            convertedTime = dateFormat.parse(s);
        }catch (ParseException e){
            e.printStackTrace();
        }
        return convertedTime;
    }

    @Override
    public void doTask() {
        Log.i("StepCountAggregator", "blev kaldt");
        Aggregate();
        Log.i("StepCountAggregator", "blev færdig");
    }

    @Override
    public void setParameters(Intent i) {

    }
}
