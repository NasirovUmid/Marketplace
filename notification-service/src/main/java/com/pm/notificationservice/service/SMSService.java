package com.pm.notificationservice.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
//@RequiredArgsConstructor
public class SMSService {

    private final Logger logger = LoggerFactory.getLogger(SMSService.class);

    private final String ACCOUNT_SID;


    private final String AUTH_TOKEN;


    private final String OUTGOING_SMS_NUMBER;


    public SMSService(@Value("${TWILIO_ACCOUNT_SID}")String accountSid,@Value("${TWILIO_AUTH_TOKEN}") String authToken,@Value("${TWILIO_OUTGOING_SMS_NUMBER}") String outgoingSmsNumber) {
        ACCOUNT_SID = accountSid;
        AUTH_TOKEN = authToken;
        OUTGOING_SMS_NUMBER = outgoingSmsNumber;
    }

    @PostConstruct
    public void setup(){

        Twilio.init(ACCOUNT_SID,AUTH_TOKEN);
    }

    public String sendSMS(String smsNumber,String smsMessage){

        logger.info("smsNumber = {} ,OUTGOING_SMS_NUMBER = {} ",smsNumber,OUTGOING_SMS_NUMBER);

        Message message = Message.creator(
                new PhoneNumber(smsNumber),
                new PhoneNumber(OUTGOING_SMS_NUMBER),
                smsMessage
        ).create();

        return message.getStatus().toString();
    }
}
