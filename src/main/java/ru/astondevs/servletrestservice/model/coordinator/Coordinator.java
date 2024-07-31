package ru.astondevs.servletrestservice.model.coordinator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coordinator {
    private Long id;
    private String name;
    private Set<Long> studentIds;
}
