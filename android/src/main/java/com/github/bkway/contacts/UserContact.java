package com.github.bkway.contacts;

import android.database.Cursor;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import java.util.HashSet;

class UserContact extends JSObject {

//    protected HashSet<String> legitFields = new HashSet<>(Arrays.asList(
//            "displayName",
//            "familyName",
//            "givenName",
//            "middleName",
//            "prefix",
//            "suffix",
//            "nickname",
//            "phoneNumbers",
//            "emails",
//            "addresses",
//            "organizations",
//            "ims",
//            "urls",
//            "events",
//    ));

    protected HashSet<String> fields;

    // TODO implement "icons"
//    public final String[] properties = { "names", "emails", "numbers", "addresses", "icons" };

    public static String parseId(Cursor c) {
        Log.i("UserContact",Contacts._ID + " = " + c.getColumnIndex(Contacts._ID));
        Log.i("UserContact",JSArray.from(c.getColumnNames()).toString());
        return c.getString(c.getColumnIndex(Contacts._ID));
    }

    public static String[] projection(JSArray fields) {
        HashSet<String> out = new HashSet<String>();
        out.add(Data._ID);
        out.add(Data.CONTACT_ID);
        out.add(Data.LOOKUP_KEY);

        for (int i = 0; i < fields.length(); i++) {
            String field = fields.optString(i);
            switch (field) {
                case "displayName":
                    out.add(Data.DISPLAY_NAME_PRIMARY);
                    break;
                case "familyName":
                    out.add(StructuredName.FAMILY_NAME);
                    break;
                case "givenName":
                    out.add(StructuredName.GIVEN_NAME);
                    break;
                case "middleName":
                    out.add(StructuredName.MIDDLE_NAME);
                    break;
                case "prefix":
                    out.add(StructuredName.PREFIX);
                    break;
                case "suffix":
                    out.add(StructuredName.SUFFIX);
                    break;
                case "nickname":
                    out.add(Nickname.NAME);
                    break;
                case "phoneNumbers":
                    out.add(Phone.NUMBER);
                    out.add(Phone.TYPE);
                    out.add(Phone.LABEL);
                    break;
                case "emails":
                    out.add(Email.DATA);
                    out.add(Email.TYPE);
                    out.add(Email.LABEL);
                    break;
                case "addresses":
                    out.add(StructuredPostal.FORMATTED_ADDRESS);
                    out.add(StructuredPostal.STREET);
                    out.add(StructuredPostal.CITY);
                    out.add(StructuredPostal.REGION);
                    out.add(StructuredPostal.POSTCODE);
                    out.add(StructuredPostal.COUNTRY);
                    out.add(StructuredPostal.LABEL);
                    break;
                case "organizations":
                    out.add(Organization.TYPE);
                    out.add(Organization.DEPARTMENT);
                    out.add(Organization.COMPANY);
                    out.add(Organization.TITLE);
                    out.add(Organization.LABEL);
                    break;
                case "ims":
                case "events":
                    out.add(Im.DATA);
                    out.add(Im.TYPE);
                    break;
                case "urls":
                    out.add(Website.URL);
                    out.add(Website.TYPE);
                    out.add(Website.LABEL);
                    break;
                default:
                    Log.i("UserContact", "Skipping field: " + field);
                    // TODO report an error back somehow?
                    // skip unknown field types
            }
        }
        return out.toArray(new String[] {});
    }


    public UserContact(String id, HashSet<String> fields) {
        super();
        put("id", id);
        this.fields = fields;
        Log.i("UserContact", "New UserContact: " + id);
    }

    public void absorb(Cursor c) {
//        String mimetype = c.getString(c.getColumnIndex(Data.MIMETYPE));
//        Log.i("UserContact", "Absorbing: " + mimetype);

//        switch (mimetype) {
//            case StructuredName.CONTENT_ITEM_TYPE:
                Log.i("UserContact", "StructuredName");
                putOpt("displayName", c, StructuredName.DISPLAY_NAME_PRIMARY);
                putOpt("familyName", c, StructuredName.FAMILY_NAME);
                putOpt("givenName", c, StructuredName.GIVEN_NAME);
                putOpt("middleName", c, StructuredName.MIDDLE_NAME);
                putOpt("namePrefix", c, StructuredName.PREFIX);
                putOpt("nameSuffix", c, StructuredName.SUFFIX);
//                break;
//            case Nickname.CONTENT_ITEM_TYPE:
                Log.i("UserContact", "Nickname");
                putOpt("nickname", c, Nickname.NAME);
//                break;
//            case Phone.CONTENT_ITEM_TYPE:
                Log.i("UserContact", "Phone");
                String phoneType = c.getString(c.getColumnIndex(Phone.LABEL));
                append("phoneNumbers", c, phoneType, Phone.NUMBER);
//                break;
//            case Email.CONTENT_ITEM_TYPE:
                Log.i("UserContact", "Email");
                String emailType = c.getString(c.getColumnIndex(Email.LABEL));
                append("emails", c, emailType, Email.ADDRESS);
//                break;
//            case Im.CONTENT_ITEM_TYPE:
                Log.i("UserContact", "IM");
                String imType = c.getString(c.getColumnIndex(Im.PROTOCOL));
                append("ims", c, imType, Im.DATA);
//                break;
//            case Website.CONTENT_ITEM_TYPE:
                Log.i("UserContact", "URL");
                String urlType = c.getString(c.getColumnIndex(Website.TYPE));
                append("websites", c, urlType, Website.URL);
//                break;
//            case Event.CONTENT_ITEM_TYPE:
                Log.i("UserContact", "Event");
                String eventType = c.getString(c.getColumnIndex(Event.LABEL));
                append("events", c, eventType, Event.DATA);
//                break;
//            case StructuredPostal.CONTENT_ITEM_TYPE:
                Log.i("UserContact", "StructuredPostal");
                JSObject addy = new JSObject();
                addy.put("type", c.getString(c.getColumnIndex(StructuredPostal.LABEL)));
                addy.put("formatted", c.getString(c.getColumnIndex(StructuredPostal.FORMATTED_ADDRESS)));
                addy.put("street", c.getString(c.getColumnIndex(StructuredPostal.STREET)));
                addy.put("city", c.getString(c.getColumnIndex(StructuredPostal.CITY)));
                addy.put("region", c.getString(c.getColumnIndex(StructuredPostal.REGION)));
                addy.put("postalCode", c.getString(c.getColumnIndex(StructuredPostal.POSTCODE)));
                addy.put("country", c.getString(c.getColumnIndex(StructuredPostal.COUNTRY)));
                append("addresses", addy);
//                break;
//            case Organization.CONTENT_ITEM_TYPE:
                Log.i("UserContact", "Organization");
                JSObject org = new JSObject();
                org.put("type", c.getString(c.getColumnIndex(Organization.LABEL)));
                org.put("name", c.getString(c.getColumnIndex(Organization.COMPANY)));
                org.put("department", c.getString(c.getColumnIndex(Organization.DEPARTMENT)));
                org.put("title", c.getString(c.getColumnIndex(Organization.TITLE)));
                append("organizations", org);
//                break;
//        }
    }
    protected void putOpt(String field, Cursor cursor, String index) {
        if (!fields.contains(field)) {
            return;
        }
        int col = cursor.getColumnIndex(index);
        try {
            put(field, cursor.getString(cursor.getColumnIndexOrThrow(index)));
        } catch (Exception e) {
            Log.i("UserContact", field + e.getLocalizedMessage());
        }
    }

    protected void append(String field, Object value) {
        if (!fields.contains(field)) {
            return;
        }
        if (!has(field)) {
            put(field, new JSArray());
        }

        try {
            accumulate(field, value);
        } catch (Exception e) {
            // ignore?
        }
    }

    protected void append(String field, Cursor cursor, String type, String index) {
        JSObject out = new JSObject();
        out.put("type", type);
        out.put("value", cursor.getString(cursor.getColumnIndex(index)));
        append(field, out);
    }

    protected void append(String field, Cursor cursor, String index) {
        append(field, cursor.getString(cursor.getColumnIndex(index)));
    }


}
