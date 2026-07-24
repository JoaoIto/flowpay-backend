package com.flowpay.routing.adapter.in.web.webhook.dto;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload oficial do Webhook do WhatsApp/Meta", example = """
{
  "object": "whatsapp_business_account",
  "entry": [
    {
      "id": "WHATSAPP_BUSINESS_ACCOUNT_ID",
      "changes": [
        {
          "field": "messages",
          "value": {
            "messaging_product": "whatsapp",
            "metadata": {
              "display_phone_number": "5511999999999",
              "phone_number_id": "1234567890"
            },
            "contacts": [
              {
                "profile": {
                  "name": "Cliente VIP"
                },
                "wa_id": "5511988887777"
              }
            ],
            "messages": [
              {
                "from": "5511988887777",
                "id": "wamid.HBgLNTUxMTk4O...",
                "timestamp": "1721590000",
                "text": {
                  "body": "Preciso de ajuda com meu faturamento!"
                },
                "type": "text"
              }
            ]
          }
        }
      ]
    }
  ]
}
""")
public record MetaWebhookPayload(String object, List<Entry> entry) {
    public record Entry(String id, List<Change> changes) {}
    public record Change(String field, Value value) {}
    public record Value(String messaging_product, Metadata metadata, List<Contact> contacts, List<Message> messages) {}
    public record Metadata(String display_phone_number, String phone_number_id) {}
    public record Contact(Profile profile, String wa_id) {}
    public record Profile(String name) {}
    public record Message(String from, String id, String timestamp, Text text, String type) {}
    public record Text(String body) {}
}
