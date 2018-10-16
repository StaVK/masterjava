package ru.javaops.masterjava.persist.model;

import lombok.*;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class City {
    private @NonNull String code;
    private @NonNull String name;
}
