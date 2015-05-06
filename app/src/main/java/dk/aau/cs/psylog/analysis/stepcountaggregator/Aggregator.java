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
import java.util.HashMap;
import java.util.Map;

import dk.aau.cs.psylog.module_lib.DBAccessContract;
import dk.aau.cs.psylog.module_lib.IScheduledTask;


public class Aggregator implements IScheduledTask{
    Uri resulturi = Uri.parse(DBAccessContract.DBACCESS_CONTENTPROVIDER + "stepcountaggregator_result");
    ContentResolver contentResolver;
    public Aggregator(Context context)
    {
        contentResolver = context.getContentResolver();
    }
    // Skal emulere denne:
    // SELECT _id, date(`time`),SUM(`stepcount`) FROM dk_aau_cs_psylog_STEPCOUNTER_steps GROUP BY date(`time`)
    public void Aggregate()
    {
        HashMap<Date, Integer> groupSum = new HashMap<>();
        //Anskaf cursor
        Cursor data = getData();
        if(data == null)
            return;

        do {
            int stepcount = data.getInt(data.getColumnIndex("stepcount"));
            String time = data.getString(data.getColumnIndex("time"));
            Date date = convertTimeStringToDate(time);
            int existing = 0;
            if(groupSum.containsKey(date))
            {
                existing = groupSum.get(date);
            }
            groupSum.put(date, existing + stepcount);
            // reportRow(Date date, int stepCount)
        }while(data.moveToNext());
        data.close();

        // Jeg tror ikke om dette er i den rigtige orden, hvilket er et problem.
        for(Map.Entry<Date, Integer> entry : groupSum.entrySet())
        {
            reportRow(entry.getKey(), entry.getValue());
        }
    }

    private void reportRow(Date date, int stepCount)
    {
        ContentValues values = new ContentValues();
        SimpleDateFormat sdf  = new SimpleDateFormat("yyyy-MM-dd");
        Cursor resultsCursor = contentResolver.query(resulturi, new String[]{"_id", "date", "stepcount"}, null, null, "_id");
        Boolean update = false;
        if(resultsCursor.moveToFirst()) {
            do {
                int stepcount = resultsCursor.getInt(resultsCursor.getColumnIndex("stepcount"));
                String dateString = resultsCursor.getString(resultsCursor.getColumnIndex("date"));
                Date existingDate = convertTimeStringToDate(dateString);
                if(date.compareTo(existingDate) == 0)
                {
                    update = true;
                    break;
                }
            }while(resultsCursor.moveToNext());
        }
        String formattedDate = sdf.format(date);
        values.put("date", formattedDate);
        values.put("stepcount", stepCount);
        if(update) {
            Log.i("StepCountAggregator", "Updating " + stepCount + " for " + formattedDate);
            int result = contentResolver.update(resulturi, values,  "\"" + formattedDate + "\"" + "=" + "date", null);
            Log.i("StepCountAggregator", result + " rows updated.");
        } else {
            Log.i("StepCountAggregator", "Inserting " + stepCount + " for " + formattedDate);
            contentResolver.insert(resulturi, values);
        }
        resultsCursor.close();

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
