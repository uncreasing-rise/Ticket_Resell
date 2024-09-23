package com.swd392.ticket_resell_be.dtos.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class WebhookRequest {
    private String webhookUrl;
   public WebhookRequest() {}

}
