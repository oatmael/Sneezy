package com.example.sneezyapplication.data;

import org.bson.BsonArray;
import org.bson.BsonDateTime;
import org.bson.BsonDocument;
import org.bson.BsonObjectId;
import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SneezeItem {

    public static final String SNEEZE_DATABASE = "LogTable";
    public static final String SNEEZE_COLLECTION = "Sneezes";

    private final ObjectId _id;
    private final String owner_id;
    private final String date;
    private final List<SneezeData> sneezes;

    public SneezeItem(ObjectId _id, String owner_id, String date, List<SneezeData> sneezes) {
        this._id = _id;
        this.owner_id = owner_id;
        this.date = date;
        this.sneezes = sneezes;
    }

    public ObjectId get_id() {
        return _id;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public String getDate() { return date; }

    public List<SneezeData> getSneezes() { return sneezes; }

    static BsonDocument toBsonDocument(final SneezeItem item) {
        final BsonDocument asDoc = new BsonDocument();
        ArrayList<BsonDocument> sneezeDataArray = new ArrayList<>();
        for (SneezeData s : item.sneezes){
            BsonDocument sd = new BsonDocument();
            sd.put(Fields.LOCATION, new BsonString(s.getLocation()));
            sd.put(Fields.DATE, new BsonString(s.getDate().toString()));
            sneezeDataArray.add(sd);
        }
        asDoc.put(Fields.ID, new BsonObjectId(item.get_id()));
        asDoc.put(Fields.OWNER_ID, new BsonString(item.getOwner_id()));
        asDoc.put(Fields.DATE, new BsonString(item.getDate()));
        asDoc.put(Fields.SNEEZES, new BsonArray(sneezeDataArray));
        return asDoc;
    }

    static SneezeItem fromBsonDocument(final BsonDocument doc) {
        DateFormat dayFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        String dayDate = doc.getString(Fields.DATE).getValue();


        List<SneezeData> decodedSneezes = new ArrayList<>();
        List<BsonValue> encodedSneezes = doc.getArray(Fields.SNEEZES).getValues();
        for (BsonValue v: encodedSneezes){
            BsonDocument vdoc = (BsonDocument) v;

            Date date = null;
            try {
                date = dayFormat.parse(vdoc.getString(Fields.DATE).getValue());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            SneezeData sd = new SneezeData(date, vdoc.getString(Fields.LOCATION).getValue());
            decodedSneezes.add(sd);
        }


        return new SneezeItem(
                doc.getObjectId(Fields.ID).getValue(),
                doc.getString(Fields.OWNER_ID).getValue(),
                dayDate,
                decodedSneezes
        );
    }

    public static final class Fields {
        public static final String ID = "_id";
        public static final String OWNER_ID = "owner_id";
        public static final String LOCATION = "location";
        public static final String DATE = "date";
        public static final String SNEEZES = "sneezes";
    }

    public static final Codec<SneezeItem> codec = new Codec<SneezeItem>() {

        @Override
        public void encode(
                final BsonWriter writer, final SneezeItem value, final EncoderContext encoderContext) {
            new BsonDocumentCodec().encode(writer, toBsonDocument(value), encoderContext);
        }

        @Override
        public Class<SneezeItem> getEncoderClass() {
            return SneezeItem.class;
        }

        @Override
        public SneezeItem decode(
                final BsonReader reader, final DecoderContext decoderContext) {
            final BsonDocument document = (new BsonDocumentCodec()).decode(reader, decoderContext);
            return fromBsonDocument(document);
        }
    };
}
