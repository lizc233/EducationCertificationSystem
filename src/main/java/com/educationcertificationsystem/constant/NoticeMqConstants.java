package com.educationcertificationsystem.constant;

public final class NoticeMqConstants {

    public static final String NOTICE_EXCHANGE = "notice.exchange";
    public static final String NOTICE_QUEUE = "notice.push.queue";
    public static final String NOTICE_ROUTING_KEY = "notice.publish";
    public static final String NOTICE_MQ_TOPIC = "notice";

    public static final String NOTICE_STATUS_DRAFT = "DRAFT";
    public static final String NOTICE_STATUS_PUBLISHING = "PUBLISHING";
    public static final String NOTICE_STATUS_PUBLISHED = "PUBLISHED";
    public static final String NOTICE_STATUS_PUBLISH_FAILED = "PUBLISH_FAILED";

    public static final String PUSH_STATUS_WAITING = "WAITING";
    public static final String PUSH_STATUS_SENT = "SENT";
    public static final String PUSH_STATUS_CONSUMED = "CONSUMED";
    public static final String PUSH_STATUS_FAILED = "FAILED";

    private NoticeMqConstants() {
    }
}
