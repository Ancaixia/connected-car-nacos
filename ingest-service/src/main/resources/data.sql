-- 种子车辆：真实场景由车企主数据下发
MERGE INTO vehicle (vin, plate, model, owner, status) KEY (vin) VALUES
('LSVAA2180C2000001', '沪A·88888', 'Model S', '上海测试车队', 'OFFLINE'),
('LSVAA2180C2000002', '京B·12345', 'Model 3', '北京测试车队', 'OFFLINE'),
('LSVAA2180C2000003', '粤C·66666', '汉 EV', '深圳测试车队', 'OFFLINE'),
('LSVAA2180C2000004', '川A·95270', '秦 PLUS', '成都测试车队', 'OFFLINE'),
('LSVAA2180C2000005', '浙B·77777', 'Model Y', '杭州测试车队', 'OFFLINE'),
('LSVAA2180C2000006', '苏C·33333', '宋 PLUS', '南京测试车队', 'OFFLINE');

-- 种子账号：username=VIN，初始密码 123456（明文，首次登录后由 UserService 升级为 bcrypt 哈希）
MERGE INTO app_user (username, password, vin, name, role) KEY (username) VALUES
('LSVAA2180C2000001', '123456', 'LSVAA2180C2000001', '沪A·88888 · 上海测试车队', 'FLEET_ADMIN'),
('LSVAA2180C2000002', '123456', 'LSVAA2180C2000002', '京B·12345 · 北京测试车队', 'FLEET_ADMIN'),
('LSVAA2180C2000003', '123456', 'LSVAA2180C2000003', '粤C·66666 · 深圳测试车队', 'FLEET_ADMIN'),
('LSVAA2180C2000004', '123456', 'LSVAA2180C2000004', '川A·95270 · 成都测试车队', 'FLEET_ADMIN'),
('LSVAA2180C2000005', '123456', 'LSVAA2180C2000005', '浙B·77777 · 杭州测试车队', 'FLEET_ADMIN'),
('LSVAA2180C2000006', '123456', 'LSVAA2180C2000006', '苏C·33333 · 南京测试车队', 'FLEET_ADMIN');
