package com.app.sneezyapplication.data;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;

import org.bson.types.ObjectId;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RealmClass(name = "Sneeze")
public class SneezeItem extends RealmObject {

    @PrimaryKey
    private ObjectId _id;
    private String _partition = "partition";
    private String date;
    private String owner_id;

    // RealmList extends AbstractList, and due to polymorphism debauchery can be treated as a List.
    private RealmList<SneezeData> sneezes;

    // Standard getters & setters
    public ObjectId get_id() { return _id; }
    public void set_id(ObjectId _id) { this._id = _id; }
    public String get_partition() { return _partition; }
    public void set_partition(String _partition) { this._partition = _partition; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getOwner_id() { return owner_id; }
    public void setOwner_id(String owner_id) { this.owner_id = owner_id; }
    public RealmList<SneezeData> getSneezes() { return sneezes; }
    public void setSneezes(RealmList<SneezeData> sneezes) { this.sneezes = sneezes; }



    public SneezeItem(String date, String owner_id, RealmList<SneezeData> sneezes) {
        this._id = ObjectId.get();
        this.date = date;
        this.owner_id = owner_id;
        this.sneezes = sneezes;
    }

    public SneezeItem() {
        this._id = ObjectId.get();
    }


    // Database field constants
    public static final class Fields {
        public static final String ID = "_id";
        public static final String OWNER_ID = "owner_id";
        public static final String LOCATION = "location";
        public static final String DATE = "date";
        public static final String SNEEZES = "sneezes";
    }

    public Date dateAsAndroidDate(){
        try {
            return new SimpleDateFormat("EEE MMM dd yyyy").parse(this.date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
