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
@Region("customer")
public class Customer {

    @Id
    private String id;

//    @Indexed(name = "customerFirstNameRangeIdx",
//            from = "/customer",
//            expression = "firstName",
//            type = IndexType.FUNCTIONAL)
//    @Indexed
    private String firstName;

    private String lastName;

    private int dob;

    private boolean aBoolean;

    private short aShort;

    private int anInt;

    private long aLong;
}