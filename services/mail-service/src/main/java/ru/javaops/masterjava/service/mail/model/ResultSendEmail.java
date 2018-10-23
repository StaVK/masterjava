package ru.javaops.masterjava.service.mail.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultSendEmail extends BaseEntity {
    private @NonNull String from;
    private @NonNull String to;
    private @NonNull String message;

}
