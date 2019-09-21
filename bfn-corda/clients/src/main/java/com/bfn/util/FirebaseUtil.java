package com.bfn.util;

import com.bfn.dto.InvoiceOfferDTO;
import com.google.cloud.firestore.Firestore;
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
        Notification m = new Notification("Invoice Offer", GSON.toJson(offer));
        Message message = Message.builder()
                .putData("offer", GSON.toJson(offer))
                .setNotification(m)
                .setTopic(topic)
                .build();

        // Send a message to the devices subscribed to the provided topic.
        String response = FirebaseMessaging.getInstance().sendAsync(message).get();
        // Response is a message ID string.
        logger.info(("\uD83D\uDE21 \uD83D\uDE21 \uD83D\uDE21 " +
                "Successfully sent FCM message to topic: \uD83D\uDE21 ").concat(topic)
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
        logger.info("\uD83D\uDC4C \uD83D\uDC4C \uD83D\uDC4C \uD83E\uDD66  \uD83E\uDD66 User record created in Firebase:  \uD83E\uDD66 ".concat(userRecord.getEmail()));
//        getUsers();
        return userRecord;
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

}
