package com.app.sneezyapplication.data;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

import org.bson.types.ObjectId;

public class SneezeItem extends RealmObject {

    @PrimaryKey
    private ObjectId _id;
    private String date;
    private String owner_id;
    private RealmList<SneezeData> sneezes;

    // Standard getters & setters
    public ObjectId get_id() { return _id; }
    public void set_id(ObjectId _id) { this._id = _id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getOwner_id() { return owner_id; }
    public void setOwner_id(String owner_id) { this.owner_id = owner_id; }
    public RealmList<SneezeData> getSneezes() { return sneezes; }
    public void setSneezes(RealmList<SneezeData> sneezes) { this.sneezes = sneezes; }

    public static final class Fields {
        public static final String ID = "_id";
        public static final String OWNER_ID = "owner_id";
        public static final String LOCATION = "location";
        public static final String DATE = "date";
        public static final String SNEEZES = "sneezes";
    }
}
