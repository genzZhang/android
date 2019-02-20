package com.genzzhang.demo.ringtone;

import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.genzzhang.demo.app.AbsActivity;
import com.genzzhang.demo.util.C;

public class RingtoneActivity extends AbsActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout layout = new FrameLayout(mContext);
        layout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(layout);
        Button btn = new Button(mContext);
        btn.setText("点击选择铃声");
        layout.addView(btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                // Allow user to pick 'Default'
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
                // Show only ringtones
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE);
                // Allow the user to pick a silent ringtone
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
                //final Uri ringtoneUri = EditorUiUtils.getRingtoneUriFromString(mCustomRingtone,CURRENT_API_VERSION);
                // Put checkmark next to the current ringtone for this contact
                //intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, ringtoneUri);
                RingtoneActivity.this.startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Uri pickedUri = data.getParcelableExtra(
                    RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (pickedUri != null) {
                try {
                    ContactRingtoneUtil.getInstance().insertContactRingtone(pickedUri, "15866668888");
                } catch (Throwable throwable) {
                    Log.e(C.TAG, throwable.toString());
                    mToast.setText("读和写通讯录权限");
                    mToast.show();
                }
            }
        }
    }
}
