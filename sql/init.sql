-- ============================================
-- 医院预约挂号系统 数据库初始化脚本
-- 版本: V2.0
-- 数据库: MySQL 8.0
-- ============================================

CREATE DATABASE IF NOT EXISTS hospital_appointment
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE hospital_appointment;

-- ============================================
-- 1. 用户与认证 (8张表)
-- ============================================

-- 系统用户主表
CREATE TABLE sys_user (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(200) NOT NULL COMMENT '密码 BCrypt加密',
  real_name VARCHAR(50) COMMENT '真实姓名',
  phone VARCHAR(20) COMMENT '手机号',
  email VARCHAR(100) COMMENT '邮箱',
  gender TINYINT DEFAULT 0 COMMENT '性别 0未知 1男 2女',
  age INT COMMENT '年龄',
  avatar VARCHAR(500) COMMENT '头像URL',
  role VARCHAR(20) NOT NULL DEFAULT 'PATIENT' COMMENT '角色 PATIENT/DOCTOR/DEPT_ADMIN/SYS_ADMIN',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0禁用 1启用',
  last_login_time DATETIME COMMENT '最后登录时间',
  last_login_ip VARCHAR(50) COMMENT '最后登录IP',
  ext1 VARCHAR(200) COMMENT '扩展字段1',
  ext2 VARCHAR(200) COMMENT '扩展字段2',
  ext3 VARCHAR(200) COMMENT '扩展字段3',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  INDEX idx_role (role),
  INDEX idx_phone (phone),
  INDEX idx_status (status)
) COMMENT '系统用户主表';

-- 角色表
CREATE TABLE sys_role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_code VARCHAR(30) NOT NULL UNIQUE COMMENT '角色编码 ROLE_PATIENT/ROLE_DOCTOR/ROLE_DEPT_ADMIN/ROLE_SYS_ADMIN',
  role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
  description VARCHAR(200) COMMENT '角色描述',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '角色表';

-- 用户角色关联
CREATE TABLE sys_user_role (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  role_id BIGINT NOT NULL COMMENT '角色ID',
  FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
  UNIQUE KEY uk_user_role (user_id, role_id)
) COMMENT '用户角色关联表';

-- 权限表
CREATE TABLE sys_permission (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  perm_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
  perm_name VARCHAR(100) NOT NULL COMMENT '权限名称',
  parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
  perm_type VARCHAR(20) COMMENT '权限类型 MENU/BUTTON/API',
  path VARCHAR(200) COMMENT '路由/API路径',
  icon VARCHAR(100) COMMENT '菜单图标',
  sort_order INT DEFAULT 0 COMMENT '排序',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT '权限表';

-- 角色权限关联
CREATE TABLE sys_role_permission (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  role_id BIGINT NOT NULL COMMENT '角色ID',
  perm_id BIGINT NOT NULL COMMENT '权限ID',
  FOREIGN KEY (role_id) REFERENCES sys_role(id) ON DELETE CASCADE,
  FOREIGN KEY (perm_id) REFERENCES sys_permission(id) ON DELETE CASCADE,
  UNIQUE KEY uk_role_perm (role_id, perm_id)
) COMMENT '角色权限关联表';

-- 登录日志
CREATE TABLE user_login_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  username VARCHAR(50) COMMENT '用户名',
  login_ip VARCHAR(50) COMMENT '登录IP',
  user_agent VARCHAR(500) COMMENT '浏览器UA',
  login_status TINYINT DEFAULT 1 COMMENT '登录状态 0失败 1成功',
  fail_reason VARCHAR(200) COMMENT '失败原因',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_create_time (create_time)
) COMMENT '登录日志';

-- 就诊人管理 (患者最多添加5个)
CREATE TABLE patient_companion (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '所属患者用户ID',
  real_name VARCHAR(50) NOT NULL COMMENT '就诊人姓名',
  id_card VARCHAR(100) COMMENT '身份证号 AES加密存储',
  phone VARCHAR(20) COMMENT '手机号',
  gender TINYINT DEFAULT 0 COMMENT '性别',
  age INT COMMENT '年龄',
  relationship VARCHAR(30) COMMENT '与本人关系 SELF/SPOUSE/PARENT/CHILD/OTHER',
  is_default TINYINT DEFAULT 0 COMMENT '是否默认就诊人',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id)
) COMMENT '就诊人管理';

-- 刷新令牌
CREATE TABLE refresh_token (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  token VARCHAR(500) NOT NULL UNIQUE COMMENT '刷新令牌',
  jti VARCHAR(100) COMMENT 'JWT ID',
  expires_at DATETIME NOT NULL COMMENT '过期时间',
  revoked TINYINT DEFAULT 0 COMMENT '是否已撤销',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  INDEX idx_token (token(100)),
  INDEX idx_user_id (user_id),
  INDEX idx_expires_at (expires_at)
) COMMENT 'JWT刷新令牌';

-- ============================================
-- 2. 医院与医生 (3张表)
-- ============================================

-- 科室表
CREATE TABLE department (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL COMMENT '科室名称',
  code VARCHAR(30) UNIQUE COMMENT '科室编码',
  description TEXT COMMENT '科室描述',
  icon VARCHAR(200) COMMENT '图标',
  address VARCHAR(200) COMMENT '科室位置',
  phone VARCHAR(20) COMMENT '科室电话',
  sort_order INT DEFAULT 0 COMMENT '排序',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0停用 1启用',
  ext1 VARCHAR(200) COMMENT '扩展字段1',
  ext2 VARCHAR(200) COMMENT '扩展字段2',
  ext3 VARCHAR(200) COMMENT '扩展字段3',
  ext4 VARCHAR(200) COMMENT '扩展字段4',
  ext5 VARCHAR(200) COMMENT '扩展字段5',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) COMMENT '科室表';

-- 医生扩展表
CREATE TABLE doctor (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE COMMENT '关联用户ID',
  department_id BIGINT COMMENT '所属科室',
  title VARCHAR(50) COMMENT '职称 主任医师/副主任医师/主治医师/住院医师',
  specialty TEXT COMMENT '擅长领域',
  introduction TEXT COMMENT '个人简介',
  consultation_fee DECIMAL(10,2) DEFAULT 0 COMMENT '挂号费',
  avg_rating DECIMAL(3,2) DEFAULT 5.00 COMMENT '平均评分',
  rating_count INT DEFAULT 0 COMMENT '评价数量',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0停诊 1在岗',
  ext1 VARCHAR(200) COMMENT '扩展字段1',
  ext2 VARCHAR(200) COMMENT '扩展字段2',
  ext3 VARCHAR(200) COMMENT '扩展字段3',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE SET NULL,
  INDEX idx_department (department_id),
  INDEX idx_status (status),
  INDEX idx_avg_rating (avg_rating)
) COMMENT '医生扩展信息表';

-- 医生职称配置
CREATE TABLE doctor_title (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title_name VARCHAR(50) NOT NULL COMMENT '职称名称',
  level INT NOT NULL COMMENT '职称等级 1-5',
  sort_order INT DEFAULT 0 COMMENT '排序',
  status TINYINT DEFAULT 1 COMMENT '状态',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT '医生职称配置表';

-- ============================================
-- 3. 预约与排班 (5张表)
-- ============================================

-- 医生排班表
CREATE TABLE doctor_schedule (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  doctor_id BIGINT NOT NULL COMMENT '医生ID',
  work_date DATE NOT NULL COMMENT '出诊日期',
  time_slot VARCHAR(30) NOT NULL COMMENT '时段 08:00-09:00/09:00-10:00/...等',
  start_time TIME NOT NULL COMMENT '开始时间',
  end_time TIME NOT NULL COMMENT '结束时间',
  max_count INT NOT NULL DEFAULT 30 COMMENT '最大号源数',
  booked_count INT NOT NULL DEFAULT 0 COMMENT '已预约数',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0停诊 1正常 2满号',
  remark VARCHAR(200) COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (doctor_id) REFERENCES doctor(user_id) ON DELETE CASCADE,
  INDEX idx_doctor_date (doctor_id, work_date),
  INDEX idx_work_date (work_date),
  INDEX idx_status (status),
  UNIQUE KEY uk_doctor_date_slot (doctor_id, work_date, time_slot)
) COMMENT '医生排班表';

-- 预约表
CREATE TABLE appointment (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  appointment_no VARCHAR(50) NOT NULL UNIQUE COMMENT '预约单号',
  patient_id BIGINT NOT NULL COMMENT '患者ID',
  companion_id BIGINT COMMENT '就诊人ID 可为空表示本人',
  doctor_id BIGINT NOT NULL COMMENT '医生ID',
  department_id BIGINT COMMENT '科室ID',
  schedule_id BIGINT NOT NULL COMMENT '排班时段ID',
  appointment_date DATE NOT NULL COMMENT '就诊日期',
  time_slot VARCHAR(30) NOT NULL COMMENT '就诊时段',
  queue_number INT COMMENT '排队序号',
  symptom_desc TEXT COMMENT '症状描述',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0待支付 1已预约 2已就诊 3已取消 4已爽约',
  cancel_reason VARCHAR(500) COMMENT '取消原因',
  cancel_time DATETIME COMMENT '取消时间',
  fee DECIMAL(10,2) DEFAULT 0 COMMENT '挂号费',
  ext1 VARCHAR(200) COMMENT '扩展字段1',
  ext2 VARCHAR(200) COMMENT '扩展字段2',
  ext3 VARCHAR(200) COMMENT '扩展字段3',
  ext4 VARCHAR(200) COMMENT '扩展字段4',
  ext5 VARCHAR(200) COMMENT '扩展字段5',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (patient_id) REFERENCES sys_user(id),
  FOREIGN KEY (doctor_id) REFERENCES sys_user(id),
  FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE SET NULL,
  FOREIGN KEY (schedule_id) REFERENCES doctor_schedule(id),
  INDEX idx_patient_id (patient_id),
  INDEX idx_doctor_id (doctor_id),
  INDEX idx_appointment_date (appointment_date),
  INDEX idx_status (status),
  INDEX idx_appointment_no (appointment_no)
) COMMENT '预约表';

-- 支付记录表
CREATE TABLE payment_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  appointment_id BIGINT NOT NULL COMMENT '预约ID',
  payment_no VARCHAR(50) NOT NULL UNIQUE COMMENT '支付流水号',
  user_id BIGINT NOT NULL COMMENT '支付用户ID',
  amount DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  pay_method VARCHAR(20) COMMENT '支付方式 WECHAT/ALIPAY',
  trade_no VARCHAR(100) COMMENT '第三方交易号',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0待支付 1支付成功 2支付失败 3已退款',
  paid_at DATETIME COMMENT '支付时间',
  refund_at DATETIME COMMENT '退款时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (appointment_id) REFERENCES appointment(id),
  FOREIGN KEY (user_id) REFERENCES sys_user(id),
  INDEX idx_user_id (user_id),
  INDEX idx_payment_no (payment_no),
  INDEX idx_status (status)
) COMMENT '支付记录表';

-- 爽约记录表
CREATE TABLE no_show_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '患者ID',
  appointment_id BIGINT NOT NULL COMMENT '预约ID',
  no_show_date DATE NOT NULL COMMENT '爽约日期',
  blocked_until DATE COMMENT '禁止预约截止日期',
  remark VARCHAR(200) COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES sys_user(id),
  FOREIGN KEY (appointment_id) REFERENCES appointment(id),
  INDEX idx_user_id (user_id),
  INDEX idx_no_show_date (no_show_date)
) COMMENT '爽约记录表';

-- 叫号队列表
CREATE TABLE queue_number (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  appointment_id BIGINT NOT NULL UNIQUE COMMENT '预约ID',
  doctor_id BIGINT NOT NULL COMMENT '医生ID',
  queue_number INT NOT NULL COMMENT '排队号',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0等待中 1已叫号 2已过号 3已就诊',
  call_time DATETIME COMMENT '叫号时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (appointment_id) REFERENCES appointment(id) ON DELETE CASCADE,
  FOREIGN KEY (doctor_id) REFERENCES sys_user(id),
  INDEX idx_doctor_date (doctor_id, create_time),
  INDEX idx_status (status)
) COMMENT '叫号队列表';

-- ============================================
-- 4. 医疗 (3张表)
-- ============================================

-- 就诊病历表
CREATE TABLE medical_record (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  appointment_id BIGINT NOT NULL UNIQUE COMMENT '预约ID',
  patient_id BIGINT NOT NULL COMMENT '患者ID',
  doctor_id BIGINT NOT NULL COMMENT '医生ID',
  diagnosis TEXT COMMENT '诊断结果',
  chief_complaint TEXT COMMENT '主诉',
  present_illness TEXT COMMENT '现病史',
  past_history TEXT COMMENT '既往史',
  physical_examination TEXT COMMENT '体格检查',
  advice TEXT COMMENT '医嘱',
  remark TEXT COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (appointment_id) REFERENCES appointment(id),
  FOREIGN KEY (patient_id) REFERENCES sys_user(id),
  FOREIGN KEY (doctor_id) REFERENCES sys_user(id),
  INDEX idx_patient_id (patient_id),
  INDEX idx_doctor_id (doctor_id)
) COMMENT '就诊病历表';

-- 电子处方表
CREATE TABLE prescription (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  medical_record_id BIGINT NOT NULL COMMENT '病历ID',
  patient_id BIGINT NOT NULL COMMENT '患者ID',
  doctor_id BIGINT NOT NULL COMMENT '医生ID',
  medication_name VARCHAR(200) NOT NULL COMMENT '药品名称',
  specification VARCHAR(100) COMMENT '规格',
  dosage VARCHAR(100) COMMENT '用量',
  frequency VARCHAR(100) COMMENT '频次',
  duration VARCHAR(100) COMMENT '疗程',
  total_amount DECIMAL(10,2) DEFAULT 0 COMMENT '金额',
  remark VARCHAR(500) COMMENT '备注',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (medical_record_id) REFERENCES medical_record(id) ON DELETE CASCADE,
  FOREIGN KEY (patient_id) REFERENCES sys_user(id),
  FOREIGN KEY (doctor_id) REFERENCES sys_user(id),
  INDEX idx_patient_id (patient_id),
  INDEX idx_medical_record (medical_record_id)
) COMMENT '电子处方表';

-- 评价表
CREATE TABLE evaluation (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  appointment_id BIGINT NOT NULL UNIQUE COMMENT '预约ID 一个预约只能评价一次',
  patient_id BIGINT NOT NULL COMMENT '患者ID',
  doctor_id BIGINT NOT NULL COMMENT '医生ID',
  rating TINYINT NOT NULL COMMENT '评分 1-5星',
  content TEXT COMMENT '评价内容',
  tags VARCHAR(300) COMMENT '评价标签 逗号分隔',
  reply_content TEXT COMMENT '医生回复',
  reply_time DATETIME COMMENT '回复时间',
  is_anonymous TINYINT DEFAULT 0 COMMENT '是否匿名',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (appointment_id) REFERENCES appointment(id),
  FOREIGN KEY (patient_id) REFERENCES sys_user(id),
  FOREIGN KEY (doctor_id) REFERENCES sys_user(id),
  INDEX idx_doctor_id (doctor_id),
  INDEX idx_rating (rating)
) COMMENT '评价表';

-- ============================================
-- 5. 消息与通知 (4张表)
-- ============================================

-- 站内信
CREATE TABLE site_message (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sender_id BIGINT COMMENT '发送者 0为系统发送',
  receiver_id BIGINT NOT NULL COMMENT '接收者',
  title VARCHAR(200) NOT NULL COMMENT '消息标题',
  content TEXT COMMENT '消息内容',
  msg_type VARCHAR(30) NOT NULL DEFAULT 'SYSTEM' COMMENT '消息类型 SYSTEM/NOTICE/REPLY',
  is_read TINYINT DEFAULT 0 COMMENT '是否已读 0未读 1已读',
  read_time DATETIME COMMENT '阅读时间',
  ext1 VARCHAR(200) COMMENT '扩展字段',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (receiver_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  INDEX idx_receiver_read (receiver_id, is_read),
  INDEX idx_create_time (create_time)
) COMMENT '站内信';

-- 短信发送记录
CREATE TABLE sms_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  phone VARCHAR(20) NOT NULL COMMENT '手机号',
  template_code VARCHAR(50) COMMENT '短信模板代码',
  content TEXT COMMENT '短信内容',
  result_code VARCHAR(20) COMMENT '发送结果码',
  result_msg VARCHAR(200) COMMENT '发送结果消息',
  biz_type VARCHAR(30) COMMENT '业务类型 REGISTER/LOGIN/NOTICE/RESET_PWD',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_phone (phone),
  INDEX idx_create_time (create_time)
) COMMENT '短信发送记录';

-- 通知模板表
CREATE TABLE notification_template (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  template_code VARCHAR(50) NOT NULL UNIQUE COMMENT '模板编码',
  template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
  template_type VARCHAR(20) NOT NULL COMMENT '模板类型 SMS/SITE/BOTH',
  title_template VARCHAR(200) COMMENT '标题模板',
  content_template TEXT NOT NULL COMMENT '内容模板 支持变量占位',
  variables VARCHAR(300) COMMENT '模板变量说明 JSON格式',
  status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '通知模板表';

-- 系统公告表
CREATE TABLE announcement (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(200) NOT NULL COMMENT '公告标题',
  content TEXT NOT NULL COMMENT '公告内容',
  summary VARCHAR(500) COMMENT '公告摘要',
  cover_image VARCHAR(500) COMMENT '封面图',
  publish_status TINYINT DEFAULT 0 COMMENT '发布状态 0草稿 1已发布 2已撤回',
  publisher_id BIGINT COMMENT '发布者',
  published_at DATETIME COMMENT '发布时间',
  is_top TINYINT DEFAULT 0 COMMENT '是否置顶',
  view_count INT DEFAULT 0 COMMENT '浏览次数',
  ext1 VARCHAR(200) COMMENT '扩展字段1',
  ext2 VARCHAR(200) COMMENT '扩展字段2',
  ext3 VARCHAR(200) COMMENT '扩展字段3',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (publisher_id) REFERENCES sys_user(id) ON DELETE SET NULL,
  INDEX idx_publish_status (publish_status),
  INDEX idx_created (create_time)
) COMMENT '系统公告表';

-- ============================================
-- 6. AI 导诊与医患沟通 (5张表)
-- ============================================

-- AI 对话历史
CREATE TABLE ai_chat_history (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  session_id VARCHAR(100) NOT NULL COMMENT '会话ID',
  user_message TEXT NOT NULL COMMENT '用户消息',
  ai_response TEXT NOT NULL COMMENT 'AI回复',
  chat_type VARCHAR(30) DEFAULT 'TRIAGE' COMMENT '对话类型 TRIAGE/FAQ/GUIDE',
  tokens_used INT COMMENT '消耗token数',
  model VARCHAR(50) DEFAULT 'deepseek-chat' COMMENT '使用的模型',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
  INDEX idx_user_id (user_id),
  INDEX idx_session_id (session_id),
  INDEX idx_create_time (create_time)
) COMMENT 'AI对话历史';

-- 医患聊天会话
CREATE TABLE chat_session (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  patient_id BIGINT NOT NULL COMMENT '患者ID',
  doctor_id BIGINT NOT NULL COMMENT '医生ID',
  appointment_id BIGINT COMMENT '关联预约ID 为空则为复诊会话',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0已关闭 1进行中 2待接诊',
  unread_count_patient INT DEFAULT 0 COMMENT '患者未读数',
  unread_count_doctor INT DEFAULT 0 COMMENT '医生未读数',
  last_message_content VARCHAR(500) COMMENT '最后消息内容',
  last_message_time DATETIME COMMENT '最后消息时间',
  last_message_type VARCHAR(20) DEFAULT 'TEXT' COMMENT '最后消息类型 TEXT/IMAGE',
  expire_time DATETIME COMMENT '会话过期时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (patient_id) REFERENCES sys_user(id),
  FOREIGN KEY (doctor_id) REFERENCES sys_user(id),
  FOREIGN KEY (appointment_id) REFERENCES appointment(id) ON DELETE SET NULL,
  INDEX idx_patient (patient_id, status),
  INDEX idx_doctor (doctor_id, status),
  UNIQUE KEY uk_patient_doctor_appt (patient_id, doctor_id, appointment_id)
) COMMENT '医患聊天会话';

-- 聊天消息
CREATE TABLE chat_message (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  session_id BIGINT NOT NULL COMMENT '会话ID',
  sender_id BIGINT NOT NULL COMMENT '发送者',
  receiver_id BIGINT NOT NULL COMMENT '接收者',
  msg_type VARCHAR(20) NOT NULL DEFAULT 'TEXT' COMMENT '消息类型 TEXT/IMAGE/FILE',
  content TEXT COMMENT '消息内容',
  raw_content TEXT COMMENT '原始内容(未过滤)',
  file_url VARCHAR(500) COMMENT '文件URL',
  file_name VARCHAR(200) COMMENT '文件名',
  file_size BIGINT COMMENT '文件大小',
  is_read TINYINT DEFAULT 0 COMMENT '是否已读',
  read_time DATETIME COMMENT '阅读时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (session_id) REFERENCES chat_session(id) ON DELETE CASCADE,
  FOREIGN KEY (sender_id) REFERENCES sys_user(id),
  FOREIGN KEY (receiver_id) REFERENCES sys_user(id),
  INDEX idx_session (session_id, create_time),
  INDEX idx_sender_receiver (sender_id, receiver_id)
) COMMENT '聊天消息';

-- 敏感词库
CREATE TABLE sensitive_word (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  word VARCHAR(100) NOT NULL UNIQUE COMMENT '敏感词',
  replacement VARCHAR(50) DEFAULT '***' COMMENT '替换内容',
  level TINYINT NOT NULL DEFAULT 1 COMMENT '敏感等级 1低 2中 3高 4禁止',
  word_type VARCHAR(30) DEFAULT 'GENERAL' COMMENT '类型 GENERAL/MEDICAL/ABUSE',
  status TINYINT DEFAULT 1 COMMENT '状态 0禁用 1启用',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '敏感词库';

-- 投诉记录
CREATE TABLE complaint (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '投诉人',
  target_user_id BIGINT COMMENT '被投诉人',
  chat_session_id BIGINT COMMENT '关联会话',
  complaint_type VARCHAR(30) COMMENT '投诉类型',
  title VARCHAR(200) NOT NULL COMMENT '投诉标题',
  content TEXT NOT NULL COMMENT '投诉内容',
  evidence_urls TEXT COMMENT '证据图片 逗号分隔',
  status TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0待处理 1处理中 2已处理 3已驳回',
  handle_result TEXT COMMENT '处理结果',
  handler_id BIGINT COMMENT '处理人',
  handled_at DATETIME COMMENT '处理时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES sys_user(id),
  FOREIGN KEY (target_user_id) REFERENCES sys_user(id),
  FOREIGN KEY (chat_session_id) REFERENCES chat_session(id) ON DELETE SET NULL,
  FOREIGN KEY (handler_id) REFERENCES sys_user(id),
  INDEX idx_status (status),
  INDEX idx_user_id (user_id)
) COMMENT '投诉记录';

-- ============================================
-- 7. 系统配置 (3张表)
-- ============================================

-- 系统参数配置
CREATE TABLE system_config (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
  config_value TEXT NOT NULL COMMENT '配置值',
  config_type VARCHAR(30) DEFAULT 'STRING' COMMENT '值类型 STRING/INT/BOOLEAN/JSON',
  description VARCHAR(300) COMMENT '配置说明',
  is_editable TINYINT DEFAULT 1 COMMENT '是否可编辑',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '系统参数配置表';

-- 操作日志
CREATE TABLE operation_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT COMMENT '操作用户',
  username VARCHAR(50) COMMENT '用户名',
  module VARCHAR(100) COMMENT '操作模块',
  operation VARCHAR(100) COMMENT '操作类型',
  request_method VARCHAR(10) COMMENT '请求方法',
  request_url VARCHAR(500) COMMENT '请求URL',
  request_params TEXT COMMENT '请求参数(脱敏)',
  response_code INT COMMENT '响应状态码',
  cost_time BIGINT COMMENT '耗时ms',
  ip VARCHAR(50) COMMENT '操作IP',
  user_agent VARCHAR(500) COMMENT '浏览器UA',
  error_msg TEXT COMMENT '错误信息',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_module (module),
  INDEX idx_create_time (create_time)
) COMMENT '操作日志';

-- 医院信息表
CREATE TABLE hospital_info (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  hospital_name VARCHAR(200) NOT NULL COMMENT '医院名称',
  short_name VARCHAR(100) COMMENT '医院简称',
  logo_url VARCHAR(500) COMMENT 'Logo URL',
  address VARCHAR(300) COMMENT '医院地址',
  phone VARCHAR(20) COMMENT '联系电话',
  emergency_phone VARCHAR(20) COMMENT '急诊电话',
  work_hours VARCHAR(100) COMMENT '上班时间 如 08:00-17:00',
  weekend_hours VARCHAR(100) COMMENT '周末时间',
  introduction TEXT COMMENT '医院简介',
  transportation TEXT COMMENT '交通指南',
  parking_info VARCHAR(300) COMMENT '停车信息',
  website VARCHAR(200) COMMENT '官网地址',
  latitude DECIMAL(10,7) COMMENT '纬度',
  longitude DECIMAL(10,7) COMMENT '经度',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '医院信息表';

-- ============================================
-- 8. 文件上传 (1张表)
-- ============================================

-- 文件上传记录
CREATE TABLE file_upload (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT COMMENT '上传用户',
  original_name VARCHAR(300) NOT NULL COMMENT '原始文件名',
  stored_name VARCHAR(300) NOT NULL COMMENT '存储文件名',
  file_path VARCHAR(500) NOT NULL COMMENT '存储路径',
  file_url VARCHAR(500) COMMENT '访问URL',
  file_size BIGINT COMMENT '文件大小(字节)',
  file_type VARCHAR(50) COMMENT '文件类型 MIME',
  file_ext VARCHAR(20) COMMENT '文件扩展名',
  biz_type VARCHAR(30) COMMENT '业务类型 AVATAR/CERT/REPORT/CHAT',
  biz_id VARCHAR(100) COMMENT '关联业务ID',
  storage_type VARCHAR(20) DEFAULT 'LOCAL' COMMENT '存储类型 LOCAL/OSS',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_id (user_id),
  INDEX idx_biz_type (biz_type, biz_id)
) COMMENT '文件上传记录';

-- ============================================
-- 种子数据
-- ============================================

-- 系统参数默认值
INSERT INTO system_config (config_key, config_value, config_type, description) VALUES
('appointment.advance_days', '7', 'INT', '可预约未来天数'),
('appointment.cancel_hours', '24', 'INT', '就诊前多少小时可取消'),
('appointment.no_show_limit', '3', 'INT', '爽约次数上限'),
('appointment.no_show_block_days', '30', 'INT', '爽约封禁天数'),
('chat.post_visit_window', '7', 'INT', '就诊后可发起咨询天数'),
('chat.session_timeout_hours', '24', 'INT', '会话超时自动关闭小时数'),
('chat.data_retention_years', '3', 'INT', '聊天记录保存年限'),
('ai.model', 'deepseek-chat', 'STRING', 'AI模型'),
('ai.max_tokens', '2000', 'INT', 'AI最大Token数'),
('ai.temperature', '0.7', 'STRING', 'AI温度参数'),
('sms.sign_name', '医院预约系统', 'STRING', '短信签名'),
('file.max_size_mb', '10', 'INT', '文件上传最大MB'),
('file.allowed_types', 'jpg,jpeg,png,gif,pdf,doc,docx', 'STRING', '允许上传的文件类型'),
('security.bcrypt_strength', '10', 'INT', 'BCrypt加密强度');

-- 角色初始化
INSERT INTO sys_role (role_code, role_name, description) VALUES
('ROLE_PATIENT', '患者', '普通患者用户'),
('ROLE_DOCTOR', '医生', '医生用户'),
('ROLE_DEPT_ADMIN', '科室管理员', '科室管理员'),
('ROLE_SYS_ADMIN', '系统管理员', '系统超级管理员');

-- 初始化管理员用户 (密码 admin123, BCrypt加密)
INSERT INTO sys_user (username, password, real_name, phone, role, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', '13800000000', 'SYS_ADMIN', 1);

-- 初始化医院信息
INSERT INTO hospital_info (hospital_name, short_name, address, phone, work_hours, introduction) VALUES
('示例综合医院', '示例医院', 'XX市XX区XX路100号', '010-12345678', '08:00-17:00', '示例综合医院是一家集医疗、教学、科研为一体的三级甲等综合医院。');

-- 初始化科室
INSERT INTO department (name, code, description, sort_order) VALUES
('内科', 'INTERNAL', '诊治内科常见疾病，包括心脑血管、消化、呼吸、内分泌等', 1),
('外科', 'SURGERY', '诊治外科相关疾病，包括普外、骨科、泌尿、神经外科等', 2),
('妇产科', 'GYNECOLOGY', '妇科疾病诊治、孕期检查、分娩、产后康复等', 3),
('儿科', 'PEDIATRICS', '儿童常见病、多发病诊治，儿童保健、预防接种等', 4),
('皮肤科', 'DERMATOLOGY', '皮肤疾病诊治，包括过敏、感染、美容皮肤等', 5),
('眼科', 'OPHTHALMOLOGY', '眼科疾病诊治，包括屈光不正、白内障、青光眼等', 6),
('中医科', 'TCM', '中医内科、针灸、推拿、康复理疗等传统中医诊疗', 7);

-- 初始化医生职称配置
INSERT INTO doctor_title (title_name, level, sort_order) VALUES
('主任医师', 1, 1),
('副主任医师', 2, 2),
('主治医师', 3, 3),
('住院医师', 4, 4);

-- 初始化常见敏感词
INSERT INTO sensitive_word (word, replacement, level, word_type) VALUES
('脏话示例1', '***', 3, 'ABUSE'),
('脏话示例2', '***', 3, 'ABUSE'),
('广告敏感词', '***', 2, 'GENERAL');
