package com.hospital.appointment.module.admin.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper {

    @Select("SELECT COUNT(*) FROM appointment WHERE appointment_date = CURDATE() AND status IN (1,2)")
    int countTodayAppointments();

    @Select("SELECT COUNT(*) FROM appointment WHERE DATE_FORMAT(appointment_date, '%Y-%m') = #{month}")
    int countMonthAppointments(@Param("month") String month);

    @Select("SELECT COUNT(*) FROM appointment WHERE appointment_date BETWEEN #{start} AND #{end}")
    int countAppointmentsBetween(@Param("start") String start, @Param("end") String end);

    @Select("SELECT COUNT(*) FROM sys_user WHERE role = #{role} AND deleted = 0")
    int countUsersByRole(@Param("role") String role);

    @Select("SELECT d.name AS name, COUNT(a.id) AS cnt FROM department d " +
            "LEFT JOIN appointment a ON d.id = a.department_id AND a.status IN (1,2) " +
            "WHERE d.deleted = 0 GROUP BY d.id, d.name ORDER BY cnt DESC")
    List<Map<String, Object>> getDepartmentStats();

    @Select("SELECT u.real_name AS name, d.title, COUNT(a.id) AS cnt FROM appointment a " +
            "LEFT JOIN sys_user u ON a.doctor_id = u.id " +
            "LEFT JOIN doctor d ON a.doctor_id = d.user_id " +
            "WHERE a.status IN (1,2) AND a.appointment_date BETWEEN #{start} AND #{end} " +
            "GROUP BY a.doctor_id, u.real_name, d.title ORDER BY cnt DESC LIMIT #{limit}")
    List<Map<String, Object>> getTopDoctors(@Param("start") String start, @Param("end") String end, @Param("limit") int limit);

    @Select("SELECT DATE(appointment_date) AS date, COUNT(*) AS cnt FROM appointment " +
            "WHERE appointment_date BETWEEN #{start} AND #{end} AND status IN (1,2) " +
            "GROUP BY DATE(appointment_date) ORDER BY date")
    List<Map<String, Object>> getAppointmentTrend(@Param("start") String start, @Param("end") String end);

    @Select("SELECT SUM(amount) FROM payment_record WHERE status = 1 AND DATE_FORMAT(paid_at, '%Y-%m') = #{month}")
    java.math.BigDecimal getRevenueByMonth(@Param("month") String month);

    @Select("SELECT COUNT(*) FROM sys_user WHERE deleted = 0 AND create_time BETWEEN #{start} AND #{end}")
    int countNewUsersBetween(@Param("start") String start, @Param("end") String end);
}
