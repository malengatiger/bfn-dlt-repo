package com.bfn.util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class AuthUtil {
    private final static Logger logger = LoggerFactory.getLogger(AuthUtil.class);

    static final FirebaseAuth auth = FirebaseAuth.getInstance();
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
        logger.info(" \uD83E\uDD66  \uD83E\uDD66 User record created in Firebase:  \uD83E\uDD66 ".concat(userRecord.getEmail()));

        return userRecord;
    }
}
