package com.hospital.appointment.module.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.appointment.module.hospital.model.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

    @Select("""
        <script>
        SELECT d.*, u.real_name, u.avatar, dept.name AS department_name
        FROM doctor d
        LEFT JOIN sys_user u ON d.user_id = u.id
        LEFT JOIN department dept ON d.department_id = dept.id
        WHERE d.status = 1 AND u.deleted = 0
        <if test='departmentId != null'> AND d.department_id = #{departmentId} </if>
        <if test='name != null and name != \"\"'> AND u.real_name LIKE CONCAT('%', #{name}, '%') </if>
        ORDER BY d.avg_rating DESC
        </script>
        """)
    List<Doctor> selectWithDetails(@Param("departmentId") Long departmentId, @Param("name") String name);

    @Select("""
        SELECT d.*, u.real_name, u.avatar, dept.name AS department_name
        FROM doctor d
        LEFT JOIN sys_user u ON d.user_id = u.id
        LEFT JOIN department dept ON d.department_id = dept.id
        WHERE d.user_id = #{userId}
        """)
    Doctor selectDetailById(@Param("userId") Long userId);
}
