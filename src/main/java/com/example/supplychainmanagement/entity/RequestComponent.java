package com.example.supplychainmanagement.entity;

import com.example.supplychainmanagement.entity.users.User;
import com.example.supplychainmanagement.model.enums.RequestStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Table(name = "request_components")
public class RequestComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator
    private UUID id;

    @JsonIgnore
    @ManyToOne
    private Component component;

    @Transient
    private String ArticleCode;

    private int qty;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime requestDate;

    @JsonIgnore
    @ManyToOne
    private User supplier;

    @Enumerated(EnumType.STRING)
    private RequestStatus requestStatus = RequestStatus.OPEN;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String Comment;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LocalDateTime updated;
}
