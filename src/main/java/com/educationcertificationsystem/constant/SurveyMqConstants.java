package com.educationcertificationsystem.constant;

public final class SurveyMqConstants {

    public static final String SURVEY_EXCHANGE = "survey.exchange";
    public static final String SURVEY_QUEUE = "survey.publish.queue";
    public static final String SURVEY_ROUTING_KEY = "survey.publish";

    public static final String QUESTIONNAIRE_STATUS_DRAFT = "DRAFT";
    public static final String QUESTIONNAIRE_STATUS_PUBLISHING = "PUBLISHING";
    public static final String QUESTIONNAIRE_STATUS_PUBLISHED = "PUBLISHED";
    public static final String QUESTIONNAIRE_STATUS_PUBLISH_FAILED = "PUBLISH_FAILED";
    public static final String QUESTIONNAIRE_STATUS_REVOKED = "REVOKED";
    public static final String QUESTIONNAIRE_STATUS_ENDED = "ENDED";

    public static final String MQ_STATUS_NONE = "NONE";
    public static final String MQ_STATUS_WAITING = "WAITING";
    public static final String MQ_STATUS_SENT = "SENT";
    public static final String MQ_STATUS_CONSUMED = "CONSUMED";
    public static final String MQ_STATUS_FAILED = "FAILED";

    public static final String TASK_ACTION_PUBLISH = "PUBLISH";
    public static final String TASK_ACTION_RETRY = "RETRY";
    public static final String TASK_ACTION_REMIND = "REMIND";

    public static final String TASK_STATUS_PUBLISHING = "PUBLISHING";
    public static final String TASK_STATUS_PUBLISHED = "PUBLISHED";
    public static final String TASK_STATUS_PUBLISH_FAILED = "PUBLISH_FAILED";
    public static final String TASK_STATUS_REMINDING = "REMINDING";
    public static final String TASK_STATUS_REMINDED = "REMINDED";
    public static final String TASK_STATUS_REMIND_FAILED = "REMIND_FAILED";
    public static final String TASK_STATUS_REVOKED = "REVOKED";

    private SurveyMqConstants() {
    }
}
