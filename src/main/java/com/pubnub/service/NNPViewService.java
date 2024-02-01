package com.pubnub.service;

import com.nestwave.device.service.GnssServiceResponse;
import com.nestwave.device.service.NavigationService;
import com.nestwave.model.GnssPositionResults;
import com.nestwave.service.PartnerService;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import com.pubnub.api.enums.PNLogVerbosity;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.model.NNPViewSubmitPositionParameters;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.HttpStatus.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class NNPViewService implements PartnerService {

    final UserId userId;
    PNConfiguration pnConfiguration;
    final String publishKey;
    final String subscribeKey;
    PubNub pubnub;
    final Environment environment;
    final RestTemplate restTemplate;
    public NNPViewService(RestTemplate restTemplate,
                          NavigationService navigationService,
                          Environment environment){
        this.restTemplate = restTemplate;
        this.environment = environment;
        this.publishKey = environment.getProperty("partners.pubnub.publishKey");
        this.subscribeKey = environment.getProperty("partners.pubnub.subscribeKey");
        String userId = "myUniqueTempUserId";

        if(isBlank(publishKey)||isBlank(subscribeKey)){
            log.info("Pubnub publish key or userId is not configured.");
            this.userId = null;
        }else {
            this.userId = new UserId(userId);
            navigationService.register(this);
            try {
                pnConfiguration = new PNConfiguration(this.userId);
                pnConfiguration.setLogVerbosity(PNLogVerbosity.BODY);
                pnConfiguration.setPublishKey(publishKey);
                pnConfiguration.setSubscribeKey(subscribeKey);
                this.pubnub = new PubNub(pnConfiguration);
            } catch (PubNubException e) {
                log.error("An exception occurred when trying to instanciate pubnub service.");
            }
        }
    }
    public GnssServiceResponse nNViewPublishPosition(NNPViewSubmitPositionParameters nNPViewSubmitPositionParameters, int customerId){
        log.info("Informations sent to pubnub : {}", nNPViewSubmitPositionParameters.toString());
        String channel;
        if(customerId != 0){
           channel = Integer.toString(customerId);
        }else{
            channel = environment.getProperty("partners.pubnub.channel");
        }
        log.info("Pubnub channel is :{}", channel);
        try{
            PNPublishResult publishResponse = this.pubnub.publish().message(nNPViewSubmitPositionParameters).channel(channel).sync();
            log.info("Response from pubnub is :{}", publishResponse.toString());
            return new GnssServiceResponse(OK, publishResponse.toString().getBytes());
        } catch (PubNubException e) {
            //throw new RuntimeException(e);
            log.error("error while publishing on PubNub: {}", e);
            return new GnssServiceResponse(INTERNAL_SERVER_ERROR, "Unexpected error"+e);
        }

    }
    @Override
    public GnssServiceResponse onGnssPosition( int customerId, long deviceId, GnssPositionResults gnssPositionResults){
        pnConfiguration.setUserId(new UserId(customerId+"-"+deviceId));
        NNPViewSubmitPositionParameters data = new NNPViewSubmitPositionParameters("NextNav placeholder", "NextNav placeholder", gnssPositionResults.confidence, gnssPositionResults.position, gnssPositionResults.HeightAboveTerrain, true,gnssPositionResults.gpsTime);
        return nNViewPublishPosition(data, customerId);
    }

}
