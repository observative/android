package org.syncloud.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.apache.log4j.Logger;
import org.syncloud.android.core.redirect.IUserService;
import org.syncloud.android.core.redirect.RedirectService;
import org.syncloud.android.core.redirect.UserCachedService;
import org.syncloud.android.core.redirect.UserStorage;

import java.io.File;

import static org.acra.ReportField.*;
import static org.syncloud.android.core.redirect.RedirectService.getApiUrl;

@ReportsCrashes(
        customReportContent = { APP_VERSION_CODE, ANDROID_VERSION, PHONE_MODEL, STACK_TRACE, LOGCAT },

        mode = ReportingInteractionMode.DIALOG,
        resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
        resDialogText = R.string.crash_dialog_text,
        resDialogIcon = R.drawable.ic_launcher, //optional. default is a warning sign
        resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
        resDialogOkToast = R.string.crash_dialog_ok_toast, // optional. displays a Toast message when the user accepts to send a report.

        logcatArguments = { "-t", "500", "-v", "long", "*:D"},
        logcatFilterByPid = false,

        reportSenderFactoryClasses = { AcraLogEmailerFactory.class }
)
public class SyncloudApplication extends Application {

    private String TAG = SyncloudApplication.class.getSimpleName();

    public static String DEVICE_ENDPOINT = "device_endpoint";

    private Preferences preferences;
    private UserStorage userStorage;

    @Override
    public void onCreate() {

        ACRA.init(this);

        ConfigureLog4J.configure();

        Logger logger = Logger.getLogger(SyncloudApplication.class);
        logger.info("Starting Syncloud App");


        super.onCreate();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        preferences = new Preferences(sharedPreferences);
        userStorage = new UserStorage(new File(getApplicationContext().getFilesDir(), "user.json"));
    }

    public IUserService userServiceCached() {
        RedirectService redirectService = new RedirectService(getApiUrl(preferences.getMainDomain()));
        UserCachedService userService = new UserCachedService(redirectService, userStorage);
        return userService;
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public void reportError() {
        ACRA.getErrorReporter().handleSilentException(null);
    }

    public boolean isWifiConnected() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }
}
