package com.fangyuan.redis.bean;

import lombok.*;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author fangyuan
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonInfo implements Serializable {

    private static final long serialVersionUID = -5666930682610937456L;

    @NotNull
    private String userId;

    @NotNull
    private String name;

    @Max(100)
    private Integer age;

    @NotNull
    private String sex;
}
