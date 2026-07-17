package com.educationcertificationsystem.mq;

import com.educationcertificationsystem.constant.SurveyMqConstants;
import com.educationcertificationsystem.model.dto.survey.SurveyPublishEvent;
import com.educationcertificationsystem.survey.service.SurveyQuestionnaireService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SurveyPublishListener {

    private final SurveyQuestionnaireService surveyQuestionnaireService;

    @RabbitListener(queues = SurveyMqConstants.SURVEY_QUEUE)
    public void onPublish(SurveyPublishEvent event) {
        if (event == null || event.getTaskId() == null || event.getQuestionnaireId() == null) {
            log.error("Receive invalid survey publish event: {}", event);
            return;
        }
        surveyQuestionnaireService.handlePublishEvent(event);
    }
}
