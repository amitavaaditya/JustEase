package com.technodevil.justease.util;

import android.net.Uri;

/**
 * Interface containing all constants
 */

public interface Constants {

    boolean D = true;

    String SERVER_ADDRESS = "http://52.88.72.90/";

    int BACKOFF_TIME = 15000;

    String STATUS = "status";
    String SUCCESS = "success";
    String NEW = "new";

    String ACTION_LOGIN = "login";
    String ACTION_FORGOT_PASSWORD = "forgotPassword";
    String ACTION_REGISTER = "register";
    String ACTION_UPDATE = "update";
    String ACTION_TOKEN_REFRESH = "tokenRefresh";
    String ACTION_SEND_MESSAGE = "sendMessage";
    String ACTION_UPDATE_PASSWORD = "updatePassword";
    String ACTION_ENQUIRY = "enquiry";
    String ACTION_RESEND_ENQUIRY = "resendEnquiry";
    String ACTION_ACCEPT_ENQUIRY = "acceptEnquiry";
    String ACTION_REQUEST_USER_INFO = "requestUserInfo";

    String FORGOT_PASSWORD_STATUS = "forgotPasswordStatus";
    String FORGOT_PASSWORD_SUCCESS = "forgotPasswordComplete";
    String FORGOT_PASSWORD_FAILURE = "forgotPasswordFailure";

    String LOGIN_STATUS = "loginStatus";
    String LOGIN_SUCCESS = "loginComplete";
    String LOGIN_FAILURE = "loginFailure";

    String REGISTER_STATUS = "registerStatus";
    String REGISTER_SUCCESS = "registerComplete";
    String REGISTER_FAILURE = "registerFailure";

    String UPDATE_STATUS = "updateStatus";
    String UPDATE_SUCCESS = "updateComplete";
    String UPDATE_FAILURE = "updateFailure";

    String PASSWORD_CHANGE_STATUS = "changePasswordStatus";
    String PASSWORD_CHANGE_SUCCESS = "changePasswordComplete";
    String PASSWORD_CHANGE_FAILURE = "changePasswordFailure";

    String ENQUIRY_STATUS = "enquiryStatus";
    String ENQUIRY_SUCCESS = "enquiryComplete";
    String ENQUIRY_FAILURE = "enquiryFailure";

    String ENQUIRY_RESEND_STATUS = "enquiryResendStatus";
    String ENQUIRY_RESEND_SUCCESS = "enquiryResendSuccess";
    String ENQUIRY_RESEND_FAILURE = "enquiryResendFailure";

    String ACCEPT_ENQUIRY_STATUS = "acceptEnquiryStatus";
    String ACCEPT_ENQUIRY_SUCCESS = "acceptEnquiryComplete";
    String ACCEPT_ENQUIRY_FAILURE = "acceptEnquiryFailure";

    String REQUEST_USER_INFO_STATUS = "requestUserInfoStatus";
    String REQUEST_USER_INFO_SUCCESS = "requestUserInfoComplete";
    String REQUEST_USER_INFO_FAILURE = "requestUserInfoFailure";

    String SEND_MESSAGE_STATUS = "sendMessageStatus";
    String SEND_MESSAGE_SUCCESS = "sendMessageComplete";
    String SEND_MESSAGE_FAILURE = "sendMessageFailure";

    String DATA = "data";
    String REGISTRATION_KEY = "registration_id";
    String USERNAME = "username";
    String PASSWORD = "password";
    String EMAIL_ID = "email_id";
    String FIRST_NAME = "first_name";
    String LAST_NAME = "last_name";
    String MOBILE_NO = "mobile_no";
    String USER_TYPE = "user_type";

    String ACCEPTED = "accepted";
    String PENDING = "pending";
    String MY_CLIENT = "my client";

    String USER = "user";
    String ADMINISTRATOR = "administrator";

    int USER_FRAGMENT_ENQUIRIES = 0;

    int ADMINISTRATOR_FRAGMENT_ENQUIRIES = 0;
    int ADMINISTRATOR_FRAGMENT_CHAT_LIST = 1;

    Uri CONTENT_URI_MESSAGES = Uri.parse("content://com.technodevil.justease.provider/messages");
    Uri CONTENT_URI_ENQUIRIES = Uri.parse("content://com.technodevil.justease.provider/enquiries");

    String TABLE_ENQUIRIES = "enquiries";
    String TABLE_MESSAGES = "messages";

    String ENQUIRY_ID = "_id";
    String ENQUIRY_CHANNEL= "enquiry_channel";
    String ENQUIRY_TITLE = "enquiry_title";
    String ENQUIRY = "enquiry";
    String ENQUIRY_DATE_TIME = "enquiry_date_time";
    String ENQUIRY_NEW_MESSAGE_COUNT = "enquiry_new_message_count";
    String ENQUIRY_ACCEPTED = "enquiry_accepted";

    String MESSAGE_ID = "_id";
    String MESSAGE_ENQUIRY_ID = "enquiry_id";
    String MESSAGE = "message_body";
    String MESSAGE_DIRECTION = "direction";
    String MESSAGE_DATE_TIME = "message_date_time";

    String NEW_ENQUIRY_ID = "new_id";

    int ENQUIRIES_ALL_ROWS = 1;
    int ENQUIRIES_SINGLE_ROW = 2;
    int MESSAGES_ALL_ROWS = 3;
    int MESSAGES_SINGLE_ROW = 4;

    String NOTIFICATION_TITLE = "notification_title";
    String NOTIFICATION_BODY = "notification_body";
    String NOTIFICATION_ID = "notificationID";

    String INCOMING = "INCOMING";
    String OUTGOING = "OUTGOING";
}