package com.bfn.util;

import com.bfn.dto.AccountInfoDTO;
import com.bfn.dto.InvoiceDTO;
import com.bfn.dto.InvoiceOfferDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.auth.*;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FcmOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class FirebaseUtil {
    private final static Logger logger = LoggerFactory.getLogger(FirebaseUtil.class);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static final FirebaseAuth auth = FirebaseAuth.getInstance();
    static final Firestore db = FirestoreClient.getFirestore();
    static final FirebaseMessaging messaging = FirebaseMessaging.getInstance();

    public static void sendInvoiceOfferMessage(InvoiceOfferDTO offer) throws ExecutionException, InterruptedException {

        String topic = "invoiceOffers";
        // See documentation on defining a message payload.
        Notification m = new Notification("New Invoice Offer", GSON.toJson(offer));
        Message message = Message.builder()
                .putData("invoiceOffer", GSON.toJson(offer))
                .setNotification(m)
                .setTopic(topic)
                .build();

        // Send a message to the devices subscribed to the provided topic.
        String response = messaging.sendAsync(message).get();
        // Response is a message ID string.
        logger.info(("\uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 " +
                "Successfully sent FCM INVOICE OFFER message to topic: \uD83D\uDE21 ").concat(topic)
                .concat("; Response: \uD83E\uDD6C \uD83E\uDD6C ").concat(response)
                .concat(" \uD83E\uDD6C \uD83E\uDD6C"));
    }
    public static void sendInvoiceMessage(InvoiceDTO offer) throws ExecutionException, InterruptedException {

        String topic = "invoices";
        // See documentation on defining a message payload.
        Notification m = new Notification("New Invoice", GSON.toJson(offer));
        Message message = Message.builder()
                .putData("invoice", GSON.toJson(offer))
                .setNotification(m)
                .setTopic(topic)
                .build();

        // Send a message to the devices subscribed to the provided topic.
        String response = messaging.sendAsync(message).get();
        // Response is a message ID string.
        logger.info(("\uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 " +
                "Successfully sent FCM INVOICE message to topic: \uD83D\uDE21 ").concat(topic)
                .concat("; Response: \uD83E\uDD6C \uD83E\uDD6C ").concat(response)
                .concat(" \uD83E\uDD6C \uD83E\uDD6C"));
    }
    public static void sendAccountMessage(AccountInfoDTO account) throws ExecutionException, InterruptedException {

        String topic = "accounts";
        // See documentation on defining a message payload.
        Notification m = new Notification("New BFN Account", GSON.toJson(account));
        Message message = Message.builder()
                .putData("account", GSON.toJson(account))
                .setNotification(m)
                .setTopic(topic)
                .build();

        // Send a message to the devices subscribed to the provided topic.
        String response = messaging.sendAsync(message).get();
        // Response is a message ID string.
        logger.info(("\uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 " +
                "Successfully sent FCM ACCOUNT message to topic: \uD83D\uDE21 ").concat(topic)
                .concat("; Response: \uD83E\uDD6C \uD83E\uDD6C ").concat(response)
                .concat(" \uD83E\uDD6C \uD83E\uDD6C"));
    }

    public static UserRecord createUser(String name, String email, String password,
                                        String cellphone,
                                        String uid)
            throws FirebaseAuthException {

        UserRecord.CreateRequest request = new UserRecord.CreateRequest();
        request.setEmail(email);
        request.setDisplayName(name);
        request.setPassword(password);
        if (cellphone != null) {
            request.setPhoneNumber("+".concat(cellphone));
        }
        request.setUid(uid);

        UserRecord userRecord = auth.createUser(request);
        logger.info("\uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C \uD83E\uDD66 \uD83E\uDD66 User record created in Firebase:  \uD83E\uDD66 ".concat(userRecord.getEmail()));
//        getUsers();
        return userRecord;
    }

    public static void deleteUsers() throws FirebaseAuthException {
        // Start listing users from the beginning, 1000 at a time.
        ListUsersPage page = FirebaseAuth.getInstance().listUsers(null);
        while (page != null) {
            for (ExportedUserRecord user : page.getValues()) {
                if (user.getEmail().contains("aubrey")) {
                    continue;
                }
                auth.deleteUser(user.getUid());
                logger.info("\uD83C\uDF4A \uD83C\uDF4A \uD83C\uDF4A User deleted: " + user.getEmail());
            }
            page = page.getNextPage();
        }
        page = auth.listUsers(null);
        for (ExportedUserRecord user : page.iterateAll()) {
            if (user.getEmail().contains("aubrey")) {
                continue;
            }
            logger.info("\uD83C\uDF4A \uD83C\uDF4A \uD83C\uDF4A User deleted: " + user.getEmail());
            auth.deleteUser(user.getUid());

        }
    }

    public static UserRecord getUser(String email) throws FirebaseAuthException {
        UserRecord record = null;
        try {
             record = auth.getUserByEmail(email);
        } catch (Exception e) {

        }
        return record;
    }

    public static List<UserRecord> getUsers() throws FirebaseAuthException {

        List<UserRecord> records = new ArrayList<>();
        ListUsersPage page = auth.listUsers(null);
        Iterable<ExportedUserRecord> m = page.getValues();
        m.forEach(records::add);

        int cnt = 0;
        for (UserRecord record : records) {
            cnt++;
            logger.info("\uD83E\uDD66  \uD83E\uDD66 UserRecord #" +
                    cnt + " from Firebase: ".concat(GSON.toJson(record)));
        }

        return records;
    }

    static void deleteCollections() {
        Iterable<CollectionReference> m = db.listCollections();
        for (CollectionReference reference : m) {
            logger.info("\uD83C\uDF4A \uD83C\uDF4A Existing Firestore collection: ".concat(reference.getPath()));
            deleteCollection(reference, 200);
        }
    }

    /**
     * Delete a collection in batches to avoid out-of-memory errors.
     * Batch size may be tuned based on document size (atmost 1MB) and application requirements.
     */
    private static void deleteCollection(CollectionReference collection, int batchSize) {
        try {
            // retrieve a small batch of documents to avoid out-of-memory errors
            ApiFuture<QuerySnapshot> future = collection.limit(batchSize).get();
            int deleted = 0;
            // future.get() blocks on document retrieval
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                document.getReference().delete();
                ++deleted;
                logger.info(" \uD83E\uDD4F  \uD83E\uDD4F  \uD83E\uDD4F  Deleted document:  \uD83D\uDC9C ".concat(document.getReference().getPath()));
            }
            if (deleted >= batchSize) {
                // retrieve and delete another batch
                deleteCollection(collection, batchSize);
                logger.info(" \uD83E\uDD4F  \uD83E\uDD4F  \uD83E\uDD4F  Deleted collection:  \uD83E\uDDE1 ".concat(collection.getPath()));
            }
        } catch (Exception e) {
            logger.error("Error deleting collection : " + e.getMessage());
        }
    }

    private static int BATCH_SIZE = 2000;

    public static void deleteCollection(String collectionName) throws ExecutionException, InterruptedException {
        // retrieve a small batch of documents to avoid out-of-memory errors
        CollectionReference collection = db.collection(collectionName);
        ApiFuture<QuerySnapshot> future = collection.limit(BATCH_SIZE).get();
        int deleted = 0;
        // future.get() blocks on document retrieval
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        for (QueryDocumentSnapshot document : documents) {
            document.getReference().delete();
            ++deleted;
            logger.info(" \uD83E\uDD4F  \uD83E\uDD4F  \uD83E\uDD4F  Deleted document:  \uD83D\uDC9C ".concat(document.getReference().getPath()));
        }
        if (deleted >= BATCH_SIZE) {
            // retrieve and delete another batch
            deleteCollection(collectionName);
            logger.info(" \uD83E\uDD4F  \uD83E\uDD4F  \uD83E\uDD4F  Deleted collection:  \uD83E\uDDE1 ".concat(collection.getPath()));
        }

    }
}
