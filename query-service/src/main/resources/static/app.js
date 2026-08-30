/*
 * 车联网云端数据平台大屏 —— Vue 3 (Composition API, 全局构建版)
 *
 * 学习要点：
 * 1. createApp + 组件化：页面拆成 PipelineStrip / StatCard / VehicleTable / Sparkline / AlarmFeed
 * 2. ref() 管理响应式数据，computed() 派生数据，watch/computed 处理联动
 * 3. 生命周期 onMounted 启动轮询，onBeforeUnmount 清理定时器
 * 4. fetch 调用 Spring Boot REST API，实现前后端分离
 */
const { createApp, ref, computed, onMounted, onBeforeUnmount } = Vue;

/* ---------- 组件 1：数据链路状态条 ---------- */
const PipelineStrip = {
    props: { pipeline: { type: Object, required: true } },
    computed: {
        stages() {
            const names = {
                simulator: '车端模拟器',
                gateway: 'MQTT 网关',
                kafka: 'Kafka',
                streamProcessor: 'Flink 流处理',
                storage: '时序存储'
            };
            return Object.entries(names).map(([key, label]) => ({
                key,
                label,
                count: this.pipeline[key] || 0
            }));
        }
    },
    template: `
        <div class="pipeline-strip">
            <div class="pipe-node" v-for="s in stages" :key="s.key">
                <span class="dot"></span>
                <div class="name">{{ s.label }}</div>
                <div class="count">{{ s.count.toLocaleString() }}</div>
            </div>
        </div>`
};

/* ---------- 组件 2：指标卡片 ---------- */
const StatCard = {
    props: {
        label: { type: String, required: true },
        value: { type: [String, Number], required: true },
        hint: { type: String, default: '' },
        color: { type: String, default: '' }
    },
    template: `
        <div class="stat-card">
            <div class="label">{{ label }}</div>
            <div class="value" :style="color ? { color: color } : {}">{{ value }}</div>
            <div class="hint" v-if="hint">{{ hint }}</div>
        </div>`
};

/* ---------- 组件 3：车辆列表 ---------- */
const VehicleTable = {
    props: {
        vehicles: { type: Array, default: () => [] },
        selectedVin: { type: String, default: '' }
    },
    emits: ['select'],
    methods: {
        fmtTime(ts) {
            return ts ? ts.replace('T', ' ').slice(0, 19) : '-';
        }
    },
    template: `
        <table class="vehicle-table">
            <thead>
                <tr>
                    <th>车牌</th><th>车型</th><th>状态</th><th>车速</th><th>位置</th><th>最后上报</th>
                </tr>
            </thead>
            <tbody>
                <tr v-for="v in vehicles" :key="v.vin"
                    :class="{ selected: v.vin === selectedVin }"
                    @click="$emit('select', v.vin)">
                    <td>{{ v.plate }}</td>
                    <td>{{ v.model }}</td>
                    <td>
                        <span class="status-badge"
                              :class="v.status === 'ONLINE' ? 'status-online' : 'status-offline'">
                            {{ v.status === 'ONLINE' ? '在线' : '离线' }}
                        </span>
                    </td>
                    <td class="speed-value">{{ Number(v.lastSpeed || 0).toFixed(1) }} km/h</td>
                    <td>{{ Number(v.lastLat || 0).toFixed(3) }}, {{ Number(v.lastLon || 0).toFixed(3) }}</td>
                    <td>{{ fmtTime(v.lastSeen) }}</td>
                </tr>
                <tr v-if="!vehicles.length">
                    <td colspan="6" class="empty">暂无车辆数据</td>
                </tr>
            </tbody>
        </table>`
};

/* ---------- 组件 4：SVG 实时曲线（零依赖手写，替代 ECharts 教学示例） ---------- */
const Sparkline = {
    props: {
        points: { type: Array, default: () => [] },
        color: { type: String, default: '#22d3ee' }
    },
    computed: {
        max() {
            return Math.max(1, ...this.points.map(p => p.y));
        },
        min() {
            return Math.min(0, ...this.points.map(p => p.y));
        },
        /* 把数据点映射为 SVG 路径 */
        path() {
            if (!this.points.length) return '';
            const W = 640, H = 150, pad = 8;
            const span = (this.max - this.min) || 1;
            const stepX = (W - pad * 2) / Math.max(1, this.points.length - 1);
            return this.points.map((p, i) => {
                const x = pad + i * stepX;
                const y = H - pad - ((p.y - this.min) / span) * (H - pad * 2);
                return (i === 0 ? 'M' : 'L') + x.toFixed(1) + ',' + y.toFixed(1);
            }).join(' ');
        },
        areaPath() {
            if (!this.path) return '';
            const H = 150, pad = 8;
            const firstX = pad;
            const lastX = 640 - pad;
            return this.path + ' L' + lastX + ',' + (H - pad) + ' L' + firstX + ',' + (H - pad) + ' Z';
        },
        lastPoint() {
            return this.points.length ? this.points[this.points.length - 1] : null;
        },
        lastCoord() {
            if (!this.lastPoint) return null;
            const W = 640, H = 150, pad = 8;
            const span = (this.max - this.min) || 1;
            const stepX = (W - pad * 2) / Math.max(1, this.points.length - 1);
            const i = this.points.length - 1;
            return {
                x: pad + i * stepX,
                y: H - pad - ((this.lastPoint.y - this.min) / span) * (H - pad * 2)
            };
        }
    },
    template: `
        <svg viewBox="0 0 640 150" style="width:100%;height:150px">
            <defs>
                <linearGradient id="sparkFill" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" :stop-color="color" stop-opacity="0.35"/>
                    <stop offset="100%" :stop-color="color" stop-opacity="0.02"/>
                </linearGradient>
            </defs>
            <path :d="areaPath" fill="url(#sparkFill)"/>
            <path :d="path" :stroke="color" stroke-width="2" fill="none"/>
            <circle v-if="lastCoord" :cx="lastCoord.x" :cy="lastCoord.y" r="4" :fill="color"/>
        </svg>`
};

/* ---------- 组件 5：报警事件流 ---------- */
const AlarmFeed = {
    props: { alarms: { type: Array, default: () => [] } },
    methods: {
        fmtTime(ts) {
            return ts ? ts.replace('T', ' ').slice(0, 19) : '-';
        },
        severityLabel(s) {
            return { HIGH: '高危', MEDIUM: '中危', LOW: '低危' }[s] || s;
        }
    },
    template: `
        <ul class="alarm-list">
            <li v-for="a in alarms" :key="a.id" class="alarm-item">
                <span class="severity" :class="'severity-' + a.severity">{{ severityLabel(a.severity) }}</span>
                <div class="msg"><b>{{ a.plate || a.vin }}</b> · {{ a.message }}</div>
                <span class="time">{{ fmtTime(a.ts) }}</span>
            </li>
            <li v-if="!alarms.length" class="empty">暂无报警（可把模拟器速度上限调高触发超速报警）</li>
        </ul>`
};

/* ---------- 根组件 ---------- */
const App = {
    components: { PipelineStrip, StatCard, VehicleTable, Sparkline, AlarmFeed },
    setup() {
        const pipeline = ref({});
        const summary = ref({
            vehicleCount: 0, onlineCount: 0,
            telemetryCount: 0, alarmCount: 0, avgSpeedLastMinute: 0
        });
        const vehicles = ref([]);
        const alarms = ref([]);
        const selectedVin = ref('');
        const telemetry = ref([]);

        let timer = null;

        async function fetchJSON(url) {
            const headers = {};
            const token = localStorage.getItem('cc_token');
            if (token) headers['Authorization'] = 'Bearer ' + token;
            const res = await fetch(url, { headers });
            if (res.status === 401) {
                // 会话失效，清理并跳登录页
                localStorage.removeItem('cc_token');
                localStorage.removeItem('cc_user');
                location.href = '/login.html';
                throw new Error('未登录或会话已过期');
            }
            if (!res.ok) {
                throw new Error(url + ' -> HTTP ' + res.status);
            }
            return res.json();
        }

        async function refreshDetail() {
            if (!selectedVin.value) return;
            const [history] = await Promise.all([
                fetchJSON('/api/vehicles/' + selectedVin.value + '/telemetry?limit=80'),
                fetchJSON('/api/vehicles/' + selectedVin.value + '/alarms?limit=30')
            ]);
            telemetry.value = history;
        }

        async function refresh() {
            try {
                const [p, s, v, a] = await Promise.all([
                    fetchJSON('/api/dashboard/pipeline'),
                    fetchJSON('/api/dashboard/summary'),
                    fetchJSON('/api/vehicles'),
                    fetchJSON('/api/alarms/recent?limit=30')
                ]);
                pipeline.value = p;
                summary.value = s;
                vehicles.value = v;
                alarms.value = a;
                if (!selectedVin.value && v.length) {
                    selectedVin.value = v[0].vin;
                }
                await refreshDetail();
            } catch (err) {
                console.error('刷新失败', err);
            }
        }

        function onSelect(vin) {
            selectedVin.value = vin;
            refreshDetail();
        }

        const plateOf = vin => {
            const hit = vehicles.value.find(v => v.vin === vin);
            return hit ? hit.plate : vin;
        };

        /* ---- 派生数据 ---- */
        const selectedPlate = computed(() => plateOf(selectedVin.value));
        const speedPoints = computed(() =>
            telemetry.value.map(t => ({ y: Number(t.speed || 0) })));
        const lastSpeed = computed(() =>
            speedPoints.value.length ? speedPoints.value[speedPoints.value.length - 1].y.toFixed(1) : '-');
        const maxSpeed = computed(() =>
            speedPoints.value.length
                ? Math.max(...speedPoints.value.map(p => p.y)).toFixed(1)
                : '-');
        const alarmsWithPlate = computed(() =>
            alarms.value.map(a => ({ ...a, plate: plateOf(a.vin) })));
        const fmt = n => Number(n || 0).toLocaleString();

        /* ---- 登录守卫：无 token 直接跳登录页 ---- */
        async function ensureLogin() {
            const token = localStorage.getItem('cc_token');
            if (!token) {
                location.href = '/login.html';
                return false;
            }
            try {
                const res = await fetch('/api/auth/check', {
                    headers: { 'Authorization': 'Bearer ' + token }
                });
                const data = await res.json();
                if (!data.success) {
                    localStorage.removeItem('cc_token');
                    location.href = '/login.html';
                    return false;
                }
                return true;
            } catch (e) {
                location.href = '/login.html';
                return false;
            }
        }

        async function logout() {
            const token = localStorage.getItem('cc_token');
            if (token) {
                await fetch('/api/auth/logout', {
                    method: 'POST',
                    headers: { 'Authorization': 'Bearer ' + token }
                }).catch(() => {});
            }
            localStorage.removeItem('cc_token');
            localStorage.removeItem('cc_user');
            location.href = '/login.html';
        }

        /* ---- 生命周期：挂载后每 2 秒刷新，卸载时清理 ---- */
        onMounted(async () => {
            const ok = await ensureLogin();
            if (!ok) return;
            refresh();
            timer = setInterval(refresh, 2000);
        });
        onBeforeUnmount(() => {
            if (timer) clearInterval(timer);
        });

        return {
            pipeline, summary, vehicles, alarms,
            selectedVin, telemetry, selectedPlate,
            speedPoints, lastSpeed, maxSpeed, alarmsWithPlate,
            onSelect, fmt, logout
        };
    },
    template: `
        <div class="app">
            <header>
                <div>
                    <div class="logo">🚗 车联网云端数据平台</div>
                    <div class="subtitle">Connected Car Cloud Platform · Java Spring Boot + Vue 示例</div>
                </div>
                <button class="logout-btn" @click="logout">退出登录</button>
            </header>

            <pipeline-strip :pipeline="pipeline"></pipeline-strip>

            <div class="stats">
                <stat-card label="在线车辆"
                           :value="summary.onlineCount + ' / ' + summary.vehicleCount"
                           hint="在线 / 总数" color="#34d399"></stat-card>
                <stat-card label="遥测数据点"
                           :value="fmt(summary.telemetryCount)"
                           hint="已写入时序库" color="#22d3ee"></stat-card>
                <stat-card label="报警事件"
                           :value="fmt(summary.alarmCount)"
                           hint="流处理实时识别" color="#f87171"></stat-card>
                <stat-card label="近1分钟平均车速"
                           :value="summary.avgSpeedLastMinute + ' km/h'"
                           hint="窗口聚合结果" color="#fbbf24"></stat-card>
            </div>

            <div class="main-grid">
                <div class="card">
                    <h3>车辆列表 <span class="dim">点击查看实时曲线</span></h3>
                    <vehicle-table :vehicles="vehicles"
                                   :selected-vin="selectedVin"
                                   @select="onSelect"></vehicle-table>
                </div>

                <div class="card chart-panel">
                    <h3>实时车速曲线 <span class="dim">{{ selectedPlate }}</span></h3>
                    <div class="chart-info" v-if="telemetry.length">
                        <span>当前 <b>{{ lastSpeed }} km/h</b></span>
                        <span>最高 <b>{{ maxSpeed }} km/h</b></span>
                        <span>数据点 <b>{{ telemetry.length }}</b></span>
                    </div>
                    <sparkline v-if="telemetry.length" :points="speedPoints"></sparkline>
                    <div v-else class="empty">数据采集中，请稍候…</div>
                </div>

                <div class="card">
                    <h3>实时报警事件</h3>
                    <alarm-feed :alarms="alarmsWithPlate"></alarm-feed>
                </div>
            </div>

            <footer>
                模拟链路：车端模拟器 → MQTT 网关 → Kafka → Flink 流处理 → 时序存储 → REST API → Vue 大屏
            </footer>
        </div>`
};

createApp(App).mount('#app');
