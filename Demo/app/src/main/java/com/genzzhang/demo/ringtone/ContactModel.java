package com.genzzhang.demo.ringtone;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class ContactModel {
    private String head_icon;
    private long id;//对于NumberModel.contacts_id
    private String name;
    private List<NumberModel> numbers = new ArrayList<NumberModel>();
    private String ring_tone;
    private String sort_key;
    private int sort_weight;


    public boolean hasPhoneNum(String phone_num) {
        for (int i = 0; i < numbers.size(); i++) {
            if (TextUtils.equals(phone_num, numbers.get(i).phone_num)) {
                return true;
            }
        }
        return false;
    }

    public static class NumberModel {
        private long contacts_id;
        private long id;
        private String phone_num;

        public NumberModel(long id, long contacts_id, String phone_num) {
            this.id = id;
            this.contacts_id = contacts_id;
            this.phone_num = phone_num;
        }

        public long getContacts_id() {
            return contacts_id;
        }

        public void setContacts_id(long contacts_id) {
            this.contacts_id = contacts_id;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getPhone_num() {
            return phone_num;
        }

        public void setPhone_num(String phone_num) {
            this.phone_num = phone_num;
        }

        @Override
        public String toString() {
            return "NumberModel{" +
                    "contacts_id=" + contacts_id +
                    ", id=" + id +
                    ", phone_num='" + phone_num + '\'' +
                    '}';
        }
    }

    public String getHead_icon() {
        return head_icon;
    }

    public void setHead_icon(String head_icon) {
        this.head_icon = head_icon;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<NumberModel> getNumbers() {
        return numbers;
    }

    public void addNumber(NumberModel number) {
        this.numbers.add(number);
    }

    public String getRing_tone() {
        return ring_tone;
    }

    public void setRing_tone(String ring_tone) {
        this.ring_tone = ring_tone;
    }

    public String getSort_key() {
        return sort_key;
    }

    public void setSort_key(String sort_key) {
        this.sort_key = sort_key;
    }

    public int getSort_weight() {
        return sort_weight;
    }

    public void setSort_weight(int sort_weight) {
        this.sort_weight = sort_weight;
    }

    @Override
    public String toString() {
        String outString =  "ContactModel{" +
                "head_icon='" + head_icon + '\'' +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", numbers=" + numbers +
                ", ring_tone='" + ring_tone + '\'' +
                ", sort_key='" + sort_key + '\'' +
                ", sort_weight=" + sort_weight +
                '}';
        for (int i = 0; i < numbers.size(); i++) {
            outString = outString + " | " + numbers.get(i).toString();
        }
        return outString;
    }
}
