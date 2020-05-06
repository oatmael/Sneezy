package com.example.sneezyapplication.data;

import org.bson.BsonDocument;
import org.bson.BsonObjectId;
import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonWriter;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.ObjectId;

public class SneezeItem {

    public static final String SNEEZE_DATABASE = "LogTable";
    public static final String SNEEZE_COLLECTION = "Sneezes";

    private final ObjectId _id;
    private final String owner_id;
    private final String location;

    public SneezeItem(ObjectId _id, String owner_id, String location) {
        this._id = _id;
        this.owner_id = owner_id;
        this.location = location;
    }

    public ObjectId get_id() {
        return _id;
    }

    public String getOwner_id() {
        return owner_id;
    }

    public String getLocation() {
        return location;
    }



    static BsonDocument toBsonDocument(final SneezeItem item) {
        final BsonDocument asDoc = new BsonDocument();
        asDoc.put(Fields.ID, new BsonObjectId(item.get_id()));
        asDoc.put(Fields.OWNER_ID, new BsonString(item.getOwner_id()));
        asDoc.put(Fields.LOCATION, new BsonString(item.getLocation()));
        return asDoc;
    }

    static SneezeItem fromBsonDocument(final BsonDocument doc) {
        return new SneezeItem(
                doc.getObjectId(Fields.ID).getValue(),
                doc.getString(Fields.OWNER_ID).getValue(),
                doc.getString(Fields.LOCATION).getValue()
        );
    }

    static final class Fields {
        static final String ID = "_id";
        static final String OWNER_ID = "owner_id";
        static final String LOCATION = "location";
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
