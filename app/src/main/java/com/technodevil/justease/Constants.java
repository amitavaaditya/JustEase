/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.technodevil.justease;

import android.net.Uri;

public class Constants {

    public static final String CONFIRM = "confirm";

    public static final String STATUS = "status";
    public static final String SUCCESS = "success";
    public static final String NEW = "new";

    public static final String ACTION_LOGIN = "login";
    public static final String ACTION_REGISTER = "register";
    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_TOKEN_REFRESH = "tokenRefresh";
    public static final String ACTION_SEND_MESSAGE = "sendMessage";
    public static final String ACTION_UPDATE_PASSWORD = "updatePassword";
    public static final String ACTION_ENQUIRY = "enquiry";
    public static final String ACTION_RESEND_ENQUIRY = "resendEnquiry";
    public static final String ACTION_ACCEPT_ENQUIRY = "acceptEnquiry";
    public static final String ACTION_REQUEST_USER_INFO = "requestUserInfo";

    public static final String LOGIN_STATUS = "loginStatus";
    public static final String LOGIN_SUCCESS = "loginComplete";
    public static final String LOGIN_FAILURE = "loginFailure";

    public static final String REGISTER_STATUS = "registerStatus";
    public static final String REGISTER_SUCCESS = "registerComplete";
    public static final String REGISTER_FAILURE = "registerFailure";

    public static final String UPDATE_STATUS = "updateStatus";
    public static final String UPDATE_SUCCESS = "updateComplete";
    public static final String UPDATE_FAILURE = "updateFailure";

    public static final String PASSWORD_CHANGE_STATUS = "changePasswordStatus";
    public static final String PASSWORD_CHANGE_SUCCESS = "changePasswordComplete";
    public static final String PASSWORD_CHANGE_FAILURE = "changePasswordFailure";

    public static final String ENQUIRY_STATUS = "enquiryStatus";
    public static final String ENQUIRY_SUCCESS = "enquiryComplete";
    public static final String ENQUIRY_FAILURE = "enquiryFailure";

    public static final String ENQUIRY_RESEND_STATUS = "enquiryResendStatus";
    public static final String ENQUIRY_RESEND_SUCCESS = "enquiryResendSuccess";
    public static final String ENQUIRY_RESEND_FAILURE = "enquiryResendFailure";

    public static final String ACCEPT_ENQUIRY_STATUS = "acceptEnquiryStatus";
    public static final String ACCEPT_ENQUIRY_SUCCESS = "acceptEnquiryComplete";
    public static final String ACCEPT_ENQUIRY_FAILURE = "acceptEnquiryFailure";

    public static final String REQUEST_USER_INFO_STATUS = "requestUserInfoStatus";
    public static final String REQUEST_USER_INFO_SUCCESS = "requestUserInfoComplete";
    public static final String REQUEST_USER_INFO_FAILURE = "requestUserInfoFailure";

    public static final String SEND_MESSAGE_STATUS = "sendMessageStatus";
    public static final String SEND_MESSAGE_SUCCESS = "sendMessageComplete";
    public static final String SEND_MESSAGE_FAILURE = "sendMessageFailure";

    public static final String DATA = "data";
    public static final String REGISTRATION_KEY = "registration_id";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String EMAIL_ID = "email_id";
    public static final String FIRST_NAME = "first_name";
    public static final String LAST_NAME = "last_name";
    public static final String MOBILE_NO = "mobile_no";
    public static final String USER_TYPE = "user_type";

    public static final String ACCEPTED = "accepted";
    public static final String PENDING = "pending";
    public static final String MY_CLIENT = "my client";

    public static final String USER = "user";
    public static final String ADMINISTRATOR = "administrator";


    public static final String CHANNELS = "CHANNELS";
    public static final String MY_ENQUIRIES = "MY ENQUIRIES";
    public static final String ENQUIRIES = "ENQUIRIES";
    public static final String MY_CASES = "MY CASES";

    public static final int USER_FRAGMENT_CHANNELS = 0;
    public static final int USER_FRAGMENT_ENQUIRIES = 1;

    public static final int ADMINISTRATOR_FRAGMENT_ENQUIRIES = 0;
    public static final int ADMINISTRATOR_FRAGMENT_CHAT_LIST = 1;

    public static String SERVER_ADDRESS = "http://192.168.0.105/justease/";

    public static final Uri CONTENT_URI_MESSAGES = Uri.parse("content://com.technodevil.justease.provider/messages");
    public static final Uri CONTENT_URI_ENQUIRIES = Uri.parse("content://com.technodevil.justease.provider/enquiries");

    public static final String TABLE_ENQUIRIES = "enquiries";
    public static final String TABLE_MESSAGES = "messages";

    public static final String ENQUIRY_ID = "_id";
    public static final String ENQUIRY_CHANNEL= "enquiry_channel";
    public static final String ENQUIRY_TITLE = "enquiry_title";
    public static final String ENQUIRY = "enquiry";
    public static final String ENQUIRY_DATE_TIME = "enquiry_date_time";
    public static final String ENQUIRY_NEW_MESSAGE_COUNT = "enquiry_new_message_count";
    public static final String ENQUIRY_ACCEPTED = "enquiry_accepted";

    public static final String MESSAGE_ID = "_id";
    public static final String MESSAGE_ENQUIRY_ID = "enquiry_id";
    public static final String MESSAGE = "message_body";
    public static final String MESSAGE_DIRECTION = "direction";
    public static final String MESSAGE_DATE_TIME = "message_date_time";

    public static final String NEW_ENQUIRY_ID = "new_id";

    public static final int ENQUIRIES_ALL_ROWS = 1;
    public static final int ENQUIRIES_SINGLE_ROW = 2;
    public static final int MESSAGES_ALL_ROWS = 3;
    public static final int MESSAGES_SINGLE_ROW = 4;

    public static final String NOTIFICATION_TITLE = "notification_title";
    public static final String NOTIFICATION_BODY = "notification_body";

    public static int BACKOFF_TIME = 15000;

    public static final String INCOMING = "INCOMING";
    public static final String OUTGOING = "OUTGOING";
}