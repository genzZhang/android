package com.genzzhang.demo.ringtone;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts.Entity;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import com.genzzhang.demo.app.DemoCache;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 注意插入，小米的手机，需要同时插入铃声的名称，则拿到uri，先找到源文件，拿到名称
 */
public class ContactRingtoneUtil {

	public static final String TAG = "ContactRingtoneUtil";
	private static ContactRingtoneUtil sInstance;

	public static int FLAG_DATABASE_ERROR = 1;
	public static int FLAG_SUCCESS = 2;
	public static int FLAG_FORMAT_ERROR = 3;
	private ContactRingtoneUtil() {
	}
	public synchronized static ContactRingtoneUtil getInstance() {
		if (sInstance == null) {
			sInstance = new ContactRingtoneUtil();
		}
		return sInstance;
	}

	public boolean insertContactRingtone(Uri pickedUri, String phoneNumber) {
		long contactId = ContactRingtoneUtil.getInstance().getContactId(phoneNumber);
		File ringtoneFile = UriPathConversionUtil.getFileFromUri(pickedUri, DemoCache.getContext());
		String ringtoneName = pickedUri.getLastPathSegment();
		Uri ringtoneUri = getRingtoneUri(Uri.fromFile(ringtoneFile), ringtoneName);
		Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);

		ArrayList<ContentProviderOperation> arrayList = new ArrayList<ContentProviderOperation>();
		arrayList.add(ContentProviderOperation.newUpdate(contactUri)
				.withValue(ContactsContract.Contacts.CUSTOM_RINGTONE, ringtoneUri.toString()).build());
		ContentResolver contentResolver = DemoCache.getContext().getContentResolver();
		try {
			contentResolver.applyBatch("com.android.contacts", arrayList);
			return true;
		} catch (Throwable e) {
			Log.i(ContactRingtoneUtil.TAG, android.util.Log.getStackTraceString(e));
		}

		return false;
	}

	public boolean insertContactRingtone(String number, String ringtonePath, String ringtoneName) {
		long contactId = ContactRingtoneUtil.getInstance().getContactId(number);
		Uri ringtoneUri = getRingtoneUri(Uri.fromFile(new File(ringtonePath)), ringtoneName);
		Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);

		ArrayList<ContentProviderOperation> arrayList = new ArrayList<ContentProviderOperation>();
		arrayList.add(ContentProviderOperation.newUpdate(contactUri)
				.withValue(ContactsContract.Contacts.CUSTOM_RINGTONE, ringtoneUri.toString()).build());
		ContentResolver contentResolver = DemoCache.getContext().getContentResolver();
		try {
			contentResolver.applyBatch("com.android.contacts", arrayList);
			return true;
		} catch (Throwable e) {
			Log.e(TAG, e.toString());
		}

		return false;
	}


	private Uri getRingtoneUri(Uri pathUri, String ringtoneName) {
		if (pathUri == null || !"file".equals(pathUri.getScheme()))	{
			return null;
		}
		File file = new File(pathUri.getPath());
		if (!file.exists() || !file.isFile()) {
			return null;
		}
		Uri uri = null;

		ContentResolver contentResolver = DemoCache.getContext().getContentResolver();
		String path = file.getPath();
		Uri contentUri = MediaStore.Audio.Media.getContentUriForPath(path);

		Cursor query = null;
		try {
			query = contentResolver.query(contentUri, null, "_data=?", new String[]{path}, null);
			if (query != null && query.moveToFirst()) {
				uri = ContentUris.withAppendedId(contentUri, query.getLong(query.getColumnIndex("_id")));
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		} finally {
			if (query != null) {
				try {
					query.close();
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}

		if (uri == null && !TextUtils.isEmpty(ringtoneName)) {
			ContentValues contentValues = new ContentValues();
			contentValues.put("_data", path);
			contentValues.put("title", ringtoneName);
			uri = contentResolver.insert(contentUri, contentValues);
		}
		return uri;
	}

	// 直接读取data表，根据号码返回rawcontactId，非phone索引表
	private int checkContactIdByNum(String number) {
		int rawContactid = -1;
		Cursor nCur = null;
		try {
			nCur = DemoCache.getContext().getContentResolver().query(Data.CONTENT_URI,
					new String[] { Phone.RAW_CONTACT_ID,
							Phone.DATA }, Phone.DATA
							+ " like '%" + number + "' AND " + Entity.MIMETYPE + " = '"
							+ Phone.CONTENT_ITEM_TYPE + "'", null, null);
			if (nCur != null && nCur.moveToFirst()) {
				rawContactid = nCur.getInt(0);
				String num = nCur.getString(1);
				Log.i(TAG, "rawContactid " + rawContactid + " num" + num + " " + nCur.getCount());
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		} finally {
			if (nCur != null) {
				try {
					nCur.close();
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}
		return rawContactid;
	}

	/**
	 *
	 checkContactUri(ContactsContract.RawContacts.CONTENT_URI);
	 checkContactUri(ContactsContract.Contacts.CONTENT_URI);
	 checkContactUri(ContactsContract.Data.CONTENT_URI);
	 checkContactUri(Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
	 number));
	 * @param uri
	 */
	public void checkContactUri(Uri uri) {
		Cursor nCur = null;
		StringBuilder stringBuilder = new StringBuilder(uri.toString());
		try {
			nCur = DemoCache.getContext().getContentResolver().query(uri, null, null, null, null);
			if (nCur != null) {
				String[] sbs = nCur.getColumnNames();
				if (sbs != null && sbs.length > 0) {
					for (int i = 0; i < sbs.length; i++) {
						stringBuilder.append("\n").append(sbs[i]);
					}
				}
			}
		} catch (Throwable e) {
			Log.e(TAG, e.getMessage());
		} finally {
			if (nCur != null) {
				try {
					nCur.close();
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}
	}


	/**
	 * 查找系统电话号码的名字，找不到则返回null
	 *
	 * @param number
	 * @return
	 */
	public String lookupNameOrNull(String number, AtomicInteger flag) {
		if (!PhoneUtil.isValid(number)) {
			return null;
		}

		String[] columns = { PhoneLookup.DISPLAY_NAME, PhoneLookup.NUMBER };
		Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
		Cursor cursor = null;

		try {
			Context context = DemoCache.getContext();
			cursor = context.getContentResolver().query(uri, columns, null, null, null);
			if (cursor == null) {
				if (flag != null) {
					flag.set(FLAG_DATABASE_ERROR);
				}
				return null;
			}
			// fix 部分机器异常，cursor column没有按照查询返回
			int displayIndex = cursor.getColumnIndex(PhoneLookup.DISPLAY_NAME);
			int numberIndex = cursor.getColumnIndex(PhoneLookup.NUMBER);
			if (displayIndex != -1 && numberIndex != -1) {
				while (cursor.moveToNext()) {
					String num = cursor.getString(numberIndex);
					String display = cursor.getString(displayIndex);
					String formatNum = PhoneUtil.getFormatNumber(num);
					if (!TextUtils.isEmpty(formatNum) && formatNum.equals(PhoneUtil.getFormatNumber(number))) {
						if (flag != null) {
							flag.set(FLAG_SUCCESS);
						}
						return display;
					} else {
						if (flag != null) {
							flag.set(FLAG_FORMAT_ERROR);
						}
					}
				}
			}
			return null;
		} catch (Throwable e) {
			Log.e(TAG, "lookupNameOrNull", e);
			if (flag != null) {
				flag.set(FLAG_DATABASE_ERROR);
			}
			return null;
		} finally {
			try {
				if (cursor != null) {
					cursor.close();
				}
			} catch (IllegalStateException e) {
				Log.e(TAG, "closing Cursor", e);
			}
		}
	}

	public int getContactId(String phone) {
		int id = -1;
		if (null != phone) {
			Uri uri = Phone.CONTENT_URI;
			Cursor nCur = null;
			try {
				nCur = DemoCache.getContext().getContentResolver().query(uri,
						new String[] { Phone.RAW_CONTACT_ID }, "PHONE_NUMBERS_EQUAL("
								+ Phone.NUMBER + " ," + phone + ")", null, null);
				if ((nCur != null) && nCur.moveToFirst()) {
					id = nCur.getInt(0);
				}

			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			} finally {
				try {
					if (nCur != null) {
						nCur.close();
					}
				} catch (IllegalStateException e) {
					Log.e(TAG, "closing Cursor", e);
				}
			}
		}

		List<ContactModel> modelList = ContactLoader.getInstance().getContacts();
		for (int i = 0; i < modelList.size(); i++) {
			ContactModel model = modelList.get(i);
			if (model.hasPhoneNum(phone)) {
				id = (int)model.getId();
				break;
			}
		}
		return id;
	}

	public Drawable getContactPhotoById(int rawcontactId) {
		Uri uri = Phone.CONTENT_URI;
		Cursor cursor = null;
		String[] COLUMNS_LOCAL = new String[2];
		COLUMNS_LOCAL[0] = "raw_contact_id";
		COLUMNS_LOCAL[1] = "data15";
		Uri photoUri = Uri.parse("content://com.android.contacts/data");
		String where = "raw_contact_id = " + rawcontactId + " AND mimetype ='vnd.android.cursor.item/photo'";
		Drawable photo = null;
		try {
			cursor =  DemoCache.getContext().getContentResolver().query(photoUri, COLUMNS_LOCAL, where, null, null);
			if (cursor != null && cursor.moveToFirst()) {
				byte[] bytes = cursor.getBlob(1);
				if (bytes != null) {
					int size = bytes.length;
					BitmapFactory.Options options = new BitmapFactory.Options();
					if (size > 102400) {
						// 大于100k,压缩为1/8
						options.inSampleSize = 8;
					} else if (size > 51200) {
						// 50k至100K,压缩为1/4
						options.inSampleSize = 4;
					} else if (size > 10240) {
						// 10k至50K,压缩为1/2
						options.inSampleSize = 2;
					} else {
						// 小于10K，不压缩
						options.inSampleSize = 1;
					}
					Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
					photo = new BitmapDrawable(bitmap);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		} finally {
			try {
				if (cursor != null) {
					cursor.close();
				}
			} catch (IllegalStateException e) {
				Log.e(TAG, "closing Cursor", e);
			}
		}
		return photo;
	}


	/**
	 * limit <=0 表示无限制
	 *
	 * @param limit
	 * @return
	 */
	public Set<String> getAllLocalNumbers(int limit) {
		Set<String> numSet = new HashSet<String>();
		String[] columns = { Phone.NUMBER };
		Cursor cursor = null;
		try {
			String limits = null;
			if (limit > 0) {
				limits = "_id desc LIMIT " + limit;
			}
			cursor =  DemoCache.getContext().getContentResolver().query(Phone.CONTENT_URI, columns, null, null, limits);
			if (cursor != null && cursor.moveToFirst()) {
				int number_index = cursor.getColumnIndex(Phone.NUMBER);
				try {
					while (!cursor.isAfterLast()) {
						String number = cursor.getString(number_index);
						if (!TextUtils.isEmpty(number)) {
							numSet.add(PhoneUtil.getFormatNumber(number));
						}
						cursor.moveToNext();
					}
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				try {
					cursor.close();
				} catch (Exception e) {
					Log.e(TAG, e.getMessage());
				}
			}
		}

		return numSet;
	}
}
