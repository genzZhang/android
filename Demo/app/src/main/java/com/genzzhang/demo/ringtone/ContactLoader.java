package com.genzzhang.demo.ringtone;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.ArrayMap;


import com.genzzhang.demo.app.DemoCache;

import java.util.ArrayList;
import java.util.List;

public class ContactLoader {

    private static ContactLoader instance = new ContactLoader();
    public static ContactLoader getInstance() {
        return instance;
    }

    public List<ContactModel> getContacts() {
        ContentResolver contentResolver = DemoCache.getContext().getContentResolver();

        String[] columns = new String[]{"_id", "display_name", "custom_ringtone", "has_phone_number", "sort_key"};
        String sortOrder = "sort_key COLLATE LOCALIZED asc";
        Cursor contactCursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, columns, null, null, sortOrder);
        ArrayMap<Long, ContactModel> arrayMap = new ArrayMap<Long, ContactModel>(contactCursor != null ? contactCursor.getCount() : 0);
        int index = 0;
        while (contactCursor != null && contactCursor.moveToNext()) {
            ContactModel model = new ContactModel();
            if (contactCursor.getInt(contactCursor.getColumnIndex("has_phone_number")) <= 0) {
                continue;
            }
            long _id = (long) contactCursor.getInt(contactCursor.getColumnIndex("_id"));
            String display_name = contactCursor.getString(contactCursor.getColumnIndex("display_name"));
            String custom_ringtone = contactCursor.getString(contactCursor.getColumnIndex("custom_ringtone"));
            String sort_key = contactCursor.getString(contactCursor.getColumnIndex("sort_key"));
            model.setId(_id);
            model.setSort_key(sort_key);
            model.setName(display_name);
            model.setRing_tone(custom_ringtone);

            model.setSort_weight(index);
            index++;
            arrayMap.put(_id, model);
        }
        if (contactCursor != null) {
            try {
                contactCursor.close();
            } catch (Throwable e) {

            }
        }

        columns = new String[]{"_id", "contact_id", "data1"};
        Cursor numberCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, columns, null, null, null);
        while (numberCursor != null && numberCursor.moveToNext()) {
            long _id = numberCursor.getLong(numberCursor.getColumnIndex("_id"));
            long contact_id = numberCursor.getLong(numberCursor.getColumnIndex("contact_id"));
            String phone_num = numberCursor.getString(numberCursor.getColumnIndex("data1"));
            if (_id > 0 && contact_id > 0) {
                if (!TextUtils.isEmpty(phone_num)) {
                    phone_num = PhoneUtil.getFormatNumber(phone_num);
                    arrayMap.get(contact_id).addNumber(new ContactModel.NumberModel(_id, contact_id, phone_num));
                }
            }
        }
        if (numberCursor != null) {
            try {
                numberCursor.close();
            } catch (Throwable e) {

            }
        }

        List<ContactModel> arrayList = new ArrayList<ContactModel>();
        for (Long longValue : arrayMap.keySet()) {
            arrayList.add(arrayMap.get(longValue));
        }

        return arrayList;
    }

}
