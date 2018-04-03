package hu.ait.macweekly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ConnectivityIssueReceiver extends BroadcastReceiver {
    BaseActivity baseActivity;

    public ConnectivityIssueReceiver() {}

    public ConnectivityIssueReceiver(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getBooleanExtra("state", false)) {
            baseActivity.getConnectBar().show();
        } else {
            baseActivity.getConnectBar().dismiss();
        }
    }
}
