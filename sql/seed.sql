-- 种子数据：医生、排班、患者、预约
USE hospital_appointment;

-- 1. 补充科室描述
UPDATE department SET description = '诊治心血管、呼吸、消化、内分泌、肾脏等内科系统疾病' WHERE code = 'INTERNAL';
UPDATE department SET description = '普外科手术、骨科、神经外科、泌尿外科、胸外科等' WHERE code = 'SURGERY';
UPDATE department SET description = '妇科常见病、产科孕期管理、分娩及产后康复' WHERE code = 'GYNECOLOGY';
UPDATE department SET description = '儿童常见病、生长发育评估、预防接种、新生儿保健' WHERE code = 'PEDIATRICS';
UPDATE department SET description = '各类皮肤病诊治，含过敏、感染、免疫性及美容皮肤科' WHERE code = 'DERMATOLOGY';
UPDATE department SET description = '近视、白内障、青光眼、眼底病、斜弱视、角膜病' WHERE code = 'OPHTHALMOLOGY';
UPDATE department SET description = '中药调理、针灸推拿、拔罐艾灸、康复理疗、中医正骨' WHERE code = 'TCM';

-- 2. 添加医生用户（密码都是 123456，BCrypt加密）
INSERT INTO sys_user (username, password, real_name, phone, role, status) VALUES
('doctor_zhang', '$2a$10$6Bl5PH5wiq2xMS1Ownp4j.YTf73QLukFo.3MSn7S/8TEq4CT7VL3S', '张明远', '13800000001', 'DOCTOR', 1),
('doctor_li', '$2a$10$6Bl5PH5wiq2xMS1Ownp4j.YTf73QLukFo.3MSn7S/8TEq4CT7VL3S', '李雪琴', '13800000002', 'DOCTOR', 1),
('doctor_wang', '$2a$10$6Bl5PH5wiq2xMS1Ownp4j.YTf73QLukFo.3MSn7S/8TEq4CT7VL3S', '王志强', '13800000003', 'DOCTOR', 1),
('doctor_zhao', '$2a$10$6Bl5PH5wiq2xMS1Ownp4j.YTf73QLukFo.3MSn7S/8TEq4CT7VL3S', '赵文博', '13800000004', 'DOCTOR', 1),
('doctor_chen', '$2a$10$6Bl5PH5wiq2xMS1Ownp4j.YTf73QLukFo.3MSn7S/8TEq4CT7VL3S', '陈雨涵', '13800000005', 'DOCTOR', 1),
('doctor_sun', '$2a$10$6Bl5PH5wiq2xMS1Ownp4j.YTf73QLukFo.3MSn7S/8TEq4CT7VL3S', '孙立群', '13800000006', 'DOCTOR', 1),
('doctor_zhou', '$2a$10$6Bl5PH5wiq2xMS1Ownp4j.YTf73QLukFo.3MSn7S/8TEq4CT7VL3S', '周雅文', '13800000007', 'DOCTOR', 1),
('doctor_wu', '$2a$10$6Bl5PH5wiq2xMS1Ownp4j.YTf73QLukFo.3MSn7S/8TEq4CT7VL3S', '吴国栋', '13800000008', 'DOCTOR', 1),
('doctor_lin', '$2a$10$6Bl5PH5wiq2xMS1Ownp4j.YTf73QLukFo.3MSn7S/8TEq4CT7VL3S', '林小梅', '13800000009', 'DOCTOR', 1),
('doctor_huang', '$2a$10$6Bl5PH5wiq2xMS1Ownp4j.YTf73QLukFo.3MSn7S/8TEq4CT7VL3S', '黄志远', '13800000010', 'DOCTOR', 1);

-- 3. 添加患者用户
INSERT INTO sys_user (username, password, real_name, phone, role, status) VALUES
('patient1', '$2a$10$6Bl5PH5wiq2xMS1Ownp4j.YTf73QLukFo.3MSn7S/8TEq4CT7VL3S', '刘小明', '13900000001', 'PATIENT', 1),
('patient2', '$2a$10$6Bl5PH5wiq2xMS1Ownp4j.YTf73QLukFo.3MSn7S/8TEq4CT7VL3S', '马丽', '13900000002', 'PATIENT', 1),
('patient3', '$2a$10$6Bl5PH5wiq2xMS1Ownp4j.YTf73QLukFo.3MSn7S/8TEq4CT7VL3S', '赵大爷', '13900000003', 'PATIENT', 1);

-- 4. 添加科室管理员
INSERT INTO sys_user (username, password, real_name, phone, role, status) VALUES
('dept_admin1', '$2a$10$6Bl5PH5wiq2xMS1Ownp4j.YTf73QLukFo.3MSn7S/8TEq4CT7VL3S', '李主任', '13700000001', 'DEPT_ADMIN', 1);

-- 5. 医生扩展信息
INSERT INTO doctor (user_id, department_id, title, specialty, introduction, consultation_fee, avg_rating, rating_count, status) VALUES
((SELECT id FROM sys_user WHERE username='doctor_zhang'), (SELECT id FROM department WHERE code='INTERNAL'), '主任医师', '心脑血管疾病、高血压、冠心病', '从事内科临床工作30年，擅长心血管疾病的诊断与治疗，对冠心病、高血压有深入研究。', 50.00, 4.8, 126, 1),
((SELECT id FROM sys_user WHERE username='doctor_li'), (SELECT id FROM department WHERE code='INTERNAL'), '副主任医师', '消化系统疾病、胃肠镜', '消化内科专家，擅长胃炎、胃溃疡、炎症性肠病的诊治及内镜诊疗。', 30.00, 4.7, 89, 1),
((SELECT id FROM sys_user WHERE username='doctor_wang'), (SELECT id FROM department WHERE code='SURGERY'), '主任医师', '普外科手术、腹腔镜微创', '外科主任，30余年临床经验，擅长腹腔镜胆囊切除、疝修补等微创手术。', 60.00, 4.9, 203, 1),
((SELECT id FROM sys_user WHERE username='doctor_zhao'), (SELECT id FROM department WHERE code='PEDIATRICS'), '副主任医师', '儿童呼吸道疾病、生长发育', '儿科副主任，擅长儿童哮喘、肺炎、生长发育迟缓等疾病的诊治。', 25.00, 4.6, 67, 1),
((SELECT id FROM sys_user WHERE username='doctor_chen'), (SELECT id FROM department WHERE code='GYNECOLOGY'), '主任医师', '妇科肿瘤、宫颈疾病、内分泌', '妇科主任，专注妇科肿瘤早期筛查与微创手术治疗。', 50.00, 4.8, 145, 1),
((SELECT id FROM sys_user WHERE username='doctor_sun'), (SELECT id FROM department WHERE code='OPHTHALMOLOGY'), '主治医师', '近视防控、白内障、干眼症', '眼科青年骨干，在青少年近视防控和白内障手术方面经验丰富。', 20.00, 4.5, 42, 1),
((SELECT id FROM sys_user WHERE username='doctor_zhou'), (SELECT id FROM department WHERE code='DERMATOLOGY'), '主治医师', '痤疮、湿疹、过敏性皮炎', '皮肤科医生，擅长各类过敏性皮肤病的诊治及激光美容治疗。', 20.00, 4.4, 31, 1),
((SELECT id FROM sys_user WHERE username='doctor_wu'), (SELECT id FROM department WHERE code='TCM'), '副主任医师', '颈肩腰腿痛、中风后遗症、亚健康', '中医康复科医生，擅长针灸推拿、中药调理亚健康状态。', 35.00, 4.7, 78, 1),
((SELECT id FROM sys_user WHERE username='doctor_lin'), (SELECT id FROM department WHERE code='PEDIATRICS'), '主治医师', '新生儿疾病、儿童营养', '儿科医生，专注新生儿保健和儿童营养指导。', 20.00, 4.3, 28, 1),
((SELECT id FROM sys_user WHERE username='doctor_huang'), (SELECT id FROM department WHERE code='SURGERY'), '主任医师', '骨科、关节置换、运动损伤', '骨科主任，擅长关节镜手术及人工关节置换术。', 60.00, 4.9, 167, 1);

-- 6. 医生排班（未来 7 天，部分医生有排班）
INSERT INTO doctor_schedule (doctor_id, work_date, time_slot, start_time, end_time, max_count, booked_count, status) VALUES
-- 张明远 内科 周一周三周五
((SELECT id FROM sys_user WHERE username='doctor_zhang'), CURDATE(), '08:00-09:00', '08:00', '09:00', 10, 3, 1),
((SELECT id FROM sys_user WHERE username='doctor_zhang'), CURDATE(), '09:00-10:00', '09:00', '10:00', 10, 5, 1),
((SELECT id FROM sys_user WHERE username='doctor_zhang'), CURDATE(), '10:00-11:00', '10:00', '11:00', 10, 8, 1),
((SELECT id FROM sys_user WHERE username='doctor_zhang'), DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00-09:00', '08:00', '09:00', 10, 2, 1),
((SELECT id FROM sys_user WHERE username='doctor_zhang'), DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00-10:00', '09:00', '10:00', 10, 0, 1),
((SELECT id FROM sys_user WHERE username='doctor_zhang'), DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00-09:00', '08:00', '09:00', 10, 0, 1),
((SELECT id FROM sys_user WHERE username='doctor_zhang'), DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00-10:00', '09:00', '10:00', 10, 0, 1),
-- 李雪琴 内科 周二周四周六
((SELECT id FROM sys_user WHERE username='doctor_li'), DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00-15:00', '14:00', '15:00', 15, 4, 1),
((SELECT id FROM sys_user WHERE username='doctor_li'), DATE_ADD(CURDATE(), INTERVAL 1 DAY), '15:00-16:00', '15:00', '16:00', 15, 0, 1),
((SELECT id FROM sys_user WHERE username='doctor_li'), DATE_ADD(CURDATE(), INTERVAL 3 DAY), '14:00-15:00', '14:00', '15:00', 15, 0, 1),
((SELECT id FROM sys_user WHERE username='doctor_li'), DATE_ADD(CURDATE(), INTERVAL 3 DAY), '15:00-16:00', '15:00', '16:00', 15, 0, 1),
-- 王志强 外科 周一至周五
((SELECT id FROM sys_user WHERE username='doctor_wang'), CURDATE(), '09:00-10:00', '09:00', '10:00', 8, 5, 1),
((SELECT id FROM sys_user WHERE username='doctor_wang'), CURDATE(), '10:00-11:00', '10:00', '11:00', 8, 2, 1),
((SELECT id FROM sys_user WHERE username='doctor_wang'), DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00-10:00', '09:00', '10:00', 8, 0, 1),
((SELECT id FROM sys_user WHERE username='doctor_wang'), DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00-10:00', '09:00', '10:00', 8, 0, 1),
((SELECT id FROM sys_user WHERE username='doctor_wang'), DATE_ADD(CURDATE(), INTERVAL 3 DAY), '09:00-10:00', '09:00', '10:00', 8, 0, 1),
-- 赵文博 儿科 周一三五
((SELECT id FROM sys_user WHERE username='doctor_zhao'), CURDATE(), '08:00-09:00', '08:00', '09:00', 12, 6, 1),
((SELECT id FROM sys_user WHERE username='doctor_zhao'), CURDATE(), '09:00-10:00', '09:00', '10:00', 12, 3, 1),
((SELECT id FROM sys_user WHERE username='doctor_zhao'), DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00-09:00', '08:00', '09:00', 12, 0, 1),
((SELECT id FROM sys_user WHERE username='doctor_zhao'), DATE_ADD(CURDATE(), INTERVAL 2 DAY), '09:00-10:00', '09:00', '10:00', 12, 0, 1),
-- 陈雨涵 妇科 周二四
((SELECT id FROM sys_user WHERE username='doctor_chen'), DATE_ADD(CURDATE(), INTERVAL 1 DAY), '10:00-11:00', '10:00', '11:00', 10, 7, 1),
((SELECT id FROM sys_user WHERE username='doctor_chen'), DATE_ADD(CURDATE(), INTERVAL 1 DAY), '11:00-12:00', '11:00', '12:00', 10, 0, 1),
((SELECT id FROM sys_user WHERE username='doctor_chen'), DATE_ADD(CURDATE(), INTERVAL 3 DAY), '10:00-11:00', '10:00', '11:00', 10, 0, 1),
-- 孙立群 眼科 天天
((SELECT id FROM sys_user WHERE username='doctor_sun'), CURDATE(), '14:00-15:00', '14:00', '15:00', 15, 3, 1),
((SELECT id FROM sys_user WHERE username='doctor_sun'), DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00-15:00', '14:00', '15:00', 15, 0, 1),
((SELECT id FROM sys_user WHERE username='doctor_sun'), DATE_ADD(CURDATE(), INTERVAL 2 DAY), '14:00-15:00', '14:00', '15:00', 15, 0, 1),
-- 吴国栋 中医科 周一至周六
((SELECT id FROM sys_user WHERE username='doctor_wu'), CURDATE(), '08:00-09:00', '08:00', '09:00', 20, 8, 1),
((SELECT id FROM sys_user WHERE username='doctor_wu'), CURDATE(), '09:00-10:00', '09:00', '10:00', 20, 12, 1),
((SELECT id FROM sys_user WHERE username='doctor_wu'), DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00-09:00', '08:00', '09:00', 20, 0, 1),
((SELECT id FROM sys_user WHERE username='doctor_wu'), DATE_ADD(CURDATE(), INTERVAL 2 DAY), '08:00-09:00', '08:00', '09:00', 20, 0, 1),
-- 黄志远 外科骨科 周二四
((SELECT id FROM sys_user WHERE username='doctor_huang'), DATE_ADD(CURDATE(), INTERVAL 1 DAY), '15:00-16:00', '15:00', '16:00', 6, 2, 1),
((SELECT id FROM sys_user WHERE username='doctor_huang'), DATE_ADD(CURDATE(), INTERVAL 3 DAY), '15:00-16:00', '15:00', '16:00', 6, 0, 1);

-- 7. 系统公告
INSERT INTO announcement (title, content, summary, publish_status, publisher_id, published_at, is_top, view_count) VALUES
('2026年端午节门诊安排通知', '各位患者：6月19日至6月21日端午节期间，我院门诊正常开放，急诊24小时照常运行。部分专家门诊停诊信息请查看具体医生排班。祝大家端午安康！', '端午节门诊正常开放', 1, 1, NOW(), 1, 328),
('我院新增智能导诊服务', '为方便广大患者就诊，我院即日起开通AI智能导诊服务。您可以通过系统描述症状，AI将为您推荐合适的就诊科室。该服务仅供参考，不能替代专业医生诊断。', 'AI智能导诊上线', 1, 1, DATE_SUB(NOW(), INTERVAL 5 DAY), 1, 562),
('关于医保结算系统升级的通知', '接市医保局通知，6月25日起我院医保结算系统将进行升级维护，届时可能影响医保实时结算，建议自费患者先自费结算后回参保地报销。预计7月1日恢复正常。', '医保系统升级通知', 1, 1, DATE_SUB(NOW(), INTERVAL 10 DAY), 0, 189),
('夏季高温防暑健康提示', '近期气温持续升高，提醒大家注意防暑降温。老年人、儿童及慢性病患者应避免高温时段外出，多饮水、清淡饮食。如有头晕、恶心等中暑症状请及时就医。', '夏季防暑健康提示', 1, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), 0, 156);

-- 8. 示例预约记录
INSERT INTO appointment (appointment_no, patient_id, doctor_id, department_id, schedule_id, appointment_date, time_slot, symptom_desc, status, fee) VALUES
('APT20260605001', (SELECT id FROM sys_user WHERE username='patient1'), (SELECT id FROM sys_user WHERE username='doctor_zhang'), (SELECT id FROM department WHERE code='INTERNAL'), 1, CURDATE(), '08:00-09:00', '最近一周胸闷气短，偶尔心慌', 1, 50.00),
('APT20260605002', (SELECT id FROM sys_user WHERE username='patient2'), (SELECT id FROM sys_user WHERE username='doctor_wang'), (SELECT id FROM department WHERE code='SURGERY'), 12, CURDATE(), '09:00-10:00', '腹部隐痛两周，饭后加重', 1, 60.00),
('APT20260603001', (SELECT id FROM sys_user WHERE username='patient1'), (SELECT id FROM sys_user WHERE username='doctor_zhang'), (SELECT id FROM department WHERE code='INTERNAL'), 1, DATE_SUB(CURDATE(), INTERVAL 2 DAY), '08:00-09:00', '感冒咳嗽一周未愈', 2, 50.00),
('APT20260602001', (SELECT id FROM sys_user WHERE username='patient3'), (SELECT id FROM sys_user WHERE username='doctor_zhao'), (SELECT id FROM department WHERE code='PEDIATRICS'), 17, DATE_SUB(CURDATE(), INTERVAL 3 DAY), '08:00-09:00', '带孩子来复查，咳嗽好转', 2, 25.00),
('APT20260601001', (SELECT id FROM sys_user WHERE username='patient1'), (SELECT id FROM sys_user WHERE username='doctor_wu'), (SELECT id FROM department WHERE code='TCM'), 28, DATE_SUB(CURDATE(), INTERVAL 4 DAY), '08:00-09:00', '腰疼三个月，久坐加重', 4, 35.00),
('APT20260528001', (SELECT id FROM sys_user WHERE username='patient2'), (SELECT id FROM sys_user WHERE username='doctor_chen'), (SELECT id FROM department WHERE code='GYNECOLOGY'), 21, DATE_SUB(CURDATE(), INTERVAL 8 DAY), '10:00-11:00', '常规妇科检查', 2, 50.00);

-- 9. 就诊病历
INSERT INTO medical_record (appointment_id, patient_id, doctor_id, diagnosis, chief_complaint, present_illness, advice) VALUES
(3, (SELECT id FROM sys_user WHERE username='patient1'), (SELECT id FROM sys_user WHERE username='doctor_zhang'), '急性上呼吸道感染', '感冒咳嗽一周未愈', '患者一周前受凉后出现咽痛、流涕，继而咳嗽，以干咳为主。无发热。自行服用感冒药效果不佳。', '1. 多饮水，注意休息 2. 继续服用止咳药物 3. 如出现发热请及时就诊'),
(4, (SELECT id FROM sys_user WHERE username='patient3'), (SELECT id FROM sys_user WHERE username='doctor_zhao'), '支气管炎恢复期', '咳嗽好转后来复查', '听诊双肺呼吸音清，未闻及干湿啰音。咳嗽明显减少，精神状态良好。', '继续观察，无需特殊处理。注意防寒保暖。'),
(6, (SELECT id FROM sys_user WHERE username='patient2'), (SELECT id FROM sys_user WHERE username='doctor_chen'), '体检未见明显异常', '常规妇科检查', '妇科检查及B超未见异常。宫颈涂片结果正常。', '建议每年一次妇科常规体检。');

-- 10. 评价
INSERT INTO evaluation (appointment_id, patient_id, doctor_id, rating, content, tags) VALUES
(3, (SELECT id FROM sys_user WHERE username='patient1'), (SELECT id FROM sys_user WHERE username='doctor_zhang'), 5, '张医生非常耐心，问诊很详细，开的药也很有效。', '态度好,专业,有效'),
(4, (SELECT id FROM sys_user WHERE username='patient3'), (SELECT id FROM sys_user WHERE username='doctor_zhao'), 4, '赵医生对小朋友很有耐心，候诊时间稍长了些。', '耐心,专业'),
(6, (SELECT id FROM sys_user WHERE username='patient2'), (SELECT id FROM sys_user WHERE username='doctor_chen'), 5, '陈医生很温柔，检查过程很放松，解释也很清楚。', '温柔,专业,细心');

-- 11. 站内信
INSERT INTO site_message (sender_id, receiver_id, title, content, msg_type, is_read) VALUES
(0, (SELECT id FROM sys_user WHERE username='patient1'), '预约成功通知', '您已成功预约张明远医生，就诊时间：今天 08:00-09:00，请按时就诊。', 'NOTICE', 0),
(0, (SELECT id FROM sys_user WHERE username='patient2'), '预约成功通知', '您已成功预约王志强医生，就诊时间：今天 09:00-10:00，请按时就诊。', 'NOTICE', 1),
(0, (SELECT id FROM sys_user WHERE username='patient1'), '复诊提醒', '张明远医生建议您一周后复诊，如有不适请及时就医。', 'NOTICE', 1);
