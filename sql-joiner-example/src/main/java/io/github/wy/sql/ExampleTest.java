package io.github.wy.sql;

import io.github.wy.sql.api.JdbcHelper;
import io.github.wy.sql.api.Op;
import io.github.wy.sql.api.SQL;
import org.example.tables.DriverInfoTable;
import org.example.tables.SysUser;
import org.example.tables.SysUserTable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.wy.sql.api.Order.DESC;
import static io.github.wy.sql.api.SQL.SELECT;

/**
 * @author rycat
 * @since 2023/11/22
 */
public class ExampleTest {
    static DataSource dataSource = new DriverManagerDataSource("jdbc:mysql://47.92.205.8:3306/car-network-cloud?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&nullCatalogMeansCurrent=true", "root", "heyt#@!2023");
    static JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    public static void main(String[] args) {
        delete();
    }

    public static void delete() {
        List<Integer> id = Stream.of(50, 51).collect(Collectors.toList());
        SysUserTable u = new SysUserTable();
        int row = SQL.DELETE(u).WHERE(u.ops(u.userId, Op.IN, id)).jdbc(jdbcTemplate);
        System.out.println("删除了：" + row);

    }

    public static void update() {
        SysUserTable u = new SysUserTable();
        SysUser sysUser = new SysUser();

        //sysUser.setUserName("JDBCtest");
        sysUser.setNickName("UPUP测试用户辅导费22");
       /* sysUser.setPlatformType("1");
        sysUser.setLoginIp("192.168.1.1");
        sysUser.setLoginDate(new Date());
        sysUser.setCreateTime(new Date());
        sysUser.setUpdateTime(new Date());
        sysUser.setRemark("just a test");*/

        int row = SQL.UPDATE(u).SET(u.loginIp + "=NULL").WHERE(u.userId(Op.EQ, 51)).jdbc(jdbcTemplate);
        System.out.println("更新结果：" + row);
        List<SysUser> list = SELECT(u.all()).FROM(u.table()).WHERE(u.userId(Op.EQ, 51)).jdbc(jdbcTemplate).withTables(u).list(u.modelClass());

        System.out.println("*****************");
        list.forEach(System.out::println);
    }

    public static void insert() {
        SysUserTable u = new SysUserTable();
        SysUser sysUser = new SysUser();
        sysUser.setDeptId(100L);
        sysUser.setUserName("JDBCtest");
        sysUser.setNickName("测试用户");
        sysUser.setPlatformType("1");
        sysUser.setLoginIp("192.168.1.1");
        sysUser.setLoginDate(new Date());
        sysUser.setCreateTime(new Date());
        sysUser.setUpdateTime(new Date());
        sysUser.setRemark("just a test");
        Optional<Number> key = SQL.INSERT(u).VALUE(sysUser).jdbcReturnKey(jdbcTemplate);
        Long genKey = key.map(Number::longValue).orElse(null);

        System.out.println("自动生成的Key:" + genKey);

        List<SysUser> list = SELECT(u.all()).FROM(u.table()).WHERE(u.userId(Op.EQ, genKey)).jdbc(jdbcTemplate).withTables(u).list(u.modelClass());

        System.out.println("*****************");
        list.forEach(System.out::println);
    }


    public static void simpleQuery() {
        SysUserTable u = new SysUserTable();
        DriverInfoTable d = new DriverInfoTable();
        SysUserTable all = u.changeAlias("h");
      /*  JdbcHelper sql = SELECT().FROM(u.table()).jdbc(jdbcTemplate).withTables(u);
        List<SysUser> list = sql.list(u.modelClass());
        System.out.println(list);*/

        SELECT(u.all(), d.all()).FROM(u.table(), SQL.LEFT_JOIN, d.table()).ON(u.withAlias(u.userId), " = ", d.withAlias(d.userId)).jdbc(jdbcTemplate).withTables(u, d).test();
    }

    public static void complexQuery() {
        DriverInfoTable a = new DriverInfoTable("t_driver_info").as("a");
        DriverInfoTable b = new DriverInfoTable("t_driver_info").as("b");
        DriverInfoTable c = new DriverInfoTable("t_driver_info").as("c");
        DriverInfoTable u = a.changeAlias("u");
        DriverInfoTable e = a.changeAlias("e");
        DriverInfoTable f = a.changeAlias("f");

        JdbcHelper sql = SELECT(f.STAR()).FROM_SUB(
                SELECT(" 20 r", e.STAR()).FROM_SUB(
                        SELECT(u.STAR())
                                .FROM_SUB(
                                        SELECT(a.all()).FROM(a.table()).WHERE(a.id(Op.EQ, 5), SQL.OR, a.id(Op.EQ, 1)).end(),
                                        SQL.UNION_ALL,
                                        SELECT(b.all()).FROM(b.table()).WHERE(b.id(Op.EQ, 5), SQL.OR, b.id(Op.EQ, 44)).end(),
                                        SQL.UNION_ALL,
                                        SELECT(c.all()).FROM(c.table()).WHERE(c.id(Op.EQ, 6), SQL.OR, c.id(Op.EQ, 3)).end()
                                ).AS(u).ORDER_BY(u.birthday_(DESC)).end()
                ).AS(e).end()
        ).AS(f).WHERE(f.fieldOf("r", Op.EQ, 20)).jdbc(jdbcTemplate).withTables(a, b, c, u, e, f);
        System.out.println(sql.map());
        System.out.println(sql.count());
        System.out.println(sql.list(a.modelClass()));
        sql.test();
    }
}
