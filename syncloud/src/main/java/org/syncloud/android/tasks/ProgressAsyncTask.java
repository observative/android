package org.syncloud.android.tasks;

import android.os.AsyncTask;

import org.syncloud.android.Progress;

public class ProgressAsyncTask<TParams, TResult> extends AsyncTask<TParams, Void, AsyncResult<TResult>> {

    public interface Success<TResult> {
        void run(TResult result);
    }

    public interface Completed<TResult> {
        void run(AsyncResult<TResult> result);
    }

    public interface Work<TParams, TResult> {
        TResult run(TParams... args);
    }

    private String title;
    private Progress progress;
    private Work<TParams, TResult> work;
    private String errorMessage;
    private Success<TResult> success;
    private Completed<TResult> completed;

    public ProgressAsyncTask(Progress progress, String title, Work<TParams, TResult> work) {
        this.progress = progress;
        this.title = title;
        this.work = work;
    }

    public ProgressAsyncTask() {
        this(null, null, null);
    }

    @Override
    protected AsyncResult<TResult> doInBackground(TParams... args) {
        if (work != null) {
            try {
                return AsyncResult.value(work.run(args));
            } catch (Throwable th) {
                return AsyncResult.exception(th);
            }
        } else {
            return null;
        }
    }

    public ProgressAsyncTask<TParams, TResult> setErrorMessage(String message) {
        this.errorMessage = message;
        return this;
    }

    public ProgressAsyncTask<TParams, TResult> setTitle(String title) {
        this.title = title;
        return this;
    }

    public ProgressAsyncTask<TParams, TResult> setProgress(Progress progress) {
        this.progress = progress;
        return this;
    }

    public ProgressAsyncTask<TParams, TResult> onSuccess(Success<TResult> success) {
        this.success = success;
        return this;
    }

    public ProgressAsyncTask<TParams, TResult> onCompleted(Completed<TResult> completed) {
        this.completed = completed;
        return this;
    }

    public ProgressAsyncTask<TParams, TResult> doWork(Work<TParams, TResult> work) {
        this.work = work;
        return this;
    }

    @Override
    protected void onPreExecute() {
        if (progress != null) {
            progress.start();
            if (title != null)
                progress.title(title);
        }
    }

    @Override
    protected void onPostExecute(AsyncResult<TResult> result) {
        if (progress != null) {
            if (result != null && !result.hasValue() && errorMessage != null)
                progress.error(errorMessage);
            else
                progress.stop();
        }
        if (result == null)
            success.run(null);
        else
            if (result.hasValue() && success != null)
                success.run(result.getValue());

        if (completed != null)
            completed.run(result);
    }

    @Override
    protected void onCancelled() {
        if (progress != null) {
            progress.stop();
        }
    }

    public static void execute(final Runnable runnable) {
        new ProgressAsyncTask<Void, Void>()
                .doWork(new Work<Void, Void>() {
                    @Override
                    public Void run(Void... args) {
                        runnable.run();
                        return null;
                    }
                })
                .execute();

    }
}
