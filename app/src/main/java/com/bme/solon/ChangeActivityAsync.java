package com.bme.solon;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import java.util.List;

/**
 * Async that changes screens to the MainActivity once all listed Asyncs are complete.
 */
public class ChangeActivityAsync extends AsyncTask<Void, Void, Void> {
    private List<AsyncTask> tasks;
    private Context context;

    /**
     * Constructor
     * @param context       The caller activity
     * @param tasks         List of tasks that must be completed
     */
    public ChangeActivityAsync(Context context, List<AsyncTask> tasks) {
        this.context = context;
        this.tasks = tasks;
    }

    /**
     * Check if all previous {@link AsyncTask}s are finished.
     * @param v     Nothing
     * @return      Nothing
     */
    @Override
    protected Void doInBackground(Void... v) {
        while(!isCancelled()) {
            if (tasks.stream().allMatch(t -> t.getStatus() == Status.FINISHED)){
                break;
            }
        }
        return null;
    }

    /**
     * Change screens to main activity
     * @param v     Nothing
     */
    @Override
    protected void onPostExecute (Void v) {
        context.startActivity(new Intent(context, MainActivity.class));
    }
}
