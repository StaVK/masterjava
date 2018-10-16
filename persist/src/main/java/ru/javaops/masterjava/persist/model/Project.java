package ru.javaops.masterjava.persist.model;

import lombok.*;

import java.util.Set;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public class Project extends BaseEntity{
    private @NonNull String name;
    private @NonNull String description;
}
