package com.flowpay.routing.adapter.in.web.webhook.dto;

import java.util.List;

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
