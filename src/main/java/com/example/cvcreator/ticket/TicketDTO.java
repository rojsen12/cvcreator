package com.example.cvcreator.ticket;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TicketDTO {
    private String subject;
    private String message;
    private String category;
    private String priority;
}