/*
 * Copyright (C) 2015 Willi Ye
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.grarak.kerneladiutor.fragments.other;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.widget.LinearLayout;

import com.grarak.kerneladiutor.MainActivity;
import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.elements.DAdapter;
import com.grarak.kerneladiutor.elements.DDivider;
import com.grarak.kerneladiutor.elements.cards.CardViewItem;
import com.grarak.kerneladiutor.elements.cards.PopupCardView;
import com.grarak.kerneladiutor.elements.cards.SwitchCardView;
import com.grarak.kerneladiutor.fragments.RecyclerViewFragment;
import com.grarak.kerneladiutor.services.BootService;
import com.grarak.kerneladiutor.services.ProfileTileReceiver;
import com.grarak.kerneladiutor.utils.Constants;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.database.ProfileDB;
import com.kerneladiutor.library.root.RootUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by willi on 09.03.15.
 */
public class SettingsFragment extends RecyclerViewFragment {

    ProgressDialog sysfsDialog = null;

    @Override
    public boolean showApplyOnBoot() {
        return false;
    }

    @Override
    public void init(Bundle savedInstanceState) {
        super.init(savedInstanceState);

        darkthemeInit();
        if (!Resources.getSystem().getConfiguration().locale.getLanguage().startsWith("en") && !Utils.isTV(getActivity()))
            forceenglishlanguageInit();
        if (Constants.VERSION_NAME.contains("beta")) betainfoInit();
        if (Utils.hasCMSDK()) profileTileInit();
        applyonbootInit();
        debuggingInit();
        securityInit();
        perappInit();
        showSectionsInit();
    }

    private void darkthemeInit() {
        SwitchCardView.DSwitchCard mDarkthemeCard = new SwitchCardView.DSwitchCard();
        mDarkthemeCard.setDescription(getString(R.string.dark_theme));
        mDarkthemeCard.setChecked(Utils.DARKTHEME);
        mDarkthemeCard.setOnDSwitchCardListener(new SwitchCardView.DSwitchCard.OnDSwitchCardListener() {
            @Override
            public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                Utils.saveBoolean("darktheme", checked, getActivity());
                startActivity(new Intent(getActivity(), MainActivity.class));
            }
        });

        addView(mDarkthemeCard);
    }

    private void forceenglishlanguageInit() {
        SwitchCardView.DSwitchCard mForceEnglishLanguageCard = new SwitchCardView.DSwitchCard();
        mForceEnglishLanguageCard.setDescription(getString(R.string.force_english_language));
        mForceEnglishLanguageCard.setChecked(Utils.getBoolean("forceenglish", false, getActivity()));
        mForceEnglishLanguageCard.setOnDSwitchCardListener(
                new SwitchCardView.DSwitchCard.OnDSwitchCardListener() {
                    @Override
                    public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                        Utils.saveBoolean("forceenglish", checked, getActivity());
                        if (!checked)
                            Utils.setLocale(Resources.getSystem().getConfiguration().locale.getLanguage(), getActivity());
                        startActivity(new Intent(getActivity(), MainActivity.class));
                    }
                });

        addView(mForceEnglishLanguageCard);
    }

    private void betainfoInit() {
        SwitchCardView.DSwitchCard mBetaInfoCard = new SwitchCardView.DSwitchCard();
        mBetaInfoCard.setTitle(getString(R.string.beta_info));
        mBetaInfoCard.setDescription(getString(R.string.beta_info_summary));
        mBetaInfoCard.setChecked(Utils.getBoolean("betainfo", true, getActivity()));
        mBetaInfoCard.setOnDSwitchCardListener(
                new SwitchCardView.DSwitchCard.OnDSwitchCardListener() {
                    @Override
                    public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                        Utils.saveBoolean("betainfo", checked, getActivity());
                    }
                });

        addView(mBetaInfoCard);
    }

    private void profileTileInit() {
        SwitchCardView.DSwitchCard mShowProfileTileCard = new SwitchCardView.DSwitchCard();
        mShowProfileTileCard.setDescription(getString(R.string.show_profile_tile));
        mShowProfileTileCard.setChecked(Utils.getBoolean("profiletile", true, getActivity()));
        mShowProfileTileCard.setOnDSwitchCardListener(new SwitchCardView.DSwitchCard.OnDSwitchCardListener() {
            @Override
            public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                Utils.saveBoolean("profiletile", checked, getActivity());
                ProfileTileReceiver.publishProfileTile(checked ? new ProfileDB(getActivity())
                        .getAllProfiles() : null, getActivity());
            }
        });

        addView(mShowProfileTileCard);
    }

    private void applyonbootInit() {
        DDivider mApplyonBootDividerCard = new DDivider();
        mApplyonBootDividerCard.setText(getString(R.string.apply_on_boot));

        addView(mApplyonBootDividerCard);

        if (!Utils.isTV(getActivity())) {
            SwitchCardView.DSwitchCard mHideApplyOnBootCard = new SwitchCardView.DSwitchCard();
            mHideApplyOnBootCard.setTitle(getString(R.string.hide_apply_on_boot));
            mHideApplyOnBootCard.setDescription(getString(R.string.hide_apply_on_boot_summary));
            mHideApplyOnBootCard.setChecked(Utils.getBoolean("hideapplyonboot", true, getActivity()));
            mHideApplyOnBootCard.setOnDSwitchCardListener(new SwitchCardView.DSwitchCard.OnDSwitchCardListener() {
                @Override
                public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                    Utils.saveBoolean("hideapplyonboot", checked, getActivity());
                }
            });

            addView(mHideApplyOnBootCard);
        }

        final List<String> list = new ArrayList<>();
        for (int i = 5; i < 421; i *= 2)
            list.add(i + getString(R.string.sec));

        PopupCardView.DPopupCard mApplyonbootDelayCard = new PopupCardView.DPopupCard(list);
        mApplyonbootDelayCard.setDescription(getString(R.string.delay));
        mApplyonbootDelayCard.setItem(Utils.getInt("applyonbootdelay", 5, getActivity()) + getString(R.string.sec));
        mApplyonbootDelayCard.setOnDPopupCardListener(new PopupCardView.DPopupCard.OnDPopupCardListener() {
            @Override
            public void onItemSelected(PopupCardView.DPopupCard dPopupCard, int position) {
                Utils.saveInt("applyonbootdelay", Utils.stringToInt(list.get(position)
                        .replace(getString(R.string.sec), "")), getActivity());
            }
        });

        addView(mApplyonbootDelayCard);

        SwitchCardView.DSwitchCard mApplyonbootNotificationCard = new SwitchCardView.DSwitchCard();
        mApplyonbootNotificationCard.setTitle(getString(R.string.notification));
        mApplyonbootNotificationCard.setDescription(getString(R.string.notification_summary));
        mApplyonbootNotificationCard.setChecked(Utils.getBoolean("applyonbootnotification", true, getActivity()));
        mApplyonbootNotificationCard.setOnDSwitchCardListener(
                new SwitchCardView.DSwitchCard.OnDSwitchCardListener() {
                    @Override
                    public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                        Utils.saveBoolean("applyonbootnotification", checked, getActivity());
                    }
                });

        addView(mApplyonbootNotificationCard);

        SwitchCardView.DSwitchCard mShowToastCard = new SwitchCardView.DSwitchCard();
        mShowToastCard.setDescription(getString(R.string.show_toast));
        mShowToastCard.setChecked(Utils.getBoolean("applyonbootshowtoast", true, getActivity()));
        mShowToastCard.setOnDSwitchCardListener(new SwitchCardView.DSwitchCard.OnDSwitchCardListener() {
            @Override
            public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                Utils.saveBoolean("applyonbootshowtoast", checked, getActivity());
            }
        });

        addView(mShowToastCard);

        CardViewItem.DCardView mTestCard = new CardViewItem.DCardView();
        mTestCard.setTitle(getString(R.string.test));
        mTestCard.setDescription(getString(R.string.test_summary));
        mTestCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                boolean applyonbootenabled = false;
                for (DAdapter.DView item : Constants.ITEMS)
                    if (item.getFragment() != null && Utils.getBoolean(item.getFragment().getClass().getSimpleName()
                            + "onboot", false, getActivity())) {
                        applyonbootenabled = true;
                        break;
                    }
                if (applyonbootenabled)
                    getActivity().startService(new Intent(getActivity(), BootService.class));
                else Utils.toast(getString(R.string.enable_apply_on_boot_first), getActivity());
            }
        });

        addView(mTestCard);
    }

    private void debuggingInit() {
        DDivider mDebuggingDividerCard = new DDivider();
        mDebuggingDividerCard.setText(getString(R.string.debugging));

        addView(mDebuggingDividerCard);

        CardViewItem.DCardView mLogcatCard = new CardViewItem.DCardView();
        mLogcatCard.setTitle(getString(R.string.logcat));
        mLogcatCard.setDescription(getString(R.string.logcat_summary));
        mLogcatCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                new Execute().execute("logcat -d > /sdcard/logcat.txt");
            }
        });

        addView(mLogcatCard);

        final StringBuilder lastKmsg = new StringBuilder();
        if (Utils.existFile("/proc/last_kmsg")) lastKmsg.append("/proc/last_kmsg");
        else if (Utils.existFile("/sys/fs/pstore/console-ramoops"))
            lastKmsg.append("/sys/fs/pstore/console-ramoops");
        if (lastKmsg.length() > 0) {
            CardViewItem.DCardView mLastKmsgCard = new CardViewItem.DCardView();
            mLastKmsgCard.setTitle(getString(R.string.last_kmsg));
            mLastKmsgCard.setDescription(getString(R.string.last_kmsg_summary));
            mLastKmsgCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
                @Override
                public void onClick(CardViewItem.DCardView dCardView) {
                    new Execute().execute("cat " + lastKmsg.toString() + " > /sdcard/last_kmsg.txt");
                }
            });

            addView(mLastKmsgCard);
        }

        CardViewItem.DCardView mDmesgCard = new CardViewItem.DCardView();
        mDmesgCard.setTitle(getString(R.string.driver_message));
        mDmesgCard.setDescription(getString(R.string.driver_message_summary));
        mDmesgCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                new Execute().execute("dmesg > /sdcard/dmesg.txt");
            }
        });

        addView(mDmesgCard);

        CardViewItem.DCardView mDumpSysFs = new CardViewItem.DCardView();
        mDumpSysFs.setTitle(getString(R.string.dump_sysfs));
        mDumpSysFs.setDescription(getString(R.string.dump_sysfs_summary));
        mDumpSysFs.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                sysfsDialog = ProgressDialog.show(MainActivity.context, getString(R.string.dump_sysfs_dialog_title),
                        getString(R.string.dump_sysfs_dialog_summary), true);
                Thread t = new Thread(new Runnable()
                {
                    public void run()
                    {
                        dumpsysfs();
                    }
                });
                t.start();
            }
        });

        addView(mDumpSysFs);

    }

    private void dumpsysfs() {
        String arrays[][] = {Constants.CPU_ARRAY, Constants.CPU_VOLTAGE_ARRAY, Constants.BATTERY_ARRAY, Constants.IO_ARRAY, Constants.VM_ARRAY};
        String twodarrays[][][] = {Constants.CPU_HOTPLUG_ARRAY, Constants.THERMAL_ARRAYS, Constants.SCREEN_ARRAY, Constants.WAKE_ARRAY, Constants.SOUND_ARRAY, Constants.WAKELOCK_ARRAY, Constants.MISC_ARRAY};
        // loop through each array in the constants file. These contain all the other arrays.
        // have to do this once for the 1d arrays and again for the 2 arrays
        try {
            File sysfsdump = new File(MainActivity.context.getFilesDir(), "sysfsdump.txt");
            if (sysfsdump.exists()) {
                sysfsdump.delete();
            }
            FileWriter output = new FileWriter(sysfsdump);
            for (int i = 0; i < arrays.length; i++) {
                for (int a = 0; a < arrays[i].length; a++) {
                    if (Utils.existFile(arrays[i][a]) && !arrays[i][a].contains("/system/bin")) {
                        output.write(sysfsrecord(arrays[i][a]));
                    }
                }
            }
            for (int i = 0; i < twodarrays.length; i++) {
                for (int a = 0; a < twodarrays[i].length; a++) {
                    for (int b = 0; b < twodarrays[i][a].length; b++) {
                        if (Utils.existFile(twodarrays[i][a][b]) && !twodarrays[i][a][b].contains("/system/bin")) {
                            output.write(sysfsrecord(twodarrays[i][a][b]));
                        }
                    }
                }
            }
            output.flush();
            output.close();

            sysfsDialog.dismiss();

            // Should send file to intent here to share it.
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("application/text");

            Uri uri = FileProvider.getUriForFile(MainActivity.context, "com.grarak.kerneladiutor.fileprovider", sysfsdump);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share file"));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String sysfsrecord (String file) {
        String ret = "";
        File sysfspath = new File(file);
        if (sysfspath.isDirectory()) {
            Log.i(Constants.TAG, "Dir: " + file);
            String path = file;
            ret = ret + "Dir: " + path + "\n";
            File dir = new File(path);
            File[] directoryListing = dir.listFiles();
            if (directoryListing != null) {
                for (File child : directoryListing) {
                    if (!child.isDirectory()) {
                        Log.i(Constants.TAG, "File: " + child + " | Value: " + Utils.readFile(child.toString()));
                        ret = ret + "File: " + child + " | Value: " + Utils.readFile(child.toString()) + "\n";
                    }
                }
            }
        } else {
            Log.i(Constants.TAG, "Path: " + file + " | Value: " + Utils.readFile(file));
            ret = ret + "Path: " + file + " | Value: " + Utils.readFile(file) + "\n";
        }
        return ret;
    }

    private class Execute extends AsyncTask<String, Void, Void> {
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.execute));
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            RootUtils.runCommand(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog.dismiss();
        }
    }

    private void securityInit() {
        DDivider mSecurityDividerCard = new DDivider();
        mSecurityDividerCard.setText(getString(R.string.security));

        addView(mSecurityDividerCard);

        CardViewItem.DCardView mSetPasswordCard = new CardViewItem.DCardView();
        mSetPasswordCard.setTitle(getString(R.string.set_password));
        mSetPasswordCard.setDescription(getString(R.string.set_password_summary));
        mSetPasswordCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                editPasswordDialog(Utils.getString("password", "", getActivity()));
            }
        });

        addView(mSetPasswordCard);

        CardViewItem.DCardView mDeletePasswordCard = new CardViewItem.DCardView();
        mDeletePasswordCard.setDescription(getString(R.string.delete_password));
        mDeletePasswordCard.setOnDCardListener(new CardViewItem.DCardView.OnDCardListener() {
            @Override
            public void onClick(CardViewItem.DCardView dCardView) {
                deletePasswordDialog(Utils.getString("password", "", getActivity()));
            }
        });

        addView(mDeletePasswordCard);
    }

    private void editPasswordDialog(final String oldPass) {
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(30, 20, 30, 20);

        final AppCompatEditText mOldPassword = new AppCompatEditText(getActivity());
        mOldPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mOldPassword.setHint(getString(R.string.old_password));
        if (!oldPass.isEmpty()) linearLayout.addView(mOldPassword);

        final AppCompatEditText mNewPassword = new AppCompatEditText(getActivity());
        mNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mNewPassword.setHint(getString(R.string.new_password));
        linearLayout.addView(mNewPassword);

        final AppCompatEditText mConfirmNewPassword = new AppCompatEditText(getActivity());
        mConfirmNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mConfirmNewPassword.setHint(getString(R.string.confirm_new_password));
        linearLayout.addView(mConfirmNewPassword);

        new AlertDialog.Builder(getActivity()).setView(linearLayout)
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!oldPass.isEmpty() && !mOldPassword.getText().toString().equals(Utils.decodeString(oldPass))) {
                            Utils.toast(getString(R.string.old_password_wrong), getActivity());
                            return;
                        }

                        if (mNewPassword.getText().toString().isEmpty()) {
                            Utils.toast(getString(R.string.password_empty), getActivity());
                            return;
                        }

                        if (!mNewPassword.getText().toString().equals(mConfirmNewPassword.getText().toString())) {
                            Utils.toast(getString(R.string.password_not_match), getActivity());
                            return;
                        }

                        if (mNewPassword.getText().toString().length() > 20) {
                            Utils.toast(getString(R.string.password_too_long), getActivity());
                            return;
                        }

                        Utils.saveString("password", Utils.encodeString(mNewPassword.getText().toString()), getActivity());
                    }
                }).show();
    }

    private void deletePasswordDialog(final String password) {
        if (password.isEmpty()) {
            Utils.toast(getString(R.string.set_password_first), getActivity());
            return;
        }

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(30, 20, 30, 20);

        final AppCompatEditText mPassword = new AppCompatEditText(getActivity());
        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        mPassword.setHint(getString(R.string.password));
        linearLayout.addView(mPassword);

        new AlertDialog.Builder(getActivity()).setView(linearLayout)
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (!mPassword.getText().toString().equals(Utils.decodeString(password))) {
                            Utils.toast(getString(R.string.password_wrong), getActivity());
                            return;
                        }

                        Utils.saveString("password", "", getActivity());
                    }
                }).show();
    }

    private void perappInit(){
        DDivider mPerAppDividerCard = new DDivider();
        mPerAppDividerCard.setText(getString(R.string.per_app));
        addView(mPerAppDividerCard);

        SwitchCardView.DSwitchCard mPerAppToastCard = new SwitchCardView.DSwitchCard();
        mPerAppToastCard.setTitle(getString(R.string.per_app_toast));
        mPerAppToastCard.setDescription(getString(R.string.per_app_toast_summary));
        mPerAppToastCard.setChecked(Utils.getBoolean("Per_App_Toast", false, getActivity()));
        mPerAppToastCard.setOnDSwitchCardListener(new SwitchCardView.DSwitchCard.OnDSwitchCardListener() {
            @Override
            public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                Utils.saveBoolean("Per_App_Toast", checked, getActivity());
            }
        });

        addView(mPerAppToastCard);

    }

    private void showSectionsInit() {
        DDivider mShowSectionsDividerCard = new DDivider();
        mShowSectionsDividerCard.setText(getString(R.string.show_sections));
        addView(mShowSectionsDividerCard);

        for (final DAdapter.DView section : Constants.ITEMS) {
            if (section.getFragment() != null
                    && !section.getFragment().getClass().getSimpleName().equals(getClass().getSimpleName())) {
                SwitchCardView.DSwitchCard mSectionCard = new SwitchCardView.DSwitchCard();
                mSectionCard.setDescription(section.getTitle());
                mSectionCard.setChecked(Utils.getBoolean(section.getFragment().getClass().getSimpleName()
                        + "visible", true, getActivity()));
                mSectionCard.setOnDSwitchCardListener(new SwitchCardView.DSwitchCard.OnDSwitchCardListener() {
                    @Override
                    public void onChecked(SwitchCardView.DSwitchCard dSwitchCard, boolean checked) {
                        Utils.saveBoolean(section.getFragment().getClass().getSimpleName()
                                + "visible", checked, getActivity());
                        ((MainActivity) getActivity()).setItems(SettingsFragment.this);
                    }
                });

                addView(mSectionCard);
            }
        }
    }

}
