package org.example.tables;

import io.github.wy.sql.Table;

import java.util.Date;

/**
 * @author rycat
 * @since 2023/11/22
 */
@Table(name = "t_driver_info")
public class DriverInfo {
    private Integer id;
    private Integer userId;
    private String driverName;
    private Date birthday;
    private String phone;
}
