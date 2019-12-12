package com.github.bkway.contacts;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.LogUtils;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import java.util.Collections;
import java.util.HashSet;

@NativePlugin(
    permissions = { Manifest.permission.READ_CONTACTS },
    requestCodes = {
            CapacitorContacts.REQUEST_PICK_CODE,
            CapacitorContacts.REQUEST_FETCH_CODE,
            CapacitorContacts.REQUEST_PERMISSIONS_CODE
    }
)
public class CapacitorContacts extends Plugin {

    static final int REQUEST_PICK_CODE = 11222;
    static final int REQUEST_FETCH_CODE = 10012;
    static final int REQUEST_PERMISSIONS_CODE = 10312;

    protected static final HashSet<String> fieldDefaults = new HashSet(Collections.singletonList("displayName"));

    static private final String LOG_TAG = LogUtils.getPluginTag();

    protected void _query(PluginCall call, Uri contentUri, String[] projection) {

        HashSet<String> fields;

        try {
            fields = new HashSet(call.getArray("fields").<String>toList());
        } catch (Exception e) {
            fields = fieldDefaults;
        }

        JSArray contacts;

//        Log.i(LOG_TAG, fields.toString());
//        Log.i(LOG_TAG, JSArray.from(UserContact.projection(JSArray.from(fields.toArray()))).toString());

        try (Cursor res = getContext().getContentResolver().query(contentUri,
                projection,
                null,
                null,
                null)) {
//            {
                contacts = serialize(res, fields);
//            }
        } catch (Exception e) {
            call.error(e.getLocalizedMessage(), e);
            return;
        }
        JSObject out = new JSObject();
        out.put("contacts", contacts);
        call.success(out);
    }

    @PluginMethod()
    public void fetch(PluginCall call) {

        if (!hasRequiredPermissions()) {
            NativePlugin annotation = handle.getPluginAnnotation();
            pluginRequestPermissions(annotation.permissions(), REQUEST_FETCH_CODE);
            return;
        }

        String query = call.getString("query");

        Uri contentUri = Uri.withAppendedPath(
                Contacts.CONTENT_FILTER_URI,
                Uri.encode(query));

        _query(call, contentUri, null);

//        JSArray fields = call.getArray("fields", new JSArray());

//        String[] projection =
//                {
//                        Contacts._ID,
//                        Contacts.LOOKUP_KEY,
//                        Contacts.DISPLAY_NAME_PRIMARY,
//                };

//        JSArray contacts = new JSArray();
//
//        try (Cursor res = getContext().getContentResolver().query(contentUri,
//                null,
//                null,
//                null,
//                null)) {
//            {
//                while (res.moveToNext()) {
//                    contacts.put(serialize(res, fields));
//                }
//            }
//        } catch (Exception e) {
//            call.error(e.getLocalizedMessage(), e);
//            return;
//        }
//
//
//        JSObject out = new JSObject();
//        out.put("contacts", contacts);
//        call.success(out);
    }

    @PluginMethod()
    public void pick(PluginCall call) {
        getBridge().logToJs("Starting pick: " + REQUEST_PICK_CODE);

        if (!hasRequiredPermissions()) {
            saveCall(call);
            NativePlugin annotation = handle.getPluginAnnotation();
            pluginRequestPermissions(annotation.permissions(), REQUEST_PICK_CODE);
            return;
        }

        saveCall(call);
        Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
//        intent.setType(Contacts.);
        startActivityForResult(call, intent, REQUEST_PICK_CODE);
    }

    @PluginMethod()
    public void requestPermissions(PluginCall call) {
        if (!hasRequiredPermissions()) {
            saveCall(call);
            NativePlugin annotation = handle.getPluginAnnotation();
            pluginRequestPermissions(annotation.permissions(), REQUEST_PERMISSIONS_CODE);
            return;
        }
        JSObject out = new JSObject();
        out.put("allowed", true);
        call.success(out);
    }

    @PluginMethod()
    public void hasPermissions(PluginCall call) {
        JSObject out = new JSObject();
        out.put("allowed", hasRequiredPermissions());
        call.success(out);
    }

    @Override
    protected void handleRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        getBridge().logToJs("Should handle permissions: " + requestCode);
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults);

        PluginCall savedCall = getSavedCall();
        if (savedCall == null) {
            return;
        }

        for (int result: grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                savedCall.error("User denied permission");
                return;
            }
        }

        switch (requestCode) {
            case REQUEST_FETCH_CODE:
                fetch(savedCall);
                return;
            case REQUEST_PICK_CODE:
                pick(savedCall);
                return;
            case REQUEST_PERMISSIONS_CODE:
                JSObject allowed = new JSObject();
                allowed.put("allowed", true);
                savedCall.success(allowed);
                return;
        }
    }

    @Override
    protected void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        getBridge().logToJs("Should capture pick: " + requestCode);
        super.handleOnActivityResult(requestCode, resultCode, data);

        // Get the previously saved call
        PluginCall savedCall = getSavedCall();

        if (savedCall == null) {
            Log.e(LOG_TAG, "savedCall missing");
            return;
        }
        Log.i(LOG_TAG, Integer.toString(REQUEST_PICK_CODE));
        if (requestCode == REQUEST_PICK_CODE) { //  && resultCode == RESULT_OK
            Uri contactUri = data.getData();

            Log.i(LOG_TAG, "received URI: " + contactUri.toString());
            Log.i(LOG_TAG, "URI should be: " + ContactsContract.RawContacts.CONTENT_URI);
            Log.i(LOG_TAG, "instead of: " + ContactsContract.Contacts.lookupContact(getContext().getContentResolver(), contactUri));

            Uri newUri = ContactsContract.Contacts.lookupContact(getContext().getContentResolver(), contactUri);
            _query(savedCall, newUri, null);
//            HashSet<String> fields;
//            try {
//                fields = new HashSet(savedCall.getArray("fields").<String>toList());
//            } catch (Exception e) {
//                fields = fieldDefaults;
//            }
//
//            _query(savedCall, newUri, UserContact.projection(JSArray.from(fields.toArray())));
        }


//            Log.i(LOG_TAG, contactUri.toString());
//
//            try (Cursor res = getContext().getContentResolver().query(contactUri,
//                    null,
//                    null,
//                    null,
//                    null)) {
//                res.moveToFirst();
//                JSObject out = new JSObject();
//                out.put("contact", serialize(res));
//
//                savedCall.success(out);
//            } catch (Exception e) {
//                savedCall.error(e.getLocalizedMessage(), e);
//            }
    }

    protected JSArray serialize(Cursor c, HashSet<String> fields) {

        JSArray out = new JSArray();

        if (c.getCount() > 0) {
            c.moveToFirst();

            String id = UserContact.parseId(c);
            UserContact active = new UserContact(id, fields);
            active.absorb(c);

            while (c.moveToNext()) {
                id = UserContact.parseId(c);
                if (!id.equals(active.getString("id"))) {
                    out.put(active);
                    active = new UserContact(id, fields);
                }
                active.absorb(c);
            }
            out.put(active);
        }

        return out;
    }
}
