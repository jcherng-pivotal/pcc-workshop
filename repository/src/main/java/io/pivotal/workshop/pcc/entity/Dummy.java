package io.pivotal.workshop.pcc.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Region;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Region("dummy")
public class Dummy {

    @Id
    private Long id;

    private boolean aBoolean;

    private short aShort;

    private int anInt;

    private long aLong;
}