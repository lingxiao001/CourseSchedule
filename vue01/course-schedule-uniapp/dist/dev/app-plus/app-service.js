var __defProp = Object.defineProperty;
var __defNormalProp = (obj, key, value) => key in obj ? __defProp(obj, key, { enumerable: true, configurable: true, writable: true, value }) : obj[key] = value;
var __publicField = (obj, key, value) => {
  __defNormalProp(obj, typeof key !== "symbol" ? key + "" : key, value);
  return value;
};
if (typeof Promise !== "undefined" && !Promise.prototype.finally) {
  Promise.prototype.finally = function(callback) {
    const promise = this.constructor;
    return this.then(
      (value) => promise.resolve(callback()).then(() => value),
      (reason) => promise.resolve(callback()).then(() => {
        throw reason;
      })
    );
  };
}
;
if (typeof uni !== "undefined" && uni && uni.requireGlobal) {
  const global = uni.requireGlobal();
  ArrayBuffer = global.ArrayBuffer;
  Int8Array = global.Int8Array;
  Uint8Array = global.Uint8Array;
  Uint8ClampedArray = global.Uint8ClampedArray;
  Int16Array = global.Int16Array;
  Uint16Array = global.Uint16Array;
  Int32Array = global.Int32Array;
  Uint32Array = global.Uint32Array;
  Float32Array = global.Float32Array;
  Float64Array = global.Float64Array;
  BigInt64Array = global.BigInt64Array;
  BigUint64Array = global.BigUint64Array;
}
;
if (uni.restoreGlobal) {
  uni.restoreGlobal(Vue, weex, plus, setTimeout, clearTimeout, setInterval, clearInterval);
}
(function(vue) {
  "use strict";
  const ON_SHOW = "onShow";
  const ON_HIDE = "onHide";
  const ON_LAUNCH = "onLaunch";
  function formatAppLog(type, filename, ...args) {
    if (uni.__log__) {
      uni.__log__(type, filename, ...args);
    } else {
      console[type].apply(console, [...args, filename]);
    }
  }
  const createLifeCycleHook = (lifecycle, flag = 0) => (hook, target = vue.getCurrentInstance()) => {
    !vue.isInSSRComponentSetup && vue.injectHook(lifecycle, hook, target);
  };
  const onShow = /* @__PURE__ */ createLifeCycleHook(
    ON_SHOW,
    1 | 2
    /* HookFlags.PAGE */
  );
  const onHide = /* @__PURE__ */ createLifeCycleHook(
    ON_HIDE,
    1 | 2
    /* HookFlags.PAGE */
  );
  const onLaunch = /* @__PURE__ */ createLifeCycleHook(
    ON_LAUNCH,
    1
    /* HookFlags.APP */
  );
  class ApiClient {
    constructor() {
      __publicField(this, "baseURL");
      __publicField(this, "timeout");
      __publicField(this, "defaultHeaders");
      this.baseURL = "/api";
      this.timeout = 1e4;
      this.defaultHeaders = {
        "Content-Type": "application/json"
      };
    }
    // 请求拦截器
    beforeRequest(config) {
      config.header = {
        ...config.header,
        "Content-Type": "application/json",
        "X-Requested-With": "XMLHttpRequest"
      };
      const token = uni.getStorageSync("token");
      if (token) {
        config.header["Authorization"] = `Bearer ${token}`;
      }
      formatAppLog("log", "at utils/request.ts:45", "[API请求]", config.method || "GET", config.url, config.data);
      return config;
    }
    // 响应拦截器
    afterResponse(response) {
      const { statusCode, data, header } = response;
      formatAppLog("log", "at utils/request.ts:54", "[API响应]", statusCode, data);
      if (statusCode >= 200 && statusCode < 300) {
        return { data, statusCode, header };
      } else {
        const errorData = data;
        const errorMessage = (errorData == null ? void 0 : errorData.message) || (errorData == null ? void 0 : errorData.detail) || (errorData == null ? void 0 : errorData.error) || `HTTP Error: ${statusCode}`;
        const error = new Error(errorMessage);
        error.data = errorData;
        error.statusCode = statusCode;
        throw error;
      }
    }
    // 通用请求方法
    async request(config) {
      return new Promise((resolve, reject) => {
        const processedConfig = this.beforeRequest(config);
        const fullUrl = config.url.startsWith("http") ? config.url : `${this.baseURL}${config.url}`;
        uni.request({
          url: fullUrl,
          method: processedConfig.method || "GET",
          data: processedConfig.data,
          header: {
            ...this.defaultHeaders,
            ...processedConfig.header
          },
          timeout: this.timeout,
          withCredentials: true,
          // 支持携带cookies
          success: (response) => {
            try {
              const processedResponse = this.afterResponse(response);
              resolve(processedResponse.data);
            } catch (error) {
              reject(error);
            }
          },
          fail: (error) => {
            formatAppLog("error", "at utils/request.ts:100", "[API请求失败]", error);
            reject(new Error(error.errMsg || "网络请求失败"));
          }
        });
      });
    }
    // GET请求
    async get(url, data) {
      return this.request({
        url,
        method: "GET",
        data
      });
    }
    // POST请求
    async post(url, data) {
      return this.request({
        url,
        method: "POST",
        data
      });
    }
    // PUT请求
    async put(url, data) {
      return this.request({
        url,
        method: "PUT",
        data
      });
    }
    // DELETE请求
    async delete(url, data) {
      return this.request({
        url,
        method: "DELETE",
        data
      });
    }
  }
  const apiClient = new ApiClient();
  const authApi = {
    /**
     * 用户登录
     */
    async login(credentials) {
      var _a, _b;
      try {
        const response = await apiClient.post("/auth/login", {
          username: credentials.username,
          password: credentials.password
        });
        if (!response || !response.role && !response.roleType) {
          throw new Error("无效的响应格式");
        }
        const authResponse = {
          user: {
            id: response.userId,
            username: response.username,
            realName: response.real_name || response.realName,
            role: (response.roleType || response.role || "").toLowerCase(),
            roleId: response.roleId,
            roleType: response.roleType
          },
          token: response.token,
          rawData: response
        };
        const sessionToken = `session_${Date.now()}_${response.userId}`;
        authResponse.token = sessionToken;
        uni.setStorageSync("token", sessionToken);
        uni.setStorageSync("user", authResponse.user);
        return authResponse;
      } catch (error) {
        const msg = ((_a = error.data) == null ? void 0 : _a.detail) || ((_b = error.data) == null ? void 0 : _b.message) || error.message || "登录失败";
        formatAppLog("error", "at api/auth.ts:75", "[登录失败]", msg, error);
        const enhancedError = new Error(msg);
        enhancedError.parsedMessage = msg;
        enhancedError.originalError = error;
        throw enhancedError;
      }
    },
    /**
     * 用户注册
     */
    async register(payload) {
      var _a, _b;
      try {
        const response = await apiClient.post("/auth/register", payload);
        const authResponse = {
          user: {
            id: response.userId,
            username: response.username,
            realName: response.real_name || response.realName,
            role: (response.roleType || response.role || "").toLowerCase(),
            roleId: response.roleId,
            roleType: response.roleType
          },
          token: response.token,
          rawData: response
        };
        const sessionToken = `session_${Date.now()}_${response.userId}`;
        authResponse.token = sessionToken;
        uni.setStorageSync("token", sessionToken);
        uni.setStorageSync("user", authResponse.user);
        return authResponse;
      } catch (error) {
        const msg = ((_a = error.data) == null ? void 0 : _a.detail) || ((_b = error.data) == null ? void 0 : _b.message) || error.message || "注册失败";
        formatAppLog("error", "at api/auth.ts:116", "[注册失败]", msg, error);
        const enhancedError = new Error(msg);
        enhancedError.parsedMessage = msg;
        enhancedError.originalError = error;
        throw enhancedError;
      }
    },
    /**
     * 退出登录
     */
    logout() {
      uni.removeStorageSync("token");
      uni.removeStorageSync("user");
    },
    /**
     * 获取本地存储的用户信息
     */
    getCurrentUser() {
      try {
        const user = uni.getStorageSync("user");
        return user || null;
      } catch {
        return null;
      }
    },
    /**
     * 检查是否已登录
     */
    isAuthenticated() {
      const user = this.getCurrentUser();
      const token = uni.getStorageSync("token");
      return !!(user && token);
    }
  };
  const _export_sfc = (sfc, props) => {
    const target = sfc.__vccOpts || sfc;
    for (const [key, val] of props) {
      target[key] = val;
    }
    return target;
  };
  const _sfc_main$k = {
    __name: "login",
    setup(__props, { expose: __expose }) {
      __expose();
      const isRegister = vue.ref(false);
      const loading = vue.ref(false);
      const loginFormData = vue.ref({
        username: "",
        password: ""
      });
      const registerFormData = vue.ref({
        username: "",
        password: "",
        realName: "",
        role: "",
        studentId: null,
        teacherId: null,
        grade: "",
        className: "",
        title: "",
        department: ""
      });
      const roleOptions = [
        { label: "学生", value: "student" },
        { label: "教师", value: "teacher" }
      ];
      const canLogin = vue.computed(() => {
        return loginFormData.value.username.trim() && loginFormData.value.password.trim();
      });
      const canRegister = vue.computed(() => {
        const data = registerFormData.value;
        return data.username.trim() && data.password.trim() && data.realName.trim() && data.role;
      });
      const selectedRole = vue.computed(() => {
        return roleOptions.find((item) => item.value === registerFormData.value.role) || {};
      });
      const handleLogin = async () => {
        if (loading.value || !canLogin.value)
          return;
        loading.value = true;
        try {
          formatAppLog("log", "at pages/auth/login.vue:235", "开始登录，用户名:", loginFormData.value.username);
          const authResponse = await authApi.login({
            username: loginFormData.value.username,
            password: loginFormData.value.password
          });
          formatAppLog("log", "at pages/auth/login.vue:243", "登录成功:", authResponse.user);
          uni.reLaunch({
            url: "/pages/dashboard/index"
          });
          uni.showToast({
            title: "登录成功",
            icon: "success"
          });
        } catch (error) {
          formatAppLog("error", "at pages/auth/login.vue:255", "登录失败:", error);
          const errorMessage = error.parsedMessage || error.message || "登录失败";
          uni.showToast({
            title: errorMessage,
            icon: "none",
            duration: 3e3
          });
        } finally {
          loading.value = false;
        }
      };
      const handleRegister = async () => {
        if (loading.value || !canRegister.value)
          return;
        loading.value = true;
        try {
          formatAppLog("log", "at pages/auth/login.vue:274", "开始注册，用户名:", registerFormData.value.username);
          const registerPayload = {
            username: registerFormData.value.username,
            password: registerFormData.value.password,
            realName: registerFormData.value.realName,
            role: registerFormData.value.role,
            ...registerFormData.value.role === "student" && {
              studentId: registerFormData.value.studentId ? Number(registerFormData.value.studentId) : null,
              grade: registerFormData.value.grade,
              className: registerFormData.value.className
            },
            ...registerFormData.value.role === "teacher" && {
              teacherId: registerFormData.value.teacherId ? Number(registerFormData.value.teacherId) : null,
              title: registerFormData.value.title,
              department: registerFormData.value.department
            }
          };
          const authResponse = await authApi.register(registerPayload);
          formatAppLog("log", "at pages/auth/login.vue:297", "注册成功:", authResponse.user);
          uni.reLaunch({
            url: "/pages/dashboard/index"
          });
          uni.showToast({
            title: "注册成功",
            icon: "success"
          });
        } catch (error) {
          formatAppLog("error", "at pages/auth/login.vue:309", "注册失败:", error);
          const errorMessage = error.parsedMessage || error.message || "注册失败";
          uni.showToast({
            title: errorMessage,
            icon: "none",
            duration: 3e3
          });
        } finally {
          loading.value = false;
        }
      };
      const onRoleChange = (e) => {
        const index = e.detail.value;
        const selected = roleOptions[index];
        registerFormData.value.role = selected.value;
        if (selected.value === "student") {
          registerFormData.value.teacherId = null;
          registerFormData.value.title = "";
          registerFormData.value.department = "";
        } else if (selected.value === "teacher") {
          registerFormData.value.studentId = null;
          registerFormData.value.grade = "";
          registerFormData.value.className = "";
        }
      };
      vue.onMounted(() => {
        try {
          if (authApi.isAuthenticated()) {
            formatAppLog("log", "at pages/auth/login.vue:343", "检测到已登录用户，跳转到首页");
            uni.reLaunch({
              url: "/pages/dashboard/index"
            });
            return;
          }
          formatAppLog("log", "at pages/auth/login.vue:350", "登录页面加载完成 - 实际API版本");
        } catch (error) {
          formatAppLog("error", "at pages/auth/login.vue:352", "初始化认证状态失败:", error);
        }
      });
      const __returned__ = { isRegister, loading, loginFormData, registerFormData, roleOptions, canLogin, canRegister, selectedRole, handleLogin, handleRegister, onRoleChange, ref: vue.ref, computed: vue.computed, onMounted: vue.onMounted, get authApi() {
        return authApi;
      } };
      Object.defineProperty(__returned__, "__isScriptSetup", { enumerable: false, value: true });
      return __returned__;
    }
  };
  function _sfc_render$k(_ctx, _cache, $props, $setup, $data, $options) {
    return vue.openBlock(), vue.createElementBlock("view", { class: "login-container" }, [
      vue.createCommentVNode(" 欢迎标题区域 "),
      vue.createElementVNode("view", { class: "welcome-section" }, [
        vue.createElementVNode("text", { class: "app-title" }, "Course Scheduler"),
        vue.createElementVNode("text", { class: "app-subtitle" }, "开启你的学习之旅")
      ]),
      vue.createCommentVNode(" 登录表单 "),
      !$setup.isRegister ? (vue.openBlock(), vue.createElementBlock("view", {
        key: 0,
        class: "login-form"
      }, [
        vue.createElementVNode("view", { class: "form-item" }, [
          vue.createElementVNode("text", { class: "input-icon" }, "👤"),
          vue.withDirectives(vue.createElementVNode("input", {
            "onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => $setup.loginFormData.username = $event),
            placeholder: "账号",
            class: "custom-input",
            disabled: $setup.loading
          }, null, 8, ["disabled"]), [
            [vue.vModelText, $setup.loginFormData.username]
          ])
        ]),
        vue.createElementVNode("view", { class: "form-item" }, [
          vue.createElementVNode("text", { class: "input-icon" }, "🔒"),
          vue.withDirectives(vue.createElementVNode("input", {
            "onUpdate:modelValue": _cache[1] || (_cache[1] = ($event) => $setup.loginFormData.password = $event),
            type: "password",
            placeholder: "密码",
            class: "custom-input",
            disabled: $setup.loading
          }, null, 8, ["disabled"]), [
            [vue.vModelText, $setup.loginFormData.password]
          ])
        ]),
        vue.createElementVNode("button", {
          class: "login-button",
          disabled: $setup.loading || !$setup.canLogin,
          onClick: $setup.handleLogin
        }, vue.toDisplayString($setup.loading ? "登录中..." : "立即进入"), 9, ["disabled"]),
        vue.createElementVNode("view", { class: "switch-link" }, [
          vue.createElementVNode("text", null, "还没有账号？"),
          vue.createElementVNode("text", {
            class: "link-text",
            onClick: _cache[2] || (_cache[2] = ($event) => $setup.isRegister = true)
          }, "立即注册")
        ])
      ])) : (vue.openBlock(), vue.createElementBlock(
        vue.Fragment,
        { key: 1 },
        [
          vue.createCommentVNode(" 注册表单 "),
          vue.createElementVNode("view", { class: "login-form" }, [
            vue.createElementVNode("view", { class: "form-item" }, [
              vue.createElementVNode("text", { class: "input-icon" }, "👤"),
              vue.withDirectives(vue.createElementVNode("input", {
                "onUpdate:modelValue": _cache[3] || (_cache[3] = ($event) => $setup.registerFormData.username = $event),
                placeholder: "账号",
                class: "custom-input",
                disabled: $setup.loading
              }, null, 8, ["disabled"]), [
                [vue.vModelText, $setup.registerFormData.username]
              ])
            ]),
            vue.createElementVNode("view", { class: "form-item" }, [
              vue.createElementVNode("text", { class: "input-icon" }, "🔒"),
              vue.withDirectives(vue.createElementVNode("input", {
                "onUpdate:modelValue": _cache[4] || (_cache[4] = ($event) => $setup.registerFormData.password = $event),
                type: "password",
                placeholder: "密码",
                class: "custom-input",
                disabled: $setup.loading
              }, null, 8, ["disabled"]), [
                [vue.vModelText, $setup.registerFormData.password]
              ])
            ]),
            vue.createElementVNode("view", { class: "form-item" }, [
              vue.createElementVNode("text", { class: "input-icon" }, "📝"),
              vue.withDirectives(vue.createElementVNode("input", {
                "onUpdate:modelValue": _cache[5] || (_cache[5] = ($event) => $setup.registerFormData.realName = $event),
                placeholder: "真实姓名",
                class: "custom-input",
                disabled: $setup.loading
              }, null, 8, ["disabled"]), [
                [vue.vModelText, $setup.registerFormData.realName]
              ])
            ]),
            vue.createElementVNode("view", { class: "form-item" }, [
              vue.createElementVNode("text", { class: "input-icon" }, "⭐"),
              vue.createElementVNode(
                "picker",
                {
                  mode: "selector",
                  range: $setup.roleOptions,
                  "range-key": "label",
                  onChange: $setup.onRoleChange,
                  class: "role-picker"
                },
                [
                  vue.createElementVNode(
                    "view",
                    { class: "custom-input picker-text" },
                    vue.toDisplayString($setup.selectedRole.label || "选择角色"),
                    1
                    /* TEXT */
                  )
                ],
                32
                /* NEED_HYDRATION */
              )
            ]),
            vue.createCommentVNode(" 学生专属字段 "),
            $setup.registerFormData.role === "student" ? (vue.openBlock(), vue.createElementBlock(
              vue.Fragment,
              { key: 0 },
              [
                vue.createElementVNode("view", { class: "form-item" }, [
                  vue.createElementVNode("text", { class: "input-icon" }, "🎓"),
                  vue.withDirectives(vue.createElementVNode("input", {
                    "onUpdate:modelValue": _cache[6] || (_cache[6] = ($event) => $setup.registerFormData.studentId = $event),
                    placeholder: "学号",
                    type: "number",
                    class: "custom-input",
                    disabled: $setup.loading
                  }, null, 8, ["disabled"]), [
                    [vue.vModelText, $setup.registerFormData.studentId]
                  ])
                ]),
                vue.createElementVNode("view", { class: "form-item" }, [
                  vue.createElementVNode("text", { class: "input-icon" }, "📚"),
                  vue.withDirectives(vue.createElementVNode("input", {
                    "onUpdate:modelValue": _cache[7] || (_cache[7] = ($event) => $setup.registerFormData.grade = $event),
                    placeholder: "年级",
                    class: "custom-input",
                    disabled: $setup.loading
                  }, null, 8, ["disabled"]), [
                    [vue.vModelText, $setup.registerFormData.grade]
                  ])
                ]),
                vue.createElementVNode("view", { class: "form-item" }, [
                  vue.createElementVNode("text", { class: "input-icon" }, "🏛️"),
                  vue.withDirectives(vue.createElementVNode("input", {
                    "onUpdate:modelValue": _cache[8] || (_cache[8] = ($event) => $setup.registerFormData.className = $event),
                    placeholder: "班级",
                    class: "custom-input",
                    disabled: $setup.loading
                  }, null, 8, ["disabled"]), [
                    [vue.vModelText, $setup.registerFormData.className]
                  ])
                ])
              ],
              64
              /* STABLE_FRAGMENT */
            )) : vue.createCommentVNode("v-if", true),
            vue.createCommentVNode(" 教师专属字段 "),
            $setup.registerFormData.role === "teacher" ? (vue.openBlock(), vue.createElementBlock(
              vue.Fragment,
              { key: 1 },
              [
                vue.createElementVNode("view", { class: "form-item" }, [
                  vue.createElementVNode("text", { class: "input-icon" }, "🏫"),
                  vue.withDirectives(vue.createElementVNode("input", {
                    "onUpdate:modelValue": _cache[9] || (_cache[9] = ($event) => $setup.registerFormData.teacherId = $event),
                    placeholder: "教师ID",
                    type: "number",
                    class: "custom-input",
                    disabled: $setup.loading
                  }, null, 8, ["disabled"]), [
                    [vue.vModelText, $setup.registerFormData.teacherId]
                  ])
                ]),
                vue.createElementVNode("view", { class: "form-item" }, [
                  vue.createElementVNode("text", { class: "input-icon" }, "👔"),
                  vue.withDirectives(vue.createElementVNode("input", {
                    "onUpdate:modelValue": _cache[10] || (_cache[10] = ($event) => $setup.registerFormData.title = $event),
                    placeholder: "职称",
                    class: "custom-input",
                    disabled: $setup.loading
                  }, null, 8, ["disabled"]), [
                    [vue.vModelText, $setup.registerFormData.title]
                  ])
                ]),
                vue.createElementVNode("view", { class: "form-item" }, [
                  vue.createElementVNode("text", { class: "input-icon" }, "🏢"),
                  vue.withDirectives(vue.createElementVNode("input", {
                    "onUpdate:modelValue": _cache[11] || (_cache[11] = ($event) => $setup.registerFormData.department = $event),
                    placeholder: "部门",
                    class: "custom-input",
                    disabled: $setup.loading
                  }, null, 8, ["disabled"]), [
                    [vue.vModelText, $setup.registerFormData.department]
                  ])
                ])
              ],
              64
              /* STABLE_FRAGMENT */
            )) : vue.createCommentVNode("v-if", true),
            vue.createElementVNode("button", {
              class: "login-button",
              disabled: $setup.loading || !$setup.canRegister,
              onClick: $setup.handleRegister
            }, vue.toDisplayString($setup.loading ? "注册中..." : "完成注册"), 9, ["disabled"]),
            vue.createElementVNode("view", { class: "switch-link" }, [
              vue.createElementVNode("text", null, "已有账号？"),
              vue.createElementVNode("text", {
                class: "link-text",
                onClick: _cache[12] || (_cache[12] = ($event) => $setup.isRegister = false)
              }, "立即登录")
            ])
          ])
        ],
        2112
        /* STABLE_FRAGMENT, DEV_ROOT_FRAGMENT */
      )),
      vue.createCommentVNode(" 底部链接 "),
      vue.createElementVNode("view", { class: "footer-section" }, [
        vue.createElementVNode("text", { class: "footer-link" }, "忘记密码？"),
        vue.createElementVNode("text", { class: "footer-link" }, "联系管理员")
      ])
    ]);
  }
  const PagesAuthLogin = /* @__PURE__ */ _export_sfc(_sfc_main$k, [["render", _sfc_render$k], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/auth/login.vue"]]);
  const _sfc_main$j = /* @__PURE__ */ vue.defineComponent({
    __name: "index",
    setup(__props, { expose: __expose }) {
      __expose();
      const buttonText = vue.ref("点击测试后端连接");
      const status = vue.ref("等待测试...");
      const detail = vue.ref("准备测试后端服务器连接");
      const statusColor = vue.ref("#666");
      const testConnection = async () => {
        buttonText.value = "测试中...";
        status.value = "正在连接...";
        detail.value = "尝试连接 localhost:8080";
        statusColor.value = "#ff9500";
        try {
          const result = await new Promise((resolve, reject) => {
            uni.request({
              url: "http://localhost:8080/api/auth/login",
              method: "POST",
              data: { username: "test", password: "test" },
              timeout: 5e3,
              success: (res) => resolve(res),
              fail: (err) => reject(err)
            });
          });
          status.value = "✅ 连接成功";
          detail.value = `后端服务器响应正常！
状态码: ${result.statusCode}
这证明前后端连接正常`;
          statusColor.value = "#28a745";
        } catch (error) {
          if (error.statusCode) {
            status.value = "✅ 连接成功 (有响应)";
            detail.value = `后端服务器已连接！
状态码: ${error.statusCode}
错误: ${error.errMsg || "正常，这只是业务逻辑错误"}`;
            statusColor.value = "#28a745";
          } else {
            status.value = "❌ 连接失败";
            detail.value = `无法连接到后端服务器
错误: ${error.errMsg || error.message || error}
请检查后端是否在运行`;
            statusColor.value = "#dc3545";
          }
        }
        buttonText.value = "再次测试";
      };
      const __returned__ = { buttonText, status, detail, statusColor, testConnection };
      Object.defineProperty(__returned__, "__isScriptSetup", { enumerable: false, value: true });
      return __returned__;
    }
  });
  function _sfc_render$j(_ctx, _cache, $props, $setup, $data, $options) {
    return vue.openBlock(), vue.createElementBlock("view", { class: "content" }, [
      vue.createElementVNode("view", { class: "header" }, [
        vue.createElementVNode("text", { class: "title" }, "🔧 后端连接测试")
      ]),
      vue.createElementVNode(
        "button",
        {
          class: "test-btn",
          onClick: $setup.testConnection
        },
        vue.toDisplayString($setup.buttonText),
        1
        /* TEXT */
      ),
      vue.createElementVNode("view", { class: "result" }, [
        vue.createElementVNode(
          "text",
          {
            class: "status",
            style: vue.normalizeStyle({ color: $setup.statusColor })
          },
          vue.toDisplayString($setup.status),
          5
          /* TEXT, STYLE */
        ),
        vue.createElementVNode(
          "text",
          { class: "detail" },
          vue.toDisplayString($setup.detail),
          1
          /* TEXT */
        )
      ])
    ]);
  }
  const PagesIndexIndex = /* @__PURE__ */ _export_sfc(_sfc_main$j, [["render", _sfc_render$j], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/index/index.vue"]]);
  const _sfc_main$i = {
    __name: "StudentDashboard",
    setup(__props, { expose: __expose }) {
      __expose();
      const loading = vue.ref(true);
      const selectedCourses = vue.ref([]);
      const courseSchedules = vue.ref([]);
      const currentWeek = vue.ref(1);
      const weekDays = ["周一", "周二", "周三", "周四", "周五"];
      const timeSlots = vue.ref([
        "08:00-09:30",
        "09:50-11:20",
        "13:30-15:00",
        "15:20-16:50",
        "18:30-20:00"
      ]);
      const timetable = vue.ref(Array(5).fill().map(() => Array(5).fill(null)));
      const getCourseAt = (day, timeSlot) => {
        var _a;
        return ((_a = timetable.value[day - 1]) == null ? void 0 : _a[timeSlot - 1]) || null;
      };
      const navigateTo = (url) => {
        uni.navigateTo({ url });
      };
      const loadStudentData = async () => {
        loading.value = true;
        try {
          setTimeout(() => {
            selectedCourses.value = [
              {
                teachingClassId: "CS101-001",
                courseName: "计算机科学导论",
                teacherName: "张教授",
                selectionTime: "2024-07-01"
              },
              {
                teachingClassId: "MATH201-002",
                courseName: "高等数学",
                teacherName: "李教授",
                selectionTime: "2024-07-01"
              }
            ];
            timetable.value[0][0] = { courseName: "计算机科学导论" };
            timetable.value[1][1] = { courseName: "高等数学" };
            loading.value = false;
          }, 1e3);
        } catch (error) {
          formatAppLog("error", "at pages/dashboard/components/StudentDashboard.vue:152", "加载学生数据失败:", error);
          loading.value = false;
        }
      };
      vue.onMounted(() => {
        loadStudentData();
        const now = /* @__PURE__ */ new Date();
        const startOfYear = new Date(now.getFullYear(), 0, 1);
        const weekNumber = Math.ceil(((now - startOfYear) / 864e5 + startOfYear.getDay() + 1) / 7);
        currentWeek.value = weekNumber;
      });
      const __returned__ = { loading, selectedCourses, courseSchedules, currentWeek, weekDays, timeSlots, timetable, getCourseAt, navigateTo, loadStudentData, ref: vue.ref, onMounted: vue.onMounted, get authApi() {
        return authApi;
      } };
      Object.defineProperty(__returned__, "__isScriptSetup", { enumerable: false, value: true });
      return __returned__;
    }
  };
  function _sfc_render$i(_ctx, _cache, $props, $setup, $data, $options) {
    return vue.openBlock(), vue.createElementBlock("view", { class: "student-dashboard" }, [
      vue.createElementVNode("view", { class: "dashboard-title" }, "学生个人中心"),
      vue.createCommentVNode(" 课程概览卡片 "),
      vue.createElementVNode("view", { class: "dashboard-row" }, [
        vue.createElementVNode("view", { class: "card selected-courses" }, [
          vue.createElementVNode("view", { class: "card-header" }, [
            vue.createElementVNode("text", { class: "card-title" }, "已选课程"),
            vue.createElementVNode("view", {
              class: "card-action",
              onClick: _cache[0] || (_cache[0] = ($event) => $setup.navigateTo("/pages/student/select-course"))
            }, [
              vue.createElementVNode("text", { class: "action-text" }, "前往选课")
            ])
          ]),
          $setup.loading ? (vue.openBlock(), vue.createElementBlock("view", {
            key: 0,
            class: "loading-state"
          }, [
            vue.createElementVNode("text", null, "加载中...")
          ])) : $setup.selectedCourses.length === 0 ? (vue.openBlock(), vue.createElementBlock("view", {
            key: 1,
            class: "empty-state"
          }, [
            vue.createElementVNode("text", { class: "empty-text" }, "暂无选课记录"),
            vue.createElementVNode("text", { class: "empty-hint" }, "点击右上角按钮开始选课")
          ])) : (vue.openBlock(), vue.createElementBlock("view", {
            key: 2,
            class: "course-list"
          }, [
            (vue.openBlock(true), vue.createElementBlock(
              vue.Fragment,
              null,
              vue.renderList($setup.selectedCourses.slice(0, 3), (course) => {
                return vue.openBlock(), vue.createElementBlock("view", {
                  key: course.teachingClassId,
                  class: "course-item"
                }, [
                  vue.createElementVNode(
                    "view",
                    { class: "course-name" },
                    vue.toDisplayString(course.courseName),
                    1
                    /* TEXT */
                  ),
                  vue.createElementVNode("view", { class: "course-info" }, [
                    vue.createElementVNode(
                      "text",
                      { class: "course-teacher" },
                      vue.toDisplayString(course.teacherName),
                      1
                      /* TEXT */
                    ),
                    vue.createElementVNode(
                      "text",
                      { class: "course-class" },
                      "班级: " + vue.toDisplayString(course.teachingClassId),
                      1
                      /* TEXT */
                    )
                  ])
                ]);
              }),
              128
              /* KEYED_FRAGMENT */
            )),
            $setup.selectedCourses.length > 3 ? (vue.openBlock(), vue.createElementBlock("view", {
              key: 0,
              class: "more-courses",
              onClick: _cache[1] || (_cache[1] = ($event) => $setup.navigateTo("/pages/student/my-courses"))
            }, [
              vue.createElementVNode(
                "text",
                null,
                "查看全部 " + vue.toDisplayString($setup.selectedCourses.length) + " 门课程",
                1
                /* TEXT */
              )
            ])) : vue.createCommentVNode("v-if", true)
          ]))
        ])
      ]),
      vue.createCommentVNode(" 本周课表 "),
      vue.createElementVNode("view", { class: "dashboard-row" }, [
        vue.createElementVNode("view", { class: "card schedule-card" }, [
          vue.createElementVNode("view", { class: "card-header" }, [
            vue.createElementVNode("text", { class: "card-title" }, "本周课表"),
            vue.createElementVNode(
              "text",
              { class: "current-week" },
              "第" + vue.toDisplayString($setup.currentWeek) + "周",
              1
              /* TEXT */
            )
          ]),
          vue.createElementVNode("view", { class: "timetable-container" }, [
            vue.createElementVNode("view", { class: "timetable-mini" }, [
              vue.createElementVNode("view", { class: "timetable-header" }, [
                vue.createElementVNode("view", { class: "time-col" }, "时间"),
                (vue.openBlock(), vue.createElementBlock(
                  vue.Fragment,
                  null,
                  vue.renderList($setup.weekDays, (day) => {
                    return vue.createElementVNode(
                      "view",
                      {
                        key: day,
                        class: "day-col"
                      },
                      vue.toDisplayString(day),
                      1
                      /* TEXT */
                    );
                  }),
                  64
                  /* STABLE_FRAGMENT */
                ))
              ]),
              vue.createElementVNode("view", { class: "timetable-body" }, [
                (vue.openBlock(true), vue.createElementBlock(
                  vue.Fragment,
                  null,
                  vue.renderList($setup.timeSlots.slice(0, 3), (timeSlot, index) => {
                    return vue.openBlock(), vue.createElementBlock("view", {
                      key: index,
                      class: "time-row"
                    }, [
                      vue.createElementVNode(
                        "view",
                        { class: "time-col" },
                        vue.toDisplayString(timeSlot),
                        1
                        /* TEXT */
                      ),
                      (vue.openBlock(), vue.createElementBlock(
                        vue.Fragment,
                        null,
                        vue.renderList(5, (day) => {
                          return vue.createElementVNode(
                            "view",
                            {
                              key: day,
                              class: vue.normalizeClass(["day-col", { "has-course": $setup.getCourseAt(day, index + 1) }])
                            },
                            [
                              $setup.getCourseAt(day, index + 1) ? (vue.openBlock(), vue.createElementBlock("view", {
                                key: 0,
                                class: "course-cell"
                              }, [
                                vue.createElementVNode(
                                  "text",
                                  { class: "course-name" },
                                  vue.toDisplayString($setup.getCourseAt(day, index + 1).courseName),
                                  1
                                  /* TEXT */
                                )
                              ])) : vue.createCommentVNode("v-if", true)
                            ],
                            2
                            /* CLASS */
                          );
                        }),
                        64
                        /* STABLE_FRAGMENT */
                      ))
                    ]);
                  }),
                  128
                  /* KEYED_FRAGMENT */
                ))
              ])
            ]),
            vue.createElementVNode("view", {
              class: "view-full",
              onClick: _cache[2] || (_cache[2] = ($event) => $setup.navigateTo("/pages/schedule/index"))
            }, [
              vue.createElementVNode("text", null, "查看完整课表")
            ])
          ])
        ])
      ]),
      vue.createCommentVNode(" 快捷操作 "),
      vue.createElementVNode("view", { class: "dashboard-row" }, [
        vue.createElementVNode("view", { class: "quick-actions" }, [
          vue.createElementVNode("view", {
            class: "quick-action",
            onClick: _cache[3] || (_cache[3] = ($event) => $setup.navigateTo("/pages/student/select-course"))
          }, [
            vue.createElementVNode("text", { class: "action-icon" }, "📚"),
            vue.createElementVNode("text", { class: "action-title" }, "选课")
          ]),
          vue.createElementVNode("view", {
            class: "quick-action",
            onClick: _cache[4] || (_cache[4] = ($event) => $setup.navigateTo("/pages/student/my-courses"))
          }, [
            vue.createElementVNode("text", { class: "action-icon" }, "📖"),
            vue.createElementVNode("text", { class: "action-title" }, "我的课程")
          ]),
          vue.createElementVNode("view", {
            class: "quick-action",
            onClick: _cache[5] || (_cache[5] = ($event) => $setup.navigateTo("/pages/schedule/index"))
          }, [
            vue.createElementVNode("text", { class: "action-icon" }, "📅"),
            vue.createElementVNode("text", { class: "action-title" }, "课表查看")
          ]),
          vue.createElementVNode("view", {
            class: "quick-action",
            onClick: _cache[6] || (_cache[6] = ($event) => $setup.navigateTo("/pages/profile/index"))
          }, [
            vue.createElementVNode("text", { class: "action-icon" }, "👤"),
            vue.createElementVNode("text", { class: "action-title" }, "个人信息")
          ])
        ])
      ])
    ]);
  }
  const StudentDashboard = /* @__PURE__ */ _export_sfc(_sfc_main$i, [["render", _sfc_render$i], ["__scopeId", "data-v-49dfe4bf"], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/dashboard/components/StudentDashboard.vue"]]);
  const _sfc_main$h = {
    __name: "TeacherDashboard",
    setup(__props, { expose: __expose }) {
      __expose();
      const loading = vue.ref(true);
      const recentCourses = vue.ref([]);
      const stats = vue.ref({
        totalCourses: 0,
        totalClasses: 0,
        totalStudents: 0,
        weeklyHours: 0
      });
      const navigateTo = (url) => {
        uni.navigateTo({ url });
      };
      const manageCourse = (course) => {
        formatAppLog("log", "at pages/dashboard/components/TeacherDashboard.vue:126", "管理课程:", course);
        navigateTo("/pages/teacher/courses");
      };
      const loadTeacherData = async () => {
        loading.value = true;
        try {
          setTimeout(() => {
            recentCourses.value = [
              {
                classCode: "CS101",
                name: "计算机科学导论"
              },
              {
                classCode: "CS201",
                name: "数据结构与算法"
              },
              {
                classCode: "CS301",
                name: "操作系统原理"
              }
            ];
            stats.value = {
              totalCourses: 8,
              totalClasses: 12,
              totalStudents: 180,
              weeklyHours: 16
            };
            loading.value = false;
          }, 800);
        } catch (error) {
          formatAppLog("error", "at pages/dashboard/components/TeacherDashboard.vue:161", "加载教师数据失败:", error);
          loading.value = false;
        }
      };
      vue.onMounted(() => {
        loadTeacherData();
      });
      const __returned__ = { loading, recentCourses, stats, navigateTo, manageCourse, loadTeacherData, ref: vue.ref, onMounted: vue.onMounted };
      Object.defineProperty(__returned__, "__isScriptSetup", { enumerable: false, value: true });
      return __returned__;
    }
  };
  function _sfc_render$h(_ctx, _cache, $props, $setup, $data, $options) {
    return vue.openBlock(), vue.createElementBlock("view", { class: "teacher-dashboard" }, [
      vue.createElementVNode("view", { class: "dashboard-title" }, "教师工作台"),
      vue.createCommentVNode(" 功能卡片区域 "),
      vue.createElementVNode("view", { class: "dashboard-row" }, [
        vue.createElementVNode("view", { class: "function-cards" }, [
          vue.createElementVNode("view", {
            class: "function-card",
            onClick: _cache[0] || (_cache[0] = ($event) => $setup.navigateTo("/pages/teacher/courses"))
          }, [
            vue.createElementVNode("view", { class: "card-icon" }, "📚"),
            vue.createElementVNode("view", { class: "card-content" }, [
              vue.createElementVNode("text", { class: "card-title" }, "课程管理"),
              vue.createElementVNode("text", { class: "card-desc" }, "管理您的课程信息")
            ]),
            vue.createElementVNode("view", { class: "card-arrow" }, "→")
          ]),
          vue.createElementVNode("view", {
            class: "function-card",
            onClick: _cache[1] || (_cache[1] = ($event) => $setup.navigateTo("/pages/teacher/teaching-classes"))
          }, [
            vue.createElementVNode("view", { class: "card-icon" }, "👥"),
            vue.createElementVNode("view", { class: "card-content" }, [
              vue.createElementVNode("text", { class: "card-title" }, "教学班管理"),
              vue.createElementVNode("text", { class: "card-desc" }, "管理您的教学班级")
            ]),
            vue.createElementVNode("view", { class: "card-arrow" }, "→")
          ])
        ])
      ]),
      vue.createCommentVNode(" 最近课程概览 "),
      vue.createElementVNode("view", { class: "dashboard-row" }, [
        vue.createElementVNode("view", { class: "card recent-courses" }, [
          vue.createElementVNode("view", { class: "card-header" }, [
            vue.createElementVNode("text", { class: "card-title" }, "最近课程"),
            vue.createElementVNode("view", {
              class: "card-action",
              onClick: _cache[2] || (_cache[2] = ($event) => $setup.navigateTo("/pages/teacher/courses"))
            }, [
              vue.createElementVNode("text", { class: "action-text" }, "查看全部")
            ])
          ]),
          $setup.loading ? (vue.openBlock(), vue.createElementBlock("view", {
            key: 0,
            class: "loading-state"
          }, [
            vue.createElementVNode("text", null, "加载中...")
          ])) : $setup.recentCourses.length === 0 ? (vue.openBlock(), vue.createElementBlock("view", {
            key: 1,
            class: "empty-state"
          }, [
            vue.createElementVNode("text", { class: "empty-text" }, "暂无课程数据"),
            vue.createElementVNode("text", { class: "empty-hint" }, "请先添加课程信息")
          ])) : (vue.openBlock(), vue.createElementBlock("view", {
            key: 2,
            class: "course-table"
          }, [
            vue.createElementVNode("view", { class: "table-header" }, [
              vue.createElementVNode("text", { class: "col-code" }, "课程代码"),
              vue.createElementVNode("text", { class: "col-name" }, "课程名称"),
              vue.createElementVNode("text", { class: "col-action" }, "操作")
            ]),
            (vue.openBlock(true), vue.createElementBlock(
              vue.Fragment,
              null,
              vue.renderList($setup.recentCourses.slice(0, 4), (course) => {
                return vue.openBlock(), vue.createElementBlock("view", {
                  key: course.classCode,
                  class: "table-row"
                }, [
                  vue.createElementVNode(
                    "text",
                    { class: "col-code" },
                    vue.toDisplayString(course.classCode),
                    1
                    /* TEXT */
                  ),
                  vue.createElementVNode(
                    "text",
                    { class: "col-name" },
                    vue.toDisplayString(course.name),
                    1
                    /* TEXT */
                  ),
                  vue.createElementVNode("view", { class: "col-action" }, [
                    vue.createElementVNode("text", {
                      class: "action-btn",
                      onClick: ($event) => $setup.manageCourse(course)
                    }, "管理", 8, ["onClick"])
                  ])
                ]);
              }),
              128
              /* KEYED_FRAGMENT */
            ))
          ]))
        ])
      ]),
      vue.createCommentVNode(" 教学统计 "),
      vue.createElementVNode("view", { class: "dashboard-row" }, [
        vue.createElementVNode("view", { class: "stats-grid" }, [
          vue.createElementVNode("view", { class: "stat-card" }, [
            vue.createElementVNode(
              "view",
              { class: "stat-number" },
              vue.toDisplayString($setup.stats.totalCourses),
              1
              /* TEXT */
            ),
            vue.createElementVNode("view", { class: "stat-label" }, "总课程数")
          ]),
          vue.createElementVNode("view", { class: "stat-card" }, [
            vue.createElementVNode(
              "view",
              { class: "stat-number" },
              vue.toDisplayString($setup.stats.totalClasses),
              1
              /* TEXT */
            ),
            vue.createElementVNode("view", { class: "stat-label" }, "教学班数")
          ]),
          vue.createElementVNode("view", { class: "stat-card" }, [
            vue.createElementVNode(
              "view",
              { class: "stat-number" },
              vue.toDisplayString($setup.stats.totalStudents),
              1
              /* TEXT */
            ),
            vue.createElementVNode("view", { class: "stat-label" }, "学生总数")
          ]),
          vue.createElementVNode("view", { class: "stat-card" }, [
            vue.createElementVNode(
              "view",
              { class: "stat-number" },
              vue.toDisplayString($setup.stats.weeklyHours),
              1
              /* TEXT */
            ),
            vue.createElementVNode("view", { class: "stat-label" }, "周课时数")
          ])
        ])
      ]),
      vue.createCommentVNode(" 快捷操作 "),
      vue.createElementVNode("view", { class: "dashboard-row" }, [
        vue.createElementVNode("view", { class: "quick-actions" }, [
          vue.createElementVNode("view", {
            class: "quick-action",
            onClick: _cache[3] || (_cache[3] = ($event) => $setup.navigateTo("/pages/teacher/courses"))
          }, [
            vue.createElementVNode("text", { class: "action-icon" }, "📖"),
            vue.createElementVNode("text", { class: "action-title" }, "课程管理")
          ]),
          vue.createElementVNode("view", {
            class: "quick-action",
            onClick: _cache[4] || (_cache[4] = ($event) => $setup.navigateTo("/pages/teacher/teaching-classes"))
          }, [
            vue.createElementVNode("text", { class: "action-icon" }, "👨‍🏫"),
            vue.createElementVNode("text", { class: "action-title" }, "教学班")
          ]),
          vue.createElementVNode("view", {
            class: "quick-action",
            onClick: _cache[5] || (_cache[5] = ($event) => $setup.navigateTo("/pages/schedule/index"))
          }, [
            vue.createElementVNode("text", { class: "action-icon" }, "📅"),
            vue.createElementVNode("text", { class: "action-title" }, "课表查看")
          ]),
          vue.createElementVNode("view", {
            class: "quick-action",
            onClick: _cache[6] || (_cache[6] = ($event) => $setup.navigateTo("/pages/profile/index"))
          }, [
            vue.createElementVNode("text", { class: "action-icon" }, "👤"),
            vue.createElementVNode("text", { class: "action-title" }, "个人信息")
          ])
        ])
      ])
    ]);
  }
  const TeacherDashboard = /* @__PURE__ */ _export_sfc(_sfc_main$h, [["render", _sfc_render$h], ["__scopeId", "data-v-ae6839df"], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/dashboard/components/TeacherDashboard.vue"]]);
  const adminApi = {
    // ========== 用户管理 ==========
    /**
     * 获取所有用户（分页）
     */
    async getUsers(params = {}) {
      return apiClient.get("/admin/users", params);
    },
    /**
     * 获取单个用户
     */
    async getUser(userId) {
      return apiClient.get(`/admin/users/${userId}`);
    },
    /**
     * 创建用户
     */
    async createUser(user) {
      return apiClient.post("/admin/users", user);
    },
    /**
     * 更新用户信息
     */
    async updateUser(id, user) {
      return apiClient.put(`/admin/users/${id}`, user);
    },
    /**
     * 删除用户
     */
    async deleteUser(id) {
      return apiClient.delete(`/admin/users/${id}`);
    },
    // ========== 课程管理 ==========
    /**
     * 获取所有课程
     */
    async getCourses() {
      return apiClient.get("/courses");
    },
    /**
     * 获取单个课程
     */
    async getCourse(courseId) {
      return apiClient.get(`/courses/${courseId}`);
    },
    /**
     * 创建课程
     */
    async createCourse(course) {
      return apiClient.post("/courses", course);
    },
    /**
     * 更新课程信息
     */
    async updateCourse(id, course) {
      return apiClient.put(`/courses/${id}`, course);
    },
    /**
     * 删除课程
     */
    async deleteCourse(id) {
      return apiClient.delete(`/courses/${id}`);
    },
    // ========== 教学班管理 ==========
    /**
     * 获取所有教学班
     */
    async getTeachingClasses() {
      return apiClient.get("/courses/classes");
    },
    /**
     * 获取指定课程的教学班
     */
    async getTeachingClassesByCourse(courseId) {
      return apiClient.get(`/courses/${courseId}/classes`);
    },
    /**
     * 创建教学班
     */
    async createTeachingClass(courseId, teachingClass) {
      return apiClient.post(`/courses/${courseId}/classes`, teachingClass);
    },
    /**
     * 更新教学班信息
     */
    async updateTeachingClass(classId, teachingClass) {
      return apiClient.put(`/courses/classes/${classId}`, teachingClass);
    },
    /**
     * 删除教学班
     */
    async deleteTeachingClass(classId) {
      return apiClient.delete(`/courses/classes/${classId}`);
    },
    // ========== 教室管理 ==========
    /**
     * 获取所有教室
     */
    async getClassrooms() {
      return apiClient.get("/classrooms");
    },
    /**
     * 创建教室
     */
    async createClassroom(classroom) {
      return apiClient.post("/classrooms", classroom);
    },
    /**
     * 更新教室信息
     */
    async updateClassroom(id, classroom) {
      return apiClient.put(`/classrooms/${id}`, classroom);
    },
    /**
     * 删除教室
     */
    async deleteClassroom(id) {
      return apiClient.delete(`/classrooms/${id}`);
    },
    /**
     * 根据时间段获取可用教室
     */
    async getAvailableClassrooms(params) {
      return apiClient.get("/classrooms/available", params);
    },
    /**
     * 根据教学楼获取教室
     */
    async getClassroomsByBuilding(building) {
      return apiClient.get(`/classrooms/building/${building}`);
    },
    // ========== 自动排课 ==========
    /**
     * 启动自动排课
     */
    async startAutoSchedule(config) {
      return apiClient.post("/admin/auto-schedule", config);
    },
    /**
     * 获取排课结果
     */
    async getScheduleResult(scheduleId) {
      return apiClient.get(`/admin/schedule-result/${scheduleId}`);
    },
    /**
     * 获取最近的排课记录
     */
    async getRecentSchedules() {
      return apiClient.get("/admin/recent-schedules");
    },
    // ========== 数据统计 ==========
    /**
     * 获取系统统计数据
     */
    async getStats() {
      return apiClient.get("/admin/stats");
    },
    /**
     * 获取课程统计图表数据
     */
    async getCourseStats() {
      return apiClient.get("/admin/stats/courses");
    },
    /**
     * 获取用户注册趋势
     */
    async getRegistrationTrends() {
      return apiClient.get("/admin/stats/registrations");
    },
    // ========== 系统配置 ==========
    /**
     * 获取排课配置
     */
    async getScheduleConfig() {
      return apiClient.get("/admin/schedule-config");
    },
    /**
     * 更新排课配置
     */
    async updateScheduleConfig(config) {
      return apiClient.put("/admin/schedule-config", config);
    },
    // ========== 课程安排管理 ==========
    /**
     * 获取指定教学班的课程安排
     */
    async getSchedulesByTeachingClass(teachingClassId, params) {
      return apiClient.get(`/schedules/teaching-class/${teachingClassId}`, params);
    },
    /**
     * 获取所有课程安排
     */
    async getAllSchedules(params) {
      return apiClient.get("/schedules", params);
    },
    /**
     * 获取单个课程安排
     */
    async getSchedule(scheduleId) {
      return apiClient.get(`/schedules/${scheduleId}`);
    },
    /**
     * 为教学班添加课程安排
     */
    async addSchedule(teachingClassId, schedule) {
      return apiClient.post(`/schedules/teaching-class/${teachingClassId}`, schedule);
    },
    /**
     * 更新课程安排
     */
    async updateSchedule(scheduleId, schedule) {
      return apiClient.put(`/schedules/${scheduleId}`, schedule);
    },
    /**
     * 删除课程安排
     */
    async deleteSchedule(scheduleId) {
      return apiClient.delete(`/schedules/${scheduleId}`);
    },
    /**
     * 批量添加课程安排
     */
    async batchAddSchedules(teachingClassId, schedules) {
      return apiClient.post(`/schedules/teaching-class/${teachingClassId}/batch`, { schedules });
    },
    /**
     * 检查课程安排冲突
     */
    async checkScheduleConflict(schedule) {
      return apiClient.post("/schedules/check-conflict", schedule);
    },
    /**
     * 获取教室在指定时间的可用性
     */
    async getClassroomAvailability(classroomId, params) {
      return apiClient.get(`/classrooms/${classroomId}/availability`, params);
    }
  };
  const _sfc_main$g = {
    __name: "AdminDashboard",
    setup(__props, { expose: __expose }) {
      __expose();
      const loadingActivities = vue.ref(true);
      const systemStats = vue.ref({
        totalUsers: 0,
        totalCourses: 0,
        studentCount: 0,
        teacherCount: 0,
        adminCount: 0
      });
      const recentActivities = vue.ref([]);
      const navigateTo = (url) => {
        uni.navigateTo({ url });
      };
      const viewAllActivities = () => {
        uni.showToast({
          title: "功能开发中",
          icon: "none"
        });
      };
      const getActivityIcon = (type) => {
        const icons = {
          user: "👤",
          course: "📚",
          schedule: "📅",
          system: "⚙️"
        };
        return icons[type] || "📝";
      };
      const loadAdminData = async () => {
        loadingActivities.value = true;
        try {
          await Promise.all([
            fetchUserStats(),
            fetchCourseStats()
          ]);
          recentActivities.value = [
            {
              id: 1,
              type: "user",
              title: "新用户注册",
              time: "2小时前"
            },
            {
              id: 2,
              type: "course",
              title: "课程信息更新",
              time: "4小时前"
            },
            {
              id: 3,
              type: "schedule",
              title: "排课任务执行",
              time: "6小时前"
            },
            {
              id: 4,
              type: "user",
              title: "用户信息修改",
              time: "8小时前"
            },
            {
              id: 5,
              type: "system",
              title: "系统维护完成",
              time: "12小时前"
            }
          ];
        } catch (error) {
          formatAppLog("error", "at pages/dashboard/components/AdminDashboard.vue:210", "加载管理员数据失败:", error);
          uni.showToast({
            title: "加载数据失败",
            icon: "error"
          });
        } finally {
          loadingActivities.value = false;
        }
      };
      const fetchUserStats = async () => {
        try {
          let allUsers = [];
          let page = 0;
          let hasMore = true;
          while (hasMore) {
            const response = await adminApi.getUsers({ page, size: 100 });
            allUsers = allUsers.concat(response.content);
            page++;
            hasMore = response.content.length === 100 && page < response.totalPages;
          }
          const studentCount = allUsers.filter((user) => user.role === "student").length;
          const teacherCount = allUsers.filter((user) => user.role === "teacher").length;
          const adminCount = allUsers.filter((user) => user.role === "admin").length;
          systemStats.value.totalUsers = allUsers.length;
          systemStats.value.studentCount = studentCount;
          systemStats.value.teacherCount = teacherCount;
          systemStats.value.adminCount = adminCount;
        } catch (error) {
          formatAppLog("error", "at pages/dashboard/components/AdminDashboard.vue:246", "获取用户统计失败:", error);
          try {
            const response = await adminApi.getUsers({ page: 0, size: 100 });
            systemStats.value.totalUsers = response.totalElements || response.content.length;
            systemStats.value.studentCount = response.content.filter((user) => user.role === "student").length;
            systemStats.value.teacherCount = response.content.filter((user) => user.role === "teacher").length;
            systemStats.value.adminCount = response.content.filter((user) => user.role === "admin").length;
          } catch (fallbackError) {
            formatAppLog("error", "at pages/dashboard/components/AdminDashboard.vue:255", "获取用户统计完全失败:", fallbackError);
          }
        }
      };
      const fetchCourseStats = async () => {
        try {
          const courses = await adminApi.getCourses();
          systemStats.value.totalCourses = courses.length;
        } catch (error) {
          formatAppLog("error", "at pages/dashboard/components/AdminDashboard.vue:266", "获取课程统计失败:", error);
          systemStats.value.totalCourses = 0;
        }
      };
      vue.onMounted(() => {
        loadAdminData();
      });
      const __returned__ = { loadingActivities, systemStats, recentActivities, navigateTo, viewAllActivities, getActivityIcon, loadAdminData, fetchUserStats, fetchCourseStats, ref: vue.ref, onMounted: vue.onMounted, get adminApi() {
        return adminApi;
      } };
      Object.defineProperty(__returned__, "__isScriptSetup", { enumerable: false, value: true });
      return __returned__;
    }
  };
  function _sfc_render$g(_ctx, _cache, $props, $setup, $data, $options) {
    return vue.openBlock(), vue.createElementBlock("view", { class: "admin-dashboard" }, [
      vue.createElementVNode("view", { class: "dashboard-title" }, "管理员后台"),
      vue.createCommentVNode(" 系统概览 "),
      vue.createElementVNode("view", { class: "dashboard-row" }, [
        vue.createElementVNode("view", { class: "overview-stats" }, [
          vue.createElementVNode("view", { class: "overview-card users" }, [
            vue.createElementVNode("view", { class: "stat-icon" }, "👥"),
            vue.createElementVNode("view", { class: "stat-content" }, [
              vue.createElementVNode(
                "text",
                { class: "stat-number" },
                vue.toDisplayString($setup.systemStats.totalUsers),
                1
                /* TEXT */
              ),
              vue.createElementVNode("text", { class: "stat-label" }, "系统用户")
            ])
          ]),
          vue.createElementVNode("view", { class: "overview-card courses" }, [
            vue.createElementVNode("view", { class: "stat-icon" }, "📚"),
            vue.createElementVNode("view", { class: "stat-content" }, [
              vue.createElementVNode(
                "text",
                { class: "stat-number" },
                vue.toDisplayString($setup.systemStats.totalCourses),
                1
                /* TEXT */
              ),
              vue.createElementVNode("text", { class: "stat-label" }, "总课程数")
            ])
          ])
        ])
      ]),
      vue.createCommentVNode(" 用户统计 "),
      vue.createElementVNode("view", { class: "dashboard-row" }, [
        vue.createElementVNode("view", { class: "card user-stats" }, [
          vue.createElementVNode("view", { class: "card-header" }, [
            vue.createElementVNode("text", { class: "card-title" }, "用户统计")
          ]),
          vue.createElementVNode("view", { class: "user-breakdown" }, [
            vue.createElementVNode("view", { class: "user-type" }, [
              vue.createElementVNode("view", { class: "type-indicator student" }),
              vue.createElementVNode("text", { class: "type-label" }, "学生"),
              vue.createElementVNode(
                "text",
                { class: "type-count" },
                vue.toDisplayString($setup.systemStats.studentCount),
                1
                /* TEXT */
              )
            ]),
            vue.createElementVNode("view", { class: "user-type" }, [
              vue.createElementVNode("view", { class: "type-indicator teacher" }),
              vue.createElementVNode("text", { class: "type-label" }, "教师"),
              vue.createElementVNode(
                "text",
                { class: "type-count" },
                vue.toDisplayString($setup.systemStats.teacherCount),
                1
                /* TEXT */
              )
            ]),
            vue.createElementVNode("view", { class: "user-type" }, [
              vue.createElementVNode("view", { class: "type-indicator admin" }),
              vue.createElementVNode("text", { class: "type-label" }, "管理员"),
              vue.createElementVNode(
                "text",
                { class: "type-count" },
                vue.toDisplayString($setup.systemStats.adminCount),
                1
                /* TEXT */
              )
            ])
          ])
        ])
      ]),
      vue.createCommentVNode(" 管理功能 "),
      vue.createElementVNode("view", { class: "dashboard-row" }, [
        vue.createElementVNode("view", { class: "management-grid" }, [
          vue.createElementVNode("view", {
            class: "mgmt-card",
            onClick: _cache[0] || (_cache[0] = ($event) => $setup.navigateTo("/pages/admin/users"))
          }, [
            vue.createElementVNode("view", { class: "mgmt-icon" }, "👤"),
            vue.createElementVNode("text", { class: "mgmt-title" }, "用户管理"),
            vue.createElementVNode("text", { class: "mgmt-desc" }, "管理系统用户")
          ]),
          vue.createElementVNode("view", {
            class: "mgmt-card",
            onClick: _cache[1] || (_cache[1] = ($event) => $setup.navigateTo("/pages/admin/courses"))
          }, [
            vue.createElementVNode("view", { class: "mgmt-icon" }, "📖"),
            vue.createElementVNode("text", { class: "mgmt-title" }, "课程管理"),
            vue.createElementVNode("text", { class: "mgmt-desc" }, "管理课程信息")
          ]),
          vue.createElementVNode("view", {
            class: "mgmt-card",
            onClick: _cache[2] || (_cache[2] = ($event) => $setup.navigateTo("/pages/admin/manual-schedule"))
          }, [
            vue.createElementVNode("view", { class: "mgmt-icon" }, "📅"),
            vue.createElementVNode("text", { class: "mgmt-title" }, "手动排课"),
            vue.createElementVNode("text", { class: "mgmt-desc" }, "手动编排课程表")
          ]),
          vue.createElementVNode("view", {
            class: "mgmt-card",
            onClick: _cache[3] || (_cache[3] = ($event) => $setup.navigateTo("/pages/admin/auto-schedule"))
          }, [
            vue.createElementVNode("view", { class: "mgmt-icon" }, "🤖"),
            vue.createElementVNode("text", { class: "mgmt-title" }, "自动排课"),
            vue.createElementVNode("text", { class: "mgmt-desc" }, "智能课程安排")
          ]),
          vue.createElementVNode("view", {
            class: "mgmt-card",
            onClick: _cache[4] || (_cache[4] = ($event) => $setup.navigateTo("/pages/admin/stats"))
          }, [
            vue.createElementVNode("view", { class: "mgmt-icon" }, "📊"),
            vue.createElementVNode("text", { class: "mgmt-title" }, "数据统计"),
            vue.createElementVNode("text", { class: "mgmt-desc" }, "系统数据分析")
          ])
        ])
      ]),
      vue.createCommentVNode(" 最近活动 "),
      vue.createElementVNode("view", { class: "dashboard-row" }, [
        vue.createElementVNode("view", { class: "card recent-activity" }, [
          vue.createElementVNode("view", { class: "card-header" }, [
            vue.createElementVNode("text", { class: "card-title" }, "最近活动"),
            vue.createElementVNode("view", {
              class: "card-action",
              onClick: $setup.viewAllActivities
            }, [
              vue.createElementVNode("text", { class: "action-text" }, "查看全部")
            ])
          ]),
          $setup.loadingActivities ? (vue.openBlock(), vue.createElementBlock("view", {
            key: 0,
            class: "loading-state"
          }, [
            vue.createElementVNode("text", null, "加载中...")
          ])) : $setup.recentActivities.length === 0 ? (vue.openBlock(), vue.createElementBlock("view", {
            key: 1,
            class: "empty-state"
          }, [
            vue.createElementVNode("text", { class: "empty-text" }, "暂无活动记录")
          ])) : (vue.openBlock(), vue.createElementBlock("view", {
            key: 2,
            class: "activity-list"
          }, [
            (vue.openBlock(true), vue.createElementBlock(
              vue.Fragment,
              null,
              vue.renderList($setup.recentActivities.slice(0, 5), (activity) => {
                return vue.openBlock(), vue.createElementBlock("view", {
                  key: activity.id,
                  class: "activity-item"
                }, [
                  vue.createElementVNode(
                    "view",
                    {
                      class: vue.normalizeClass(["activity-icon", activity.type])
                    },
                    vue.toDisplayString($setup.getActivityIcon(activity.type)),
                    3
                    /* TEXT, CLASS */
                  ),
                  vue.createElementVNode("view", { class: "activity-content" }, [
                    vue.createElementVNode(
                      "text",
                      { class: "activity-title" },
                      vue.toDisplayString(activity.title),
                      1
                      /* TEXT */
                    ),
                    vue.createElementVNode(
                      "text",
                      { class: "activity-time" },
                      vue.toDisplayString(activity.time),
                      1
                      /* TEXT */
                    )
                  ])
                ]);
              }),
              128
              /* KEYED_FRAGMENT */
            ))
          ]))
        ])
      ]),
      vue.createCommentVNode(" 系统状态 "),
      vue.createElementVNode("view", { class: "dashboard-row" }, [
        vue.createElementVNode("view", { class: "system-status" }, [
          vue.createElementVNode("view", { class: "status-item" }, [
            vue.createElementVNode("view", { class: "status-dot active" }),
            vue.createElementVNode("text", { class: "status-text" }, "系统运行正常")
          ]),
          vue.createElementVNode("view", { class: "status-item" }, [
            vue.createElementVNode("view", { class: "status-dot active" }),
            vue.createElementVNode("text", { class: "status-text" }, "数据库连接正常")
          ])
        ])
      ])
    ]);
  }
  const AdminDashboard = /* @__PURE__ */ _export_sfc(_sfc_main$g, [["render", _sfc_render$g], ["__scopeId", "data-v-c7373bc3"], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/dashboard/components/AdminDashboard.vue"]]);
  const _sfc_main$f = {
    __name: "index",
    setup(__props, { expose: __expose }) {
      __expose();
      const currentTab = vue.ref("dashboard");
      const userInfo = vue.computed(() => {
        const user = authApi.getCurrentUser();
        return user || { realName: "用户", role: "student" };
      });
      const roleText = vue.computed(() => {
        const roleMap = {
          student: "学生",
          teacher: "教师",
          admin: "管理员"
        };
        return roleMap[userInfo.value.role] || "用户";
      });
      const avatarColor = vue.computed(() => {
        var _a;
        const colors = ["#FF7875", "#FFC069", "#95DE64", "#597EF7", "#AD6800"];
        const charCodeSum = ((_a = userInfo.value.realName) == null ? void 0 : _a.charCodeAt(0)) || 0;
        return colors[charCodeSum % colors.length];
      });
      const switchTab = (tab) => {
        currentTab.value = tab;
        switch (tab) {
          case "dashboard":
            break;
          case "schedule":
            uni.navigateTo({ url: "/pages/schedule/index" });
            break;
          case "profile":
            uni.navigateTo({ url: "/pages/profile/index" });
            break;
        }
      };
      vue.onMounted(() => {
        if (!authApi.isAuthenticated()) {
          uni.reLaunch({
            url: "/pages/auth/login"
          });
          return;
        }
        formatAppLog("log", "at pages/dashboard/index.vue:100", "用户信息:", userInfo.value);
      });
      const __returned__ = { currentTab, userInfo, roleText, avatarColor, switchTab, ref: vue.ref, computed: vue.computed, onMounted: vue.onMounted, get authApi() {
        return authApi;
      }, StudentDashboard, TeacherDashboard, AdminDashboard };
      Object.defineProperty(__returned__, "__isScriptSetup", { enumerable: false, value: true });
      return __returned__;
    }
  };
  function _sfc_render$f(_ctx, _cache, $props, $setup, $data, $options) {
    var _a;
    return vue.openBlock(), vue.createElementBlock("view", { class: "mobile-dashboard" }, [
      vue.createCommentVNode(" 顶部用户信息栏 "),
      vue.createElementVNode("view", { class: "dashboard-header" }, [
        vue.createElementVNode("view", { class: "user-info" }, [
          vue.createElementVNode(
            "view",
            {
              class: "user-avatar",
              style: vue.normalizeStyle({ backgroundColor: $setup.avatarColor })
            },
            vue.toDisplayString((_a = $setup.userInfo.realName) == null ? void 0 : _a.charAt(0)),
            5
            /* TEXT, STYLE */
          ),
          vue.createElementVNode("view", { class: "user-details" }, [
            vue.createElementVNode("text", { class: "welcome-text" }, "欢迎回来,"),
            vue.createElementVNode(
              "text",
              { class: "user-name" },
              vue.toDisplayString($setup.userInfo.realName),
              1
              /* TEXT */
            ),
            vue.createElementVNode(
              "text",
              { class: "user-role" },
              vue.toDisplayString($setup.roleText),
              1
              /* TEXT */
            )
          ])
        ])
      ]),
      vue.createCommentVNode(" 主内容区：根据角色动态渲染 "),
      vue.createElementVNode("view", { class: "dashboard-content" }, [
        $setup.userInfo.role === "student" ? (vue.openBlock(), vue.createBlock($setup["StudentDashboard"], { key: 0 })) : $setup.userInfo.role === "teacher" ? (vue.openBlock(), vue.createBlock($setup["TeacherDashboard"], { key: 1 })) : $setup.userInfo.role === "admin" ? (vue.openBlock(), vue.createBlock($setup["AdminDashboard"], { key: 2 })) : vue.createCommentVNode("v-if", true)
      ]),
      vue.createCommentVNode(" 底部导航栏 "),
      vue.createElementVNode("view", { class: "bottom-nav" }, [
        vue.createElementVNode(
          "view",
          {
            class: vue.normalizeClass(["nav-item", { active: $setup.currentTab === "dashboard" }]),
            onClick: _cache[0] || (_cache[0] = ($event) => $setup.switchTab("dashboard"))
          },
          [
            vue.createElementVNode("text", { class: "nav-icon" }, "🏠"),
            vue.createElementVNode("text", { class: "nav-text" }, "首页")
          ],
          2
          /* CLASS */
        ),
        $setup.userInfo.role !== "admin" ? (vue.openBlock(), vue.createElementBlock(
          "view",
          {
            key: 0,
            class: vue.normalizeClass(["nav-item", { active: $setup.currentTab === "schedule" }]),
            onClick: _cache[1] || (_cache[1] = ($event) => $setup.switchTab("schedule"))
          },
          [
            vue.createElementVNode("text", { class: "nav-icon" }, "📅"),
            vue.createElementVNode("text", { class: "nav-text" }, "课表")
          ],
          2
          /* CLASS */
        )) : vue.createCommentVNode("v-if", true),
        vue.createElementVNode(
          "view",
          {
            class: vue.normalizeClass(["nav-item", { active: $setup.currentTab === "profile" }]),
            onClick: _cache[2] || (_cache[2] = ($event) => $setup.switchTab("profile"))
          },
          [
            vue.createElementVNode("text", { class: "nav-icon" }, "👤"),
            vue.createElementVNode("text", { class: "nav-text" }, "我的")
          ],
          2
          /* CLASS */
        )
      ])
    ]);
  }
  const PagesDashboardIndex = /* @__PURE__ */ _export_sfc(_sfc_main$f, [["render", _sfc_render$f], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/dashboard/index.vue"]]);
  const _sfc_main$e = {
    __name: "api",
    setup(__props, { expose: __expose }) {
      __expose();
      const loading = vue.ref(false);
      const resultText = vue.ref("等待测试...");
      const isSuccess = vue.ref(false);
      const isError = vue.ref(false);
      const testConnection = async () => {
        loading.value = true;
        isSuccess.value = false;
        isError.value = false;
        resultText.value = "正在连接后端...";
        try {
          const response = await apiClient.get("/test");
          isSuccess.value = true;
          resultText.value = `✅ 连接成功！
服务器响应: ${JSON.stringify(response, null, 2)}`;
          uni.showToast({
            title: "连接成功",
            icon: "success"
          });
        } catch (error) {
          isError.value = true;
          resultText.value = `❌ 连接失败
错误信息: ${error.message}
错误详情: ${JSON.stringify(error, null, 2)}`;
          uni.showToast({
            title: "连接失败",
            icon: "none",
            duration: 3e3
          });
          formatAppLog("error", "at pages/test/api.vue:67", "API连接测试失败:", error);
        } finally {
          loading.value = false;
        }
      };
      const __returned__ = { loading, resultText, isSuccess, isError, testConnection, ref: vue.ref, get apiClient() {
        return apiClient;
      } };
      Object.defineProperty(__returned__, "__isScriptSetup", { enumerable: false, value: true });
      return __returned__;
    }
  };
  function _sfc_render$e(_ctx, _cache, $props, $setup, $data, $options) {
    return vue.openBlock(), vue.createElementBlock("view", { class: "test-container" }, [
      vue.createElementVNode("view", { class: "header" }, [
        vue.createElementVNode("text", { class: "title" }, "API连接测试")
      ]),
      vue.createElementVNode("view", { class: "test-section" }, [
        vue.createElementVNode("button", {
          class: "test-button",
          onClick: $setup.testConnection,
          disabled: $setup.loading
        }, vue.toDisplayString($setup.loading ? "测试中..." : "测试后端连接"), 9, ["disabled"])
      ]),
      vue.createElementVNode("view", { class: "result-section" }, [
        vue.createElementVNode("view", { class: "result-title" }, "测试结果："),
        vue.createElementVNode(
          "view",
          {
            class: vue.normalizeClass(["result-content", { success: $setup.isSuccess, error: $setup.isError }])
          },
          vue.toDisplayString($setup.resultText),
          3
          /* TEXT, CLASS */
        )
      ]),
      vue.createElementVNode("view", { class: "info-section" }, [
        vue.createElementVNode("text", { class: "info-text" }, "点击按钮测试是否能连接到后端服务器 (localhost:8080)")
      ])
    ]);
  }
  const PagesTestApi = /* @__PURE__ */ _export_sfc(_sfc_main$e, [["render", _sfc_render$e], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/test/api.vue"]]);
  const _sfc_main$d = {
    data() {
      return {
        status: "未测试",
        result: "点击按钮开始测试...",
        statusColor: "#666"
      };
    },
    methods: {
      async testAPI() {
        this.status = "测试中...";
        this.result = "正在尝试连接后端服务器...";
        this.statusColor = "#ff9500";
        try {
          const response = await this.directTest();
          this.status = "连接成功 ✅";
          this.result = `成功连接到后端服务器！

响应数据:
${JSON.stringify(response, null, 2)}`;
          this.statusColor = "#28a745";
        } catch (error1) {
          try {
            const healthCheck = await this.healthCheck();
            this.status = "部分成功 ⚠️";
            this.result = `基础连接正常，但API可能有问题:

${healthCheck}`;
            this.statusColor = "#ffc107";
          } catch (error2) {
            this.status = "连接失败 ❌";
            this.result = `连接失败，错误详情:

错误1: ${error1.message || error1}

错误2: ${error2.message || error2}

可能原因:
1. 后端服务器未启动
2. 端口8080被占用
3. 网络连接问题
4. CORS跨域限制`;
            this.statusColor = "#dc3545";
          }
        }
      },
      // 直接使用uni.request测试
      directTest() {
        return new Promise((resolve, reject) => {
          uni.request({
            url: "/api/test",
            method: "GET",
            timeout: 5e3,
            success: (res) => {
              resolve(res.data);
            },
            fail: (err) => {
              reject(err);
            }
          });
        });
      },
      // 健康检查
      healthCheck() {
        return new Promise((resolve, reject) => {
          uni.request({
            url: "/api/health",
            method: "GET",
            timeout: 3e3,
            success: (res) => {
              resolve(`服务器响应状态: ${res.statusCode}
响应头: ${JSON.stringify(res.header, null, 2)}`);
            },
            fail: (err) => {
              reject(err);
            }
          });
        });
      }
    },
    // 页面加载时显示基本信息
    onLoad() {
      formatAppLog("log", "at pages/test/simple.vue:112", "测试页面加载成功");
      this.result = `页面加载成功！

当前时间: ${(/* @__PURE__ */ new Date()).toLocaleString()}
前端地址: ${window.location.href}
后端地址: http://localhost:8080

点击按钮开始测试连接...`;
    }
  };
  function _sfc_render$d(_ctx, _cache, $props, $setup, $data, $options) {
    return vue.openBlock(), vue.createElementBlock("view", { style: { "padding": "20px", "background": "#f0f8ff", "min-height": "100vh" } }, [
      vue.createElementVNode("text", { style: { "font-size": "24px", "color": "#333" } }, "🔧 后端连接测试"),
      vue.createElementVNode("view", { style: { "margin": "20px 0" } }, [
        vue.createElementVNode("button", {
          style: { "background": "#007aff", "color": "white", "padding": "15px 30px", "border": "none", "border-radius": "8px", "font-size": "16px" },
          onClick: _cache[0] || (_cache[0] = (...args) => $options.testAPI && $options.testAPI(...args))
        }, " 点击测试后端连接 ")
      ]),
      vue.createElementVNode("view", { style: { "background": "white", "padding": "15px", "border-radius": "8px", "margin": "20px 0" } }, [
        vue.createElementVNode("text", { style: { "font-weight": "bold", "color": "#333" } }, "测试状态: "),
        vue.createElementVNode(
          "text",
          {
            style: vue.normalizeStyle({ color: $data.statusColor })
          },
          vue.toDisplayString($data.status),
          5
          /* TEXT, STYLE */
        )
      ]),
      vue.createElementVNode("view", { style: { "background": "white", "padding": "15px", "border-radius": "8px", "margin": "20px 0" } }, [
        vue.createElementVNode("text", { style: { "font-weight": "bold", "color": "#333", "display": "block", "margin-bottom": "10px" } }, "详细结果:"),
        vue.createElementVNode(
          "text",
          { style: { "font-size": "14px", "color": "#666", "white-space": "pre-wrap" } },
          vue.toDisplayString($data.result),
          1
          /* TEXT */
        )
      ]),
      vue.createElementVNode("view", { style: { "background": "#fff3cd", "padding": "15px", "border-radius": "8px", "margin": "20px 0" } }, [
        vue.createElementVNode("text", { style: { "font-size": "14px", "color": "#856404" } }, " 📝 说明： • 后端服务器: localhost:8080 (通过代理访问) • 前端服务器: localhost:5173 • 如果显示CORS错误，说明连接正常，只是跨域配置问题 • 如果显示404错误，说明连接正常，只是接口不存在 ")
      ])
    ]);
  }
  const PagesTestSimple = /* @__PURE__ */ _export_sfc(_sfc_main$d, [["render", _sfc_render$d], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/test/simple.vue"]]);
  const _sfc_main$c = {
    data() {
      return {
        searchText: "",
        courses: [
          { id: 1, name: "高等数学", teacher: "张教授", time: "周一 8:00-10:00" },
          { id: 2, name: "大学英语", teacher: "李老师", time: "周二 10:00-12:00" }
        ]
      };
    },
    computed: {
      filteredCourses() {
        return this.courses.filter(
          (course) => course.name.includes(this.searchText)
        );
      }
    },
    methods: {
      selectCourse(course) {
        formatAppLog("log", "at pages/student/select-course.vue:39", "选课:", course);
      }
    }
  };
  function _sfc_render$c(_ctx, _cache, $props, $setup, $data, $options) {
    const _component_u_input = vue.resolveComponent("u-input");
    const _component_u_button = vue.resolveComponent("u-button");
    return vue.openBlock(), vue.createElementBlock("view", { class: "select-course" }, [
      vue.createElementVNode("view", { class: "search-bar" }, [
        vue.createVNode(_component_u_input, {
          modelValue: $data.searchText,
          "onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => $data.searchText = $event),
          placeholder: "搜索课程"
        }, null, 8, ["modelValue"])
      ]),
      vue.createElementVNode("view", { class: "course-list" }, [
        (vue.openBlock(true), vue.createElementBlock(
          vue.Fragment,
          null,
          vue.renderList($options.filteredCourses, (course) => {
            return vue.openBlock(), vue.createElementBlock("view", {
              key: course.id,
              class: "course-item"
            }, [
              vue.createElementVNode(
                "text",
                { class: "course-name" },
                vue.toDisplayString(course.name),
                1
                /* TEXT */
              ),
              vue.createElementVNode(
                "text",
                { class: "course-teacher" },
                "教师：" + vue.toDisplayString(course.teacher),
                1
                /* TEXT */
              ),
              vue.createElementVNode(
                "text",
                { class: "course-time" },
                "时间：" + vue.toDisplayString(course.time),
                1
                /* TEXT */
              ),
              vue.createVNode(_component_u_button, {
                size: "small",
                type: "primary",
                onClick: ($event) => $options.selectCourse(course)
              }, {
                default: vue.withCtx(() => [
                  vue.createTextVNode("选择")
                ]),
                _: 2
                /* DYNAMIC */
              }, 1032, ["onClick"])
            ]);
          }),
          128
          /* KEYED_FRAGMENT */
        ))
      ])
    ]);
  }
  const PagesStudentSelectCourse = /* @__PURE__ */ _export_sfc(_sfc_main$c, [["render", _sfc_render$c], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/student/select-course.vue"]]);
  const _sfc_main$b = {
    data() {
      return {
        myCourses: []
      };
    }
  };
  function _sfc_render$b(_ctx, _cache, $props, $setup, $data, $options) {
    return vue.openBlock(), vue.createElementBlock("view", { class: "my-courses" }, [
      vue.createElementVNode("text", { class: "title" }, "我的课程"),
      vue.createElementVNode("view", { class: "course-list" }, [
        (vue.openBlock(true), vue.createElementBlock(
          vue.Fragment,
          null,
          vue.renderList($data.myCourses, (course) => {
            return vue.openBlock(), vue.createElementBlock("view", {
              key: course.id,
              class: "course-item"
            }, [
              vue.createElementVNode(
                "text",
                { class: "course-name" },
                vue.toDisplayString(course.name),
                1
                /* TEXT */
              ),
              vue.createElementVNode(
                "text",
                { class: "course-info" },
                vue.toDisplayString(course.teacher) + " | " + vue.toDisplayString(course.time),
                1
                /* TEXT */
              )
            ]);
          }),
          128
          /* KEYED_FRAGMENT */
        ))
      ])
    ]);
  }
  const PagesStudentMyCourses = /* @__PURE__ */ _export_sfc(_sfc_main$b, [["render", _sfc_render$b], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/student/my-courses.vue"]]);
  const _sfc_main$a = {
    data() {
      return {
        scheduleData: []
      };
    }
  };
  function _sfc_render$a(_ctx, _cache, $props, $setup, $data, $options) {
    return vue.openBlock(), vue.createElementBlock("view", { class: "schedule" }, [
      vue.createElementVNode("text", { class: "title" }, "我的课表"),
      vue.createElementVNode("view", { class: "schedule-table" }, [
        vue.createCommentVNode(" TODO: 实现课表组件 "),
        vue.createElementVNode("text", null, "课表功能开发中...")
      ])
    ]);
  }
  const PagesStudentSchedule = /* @__PURE__ */ _export_sfc(_sfc_main$a, [["render", _sfc_render$a], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/student/schedule.vue"]]);
  const _sfc_main$9 = {
    data() {
      return {
        courses: [
          { id: 1, name: "高等数学", studentCount: 45 },
          { id: 2, name: "线性代数", studentCount: 38 }
        ]
      };
    },
    methods: {
      manageCourse(course) {
        formatAppLog("log", "at pages/teacher/courses.vue:26", "管理课程:", course);
      }
    }
  };
  function _sfc_render$9(_ctx, _cache, $props, $setup, $data, $options) {
    const _component_u_button = vue.resolveComponent("u-button");
    return vue.openBlock(), vue.createElementBlock("view", { class: "teacher-courses" }, [
      vue.createElementVNode("text", { class: "title" }, "课程管理"),
      vue.createElementVNode("view", { class: "course-list" }, [
        (vue.openBlock(true), vue.createElementBlock(
          vue.Fragment,
          null,
          vue.renderList($data.courses, (course) => {
            return vue.openBlock(), vue.createElementBlock("view", {
              key: course.id,
              class: "course-item"
            }, [
              vue.createElementVNode(
                "text",
                { class: "course-name" },
                vue.toDisplayString(course.name),
                1
                /* TEXT */
              ),
              vue.createElementVNode(
                "text",
                { class: "course-info" },
                "学生数：" + vue.toDisplayString(course.studentCount),
                1
                /* TEXT */
              ),
              vue.createVNode(_component_u_button, {
                size: "small",
                onClick: ($event) => $options.manageCourse(course)
              }, {
                default: vue.withCtx(() => [
                  vue.createTextVNode("管理")
                ]),
                _: 2
                /* DYNAMIC */
              }, 1032, ["onClick"])
            ]);
          }),
          128
          /* KEYED_FRAGMENT */
        ))
      ])
    ]);
  }
  const PagesTeacherCourses = /* @__PURE__ */ _export_sfc(_sfc_main$9, [["render", _sfc_render$9], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/teacher/courses.vue"]]);
  const _sfc_main$8 = {
    data() {
      return {
        classes: [
          { id: 1, name: "高等数学A班", studentCount: 30 },
          { id: 2, name: "高等数学B班", studentCount: 28 }
        ]
      };
    },
    methods: {
      manageClass(classItem) {
        formatAppLog("log", "at pages/teacher/teaching-classes.vue:26", "管理教学班:", classItem);
      }
    }
  };
  function _sfc_render$8(_ctx, _cache, $props, $setup, $data, $options) {
    const _component_u_button = vue.resolveComponent("u-button");
    return vue.openBlock(), vue.createElementBlock("view", { class: "teaching-classes" }, [
      vue.createElementVNode("text", { class: "title" }, "教学班管理"),
      vue.createElementVNode("view", { class: "class-list" }, [
        (vue.openBlock(true), vue.createElementBlock(
          vue.Fragment,
          null,
          vue.renderList($data.classes, (classItem) => {
            return vue.openBlock(), vue.createElementBlock("view", {
              key: classItem.id,
              class: "class-item"
            }, [
              vue.createElementVNode(
                "text",
                { class: "class-name" },
                vue.toDisplayString(classItem.name),
                1
                /* TEXT */
              ),
              vue.createElementVNode(
                "text",
                { class: "class-info" },
                "人数：" + vue.toDisplayString(classItem.studentCount),
                1
                /* TEXT */
              ),
              vue.createVNode(_component_u_button, {
                size: "small",
                onClick: ($event) => $options.manageClass(classItem)
              }, {
                default: vue.withCtx(() => [
                  vue.createTextVNode("查看详情")
                ]),
                _: 2
                /* DYNAMIC */
              }, 1032, ["onClick"])
            ]);
          }),
          128
          /* KEYED_FRAGMENT */
        ))
      ])
    ]);
  }
  const PagesTeacherTeachingClasses = /* @__PURE__ */ _export_sfc(_sfc_main$8, [["render", _sfc_render$8], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/teacher/teaching-classes.vue"]]);
  const _sfc_main$7 = {
    name: "AdminUsers",
    data() {
      return {
        loading: false,
        submitting: false,
        userList: [],
        searchQuery: "",
        searchTimeout: null,
        currentPage: 0,
        pageSize: 10,
        hasMore: true,
        totalUsers: 0,
        // 统计数据
        stats: {
          totalStudents: 0,
          totalTeachers: 0
        },
        // 弹窗相关
        showUserDialog: false,
        isEdit: false,
        userForm: {
          id: null,
          username: "",
          password: "",
          newPassword: "",
          realName: "",
          role: "student",
          studentId: null,
          grade: "",
          className: "",
          teacherId: null,
          title: "",
          department: ""
        },
        roleOptions: [
          { value: "student", label: "学生" },
          { value: "teacher", label: "教师" }
        ]
      };
    },
    computed: {
      totalAdmins() {
        return this.userList.filter((user) => user.role === "admin").length;
      }
    },
    async onLoad() {
      await this.loadUsers();
      await this.loadStats();
    },
    methods: {
      async loadUsers(reset = true) {
        if (this.loading)
          return;
        try {
          this.loading = true;
          if (reset) {
            this.currentPage = 0;
            this.userList = [];
          }
          const params = {
            page: this.currentPage,
            size: this.pageSize
          };
          if (this.searchQuery.trim()) {
            params.search = this.searchQuery.trim();
          }
          const response = await adminApi.getUsers(params);
          const { content, totalElements, totalPages } = response;
          if (reset) {
            this.userList = content;
          } else {
            this.userList.push(...content);
          }
          this.totalUsers = totalElements;
          this.hasMore = this.currentPage < totalPages - 1;
        } catch (error) {
          formatAppLog("error", "at pages/admin/users.vue:348", "加载用户列表失败:", error);
          uni.showToast({
            title: "加载失败",
            icon: "error"
          });
        } finally {
          this.loading = false;
        }
      },
      async loadStats() {
        try {
          const stats = await adminApi.getStats();
          this.stats = stats;
        } catch (error) {
          formatAppLog("error", "at pages/admin/users.vue:363", "加载统计数据失败:", error);
        }
      },
      async loadMore() {
        if (!this.hasMore || this.loading)
          return;
        this.currentPage++;
        await this.loadUsers(false);
      },
      onSearchInput() {
        if (this.searchTimeout) {
          clearTimeout(this.searchTimeout);
        }
        this.searchTimeout = setTimeout(() => {
          this.loadUsers();
        }, 500);
      },
      getUserAvatar(name) {
        return name ? name.charAt(0).toUpperCase() : "?";
      },
      getRoleClass(role) {
        return `role-${role}`;
      },
      getRoleDisplayName(role) {
        const names = {
          admin: "管理员",
          teacher: "教师",
          student: "学生"
        };
        return names[role] || role;
      },
      viewUser(user) {
        formatAppLog("log", "at pages/admin/users.vue:403", "查看用户:", user);
      },
      showAddDialog() {
        this.isEdit = false;
        this.resetUserForm();
        this.showUserDialog = true;
      },
      editUser(user) {
        this.isEdit = true;
        this.userForm = {
          id: user.id,
          username: user.username,
          password: "",
          newPassword: "",
          realName: user.real_name,
          role: user.role,
          studentId: user.roleId,
          grade: user.grade || "",
          className: user.className || "",
          teacherId: user.roleId,
          title: user.title || "",
          department: user.department || ""
        };
        this.showUserDialog = true;
      },
      async deleteUser(user) {
        const result = await uni.showModal({
          title: "确认删除",
          content: `确定要删除用户 "${user.real_name}" 吗？此操作不可恢复。`,
          confirmText: "删除",
          cancelText: "取消"
        });
        if (!result.confirm)
          return;
        try {
          await adminApi.deleteUser(user.id);
          uni.showToast({
            title: "删除成功",
            icon: "success"
          });
          await this.loadUsers();
          await this.loadStats();
        } catch (error) {
          formatAppLog("error", "at pages/admin/users.vue:450", "删除用户失败:", error);
          uni.showToast({
            title: "删除失败",
            icon: "error"
          });
        }
      },
      async submitUser() {
        if (!this.validateForm())
          return;
        try {
          this.submitting = true;
          const formData = {
            username: this.userForm.username,
            realName: this.userForm.realName,
            role: this.userForm.role
          };
          if (this.userForm.role === "student") {
            if (this.userForm.studentId) {
              formData.studentId = Number(this.userForm.studentId);
            }
            if (this.userForm.grade) {
              formData.grade = this.userForm.grade;
            }
            if (this.userForm.className) {
              formData.className = this.userForm.className;
            }
          } else if (this.userForm.role === "teacher") {
            if (this.userForm.teacherId) {
              formData.teacherId = Number(this.userForm.teacherId);
            }
            if (this.userForm.title) {
              formData.title = this.userForm.title;
            }
            if (this.userForm.department) {
              formData.department = this.userForm.department;
            }
          }
          if (this.isEdit) {
            if (this.userForm.newPassword) {
              formData.newPassword = this.userForm.newPassword;
            }
            await adminApi.updateUser(this.userForm.id, formData);
            uni.showToast({
              title: "更新成功",
              icon: "success"
            });
          } else {
            formData.password = this.userForm.password;
            await adminApi.createUser(formData);
            uni.showToast({
              title: "添加成功",
              icon: "success"
            });
          }
          this.closeUserDialog();
          await this.loadUsers();
          await this.loadStats();
        } catch (error) {
          formatAppLog("error", "at pages/admin/users.vue:518", "提交用户信息失败:", error);
          uni.showToast({
            title: "操作失败",
            icon: "error"
          });
        } finally {
          this.submitting = false;
        }
      },
      validateForm() {
        if (!this.userForm.username.trim()) {
          uni.showToast({
            title: "请输入用户名",
            icon: "error"
          });
          return false;
        }
        if (!this.isEdit && !this.userForm.password.trim()) {
          uni.showToast({
            title: "请输入密码",
            icon: "error"
          });
          return false;
        }
        if (!this.userForm.realName.trim()) {
          uni.showToast({
            title: "请输入真实姓名",
            icon: "error"
          });
          return false;
        }
        return true;
      },
      resetUserForm() {
        this.userForm = {
          id: null,
          username: "",
          password: "",
          newPassword: "",
          realName: "",
          role: "student",
          studentId: null,
          grade: "",
          className: "",
          teacherId: null,
          title: "",
          department: ""
        };
      },
      closeUserDialog() {
        this.showUserDialog = false;
        this.resetUserForm();
      }
    }
  };
  function _sfc_render$7(_ctx, _cache, $props, $setup, $data, $options) {
    return vue.openBlock(), vue.createElementBlock("view", { class: "users-page" }, [
      vue.createCommentVNode(" 页面标题 "),
      vue.createElementVNode("view", { class: "page-header" }, [
        vue.createElementVNode("text", { class: "page-title" }, "用户管理")
      ]),
      vue.createCommentVNode(" 统计卡片 "),
      vue.createElementVNode("view", { class: "stats-section" }, [
        vue.createElementVNode("view", { class: "stats-grid" }, [
          vue.createElementVNode("view", { class: "stat-card" }, [
            vue.createElementVNode(
              "text",
              { class: "stat-value" },
              vue.toDisplayString($data.stats.totalStudents),
              1
              /* TEXT */
            ),
            vue.createElementVNode("text", { class: "stat-label" }, "学生用户")
          ]),
          vue.createElementVNode("view", { class: "stat-card" }, [
            vue.createElementVNode(
              "text",
              { class: "stat-value" },
              vue.toDisplayString($data.stats.totalTeachers),
              1
              /* TEXT */
            ),
            vue.createElementVNode("text", { class: "stat-label" }, "教师用户")
          ]),
          vue.createElementVNode("view", { class: "stat-card" }, [
            vue.createElementVNode(
              "text",
              { class: "stat-value" },
              vue.toDisplayString($options.totalAdmins),
              1
              /* TEXT */
            ),
            vue.createElementVNode("text", { class: "stat-label" }, "管理员")
          ])
        ])
      ]),
      vue.createCommentVNode(" 搜索和操作栏 "),
      vue.createElementVNode("view", { class: "operation-bar" }, [
        vue.createElementVNode("view", { class: "search-container" }, [
          vue.withDirectives(vue.createElementVNode(
            "input",
            {
              type: "text",
              "onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => $data.searchQuery = $event),
              placeholder: "搜索用户名或姓名...",
              class: "search-input",
              onInput: _cache[1] || (_cache[1] = (...args) => $options.onSearchInput && $options.onSearchInput(...args))
            },
            null,
            544
            /* NEED_HYDRATION, NEED_PATCH */
          ), [
            [vue.vModelText, $data.searchQuery]
          ]),
          vue.createElementVNode("text", { class: "search-icon" }, "🔍")
        ]),
        vue.createElementVNode("button", {
          class: "add-btn",
          onClick: _cache[2] || (_cache[2] = (...args) => $options.showAddDialog && $options.showAddDialog(...args))
        }, [
          vue.createElementVNode("text", { class: "add-btn-text" }, "+ 添加用户")
        ])
      ]),
      vue.createCommentVNode(" 用户列表 "),
      !$data.loading ? (vue.openBlock(), vue.createElementBlock("view", {
        key: 0,
        class: "user-list"
      }, [
        (vue.openBlock(true), vue.createElementBlock(
          vue.Fragment,
          null,
          vue.renderList($data.userList, (user) => {
            return vue.openBlock(), vue.createElementBlock("view", {
              class: "user-card",
              key: user.id,
              onClick: ($event) => $options.viewUser(user)
            }, [
              vue.createElementVNode("view", { class: "user-header" }, [
                vue.createElementVNode("view", { class: "user-avatar" }, [
                  vue.createElementVNode(
                    "text",
                    { class: "avatar-text" },
                    vue.toDisplayString($options.getUserAvatar(user.real_name)),
                    1
                    /* TEXT */
                  )
                ]),
                vue.createElementVNode("view", { class: "user-info" }, [
                  vue.createElementVNode(
                    "text",
                    { class: "user-name" },
                    vue.toDisplayString(user.real_name),
                    1
                    /* TEXT */
                  ),
                  vue.createElementVNode(
                    "text",
                    { class: "user-username" },
                    "@" + vue.toDisplayString(user.username),
                    1
                    /* TEXT */
                  )
                ]),
                vue.createElementVNode(
                  "view",
                  {
                    class: vue.normalizeClass(["role-tag", $options.getRoleClass(user.role)])
                  },
                  [
                    vue.createElementVNode(
                      "text",
                      { class: "role-text" },
                      vue.toDisplayString($options.getRoleDisplayName(user.role)),
                      1
                      /* TEXT */
                    )
                  ],
                  2
                  /* CLASS */
                )
              ]),
              user.role === "student" ? (vue.openBlock(), vue.createElementBlock("view", {
                key: 0,
                class: "user-details"
              }, [
                vue.createElementVNode("view", { class: "detail-item" }, [
                  vue.createElementVNode("text", { class: "detail-label" }, "年级："),
                  vue.createElementVNode(
                    "text",
                    { class: "detail-value" },
                    vue.toDisplayString(user.grade || "未设置"),
                    1
                    /* TEXT */
                  )
                ]),
                vue.createElementVNode("view", { class: "detail-item" }, [
                  vue.createElementVNode("text", { class: "detail-label" }, "班级："),
                  vue.createElementVNode(
                    "text",
                    { class: "detail-value" },
                    vue.toDisplayString(user.className || "未设置"),
                    1
                    /* TEXT */
                  )
                ])
              ])) : user.role === "teacher" ? (vue.openBlock(), vue.createElementBlock("view", {
                key: 1,
                class: "user-details"
              }, [
                vue.createElementVNode("view", { class: "detail-item" }, [
                  vue.createElementVNode("text", { class: "detail-label" }, "职称："),
                  vue.createElementVNode(
                    "text",
                    { class: "detail-value" },
                    vue.toDisplayString(user.title || "未设置"),
                    1
                    /* TEXT */
                  )
                ]),
                vue.createElementVNode("view", { class: "detail-item" }, [
                  vue.createElementVNode("text", { class: "detail-label" }, "院系："),
                  vue.createElementVNode(
                    "text",
                    { class: "detail-value" },
                    vue.toDisplayString(user.department || "未设置"),
                    1
                    /* TEXT */
                  )
                ])
              ])) : vue.createCommentVNode("v-if", true),
              vue.createElementVNode("view", { class: "user-actions" }, [
                vue.createElementVNode("button", {
                  class: "action-btn edit-btn",
                  onClick: vue.withModifiers(($event) => $options.editUser(user), ["stop"])
                }, [
                  vue.createElementVNode("text", null, "编辑")
                ], 8, ["onClick"]),
                vue.createElementVNode("button", {
                  class: "action-btn delete-btn",
                  onClick: vue.withModifiers(($event) => $options.deleteUser(user), ["stop"])
                }, [
                  vue.createElementVNode("text", null, "删除")
                ], 8, ["onClick"])
              ])
            ], 8, ["onClick"]);
          }),
          128
          /* KEYED_FRAGMENT */
        ))
      ])) : vue.createCommentVNode("v-if", true),
      vue.createCommentVNode(" 加载状态 "),
      $data.loading ? (vue.openBlock(), vue.createElementBlock("view", {
        key: 1,
        class: "loading-container"
      }, [
        vue.createElementVNode("text", { class: "loading-text" }, "加载中...")
      ])) : vue.createCommentVNode("v-if", true),
      vue.createCommentVNode(" 空状态 "),
      !$data.loading && $data.userList.length === 0 ? (vue.openBlock(), vue.createElementBlock("view", {
        key: 2,
        class: "empty-state"
      }, [
        vue.createElementVNode("text", { class: "empty-icon" }, "👥"),
        vue.createElementVNode("text", { class: "empty-text" }, "暂无用户数据")
      ])) : vue.createCommentVNode("v-if", true),
      vue.createCommentVNode(" 加载更多 "),
      !$data.loading && $data.hasMore ? (vue.openBlock(), vue.createElementBlock("view", {
        key: 3,
        class: "load-more"
      }, [
        vue.createElementVNode("button", {
          class: "load-more-btn",
          onClick: _cache[3] || (_cache[3] = (...args) => $options.loadMore && $options.loadMore(...args))
        }, [
          vue.createElementVNode("text", null, "加载更多")
        ])
      ])) : vue.createCommentVNode("v-if", true),
      vue.createCommentVNode(" 添加/编辑用户弹窗 "),
      $data.showUserDialog ? (vue.openBlock(), vue.createElementBlock("view", {
        key: 4,
        class: "modal-overlay",
        onClick: _cache[18] || (_cache[18] = (...args) => $options.closeUserDialog && $options.closeUserDialog(...args))
      }, [
        vue.createElementVNode("view", {
          class: "modal-content",
          onClick: _cache[17] || (_cache[17] = vue.withModifiers(() => {
          }, ["stop"]))
        }, [
          vue.createElementVNode("view", { class: "modal-header" }, [
            vue.createElementVNode(
              "text",
              { class: "modal-title" },
              vue.toDisplayString($data.isEdit ? "编辑用户" : "添加用户"),
              1
              /* TEXT */
            ),
            vue.createElementVNode("button", {
              class: "close-btn",
              onClick: _cache[4] || (_cache[4] = (...args) => $options.closeUserDialog && $options.closeUserDialog(...args))
            }, "×")
          ]),
          vue.createElementVNode("view", { class: "form-content" }, [
            vue.createElementVNode("view", { class: "form-group" }, [
              vue.createElementVNode("text", { class: "form-label" }, "用户名 *"),
              vue.withDirectives(vue.createElementVNode("input", {
                type: "text",
                "onUpdate:modelValue": _cache[5] || (_cache[5] = ($event) => $data.userForm.username = $event),
                class: "form-input",
                disabled: $data.isEdit,
                placeholder: "请输入用户名"
              }, null, 8, ["disabled"]), [
                [vue.vModelText, $data.userForm.username]
              ])
            ]),
            !$data.isEdit ? (vue.openBlock(), vue.createElementBlock("view", {
              key: 0,
              class: "form-group"
            }, [
              vue.createElementVNode("text", { class: "form-label" }, "密码 *"),
              vue.withDirectives(vue.createElementVNode(
                "input",
                {
                  type: "password",
                  "onUpdate:modelValue": _cache[6] || (_cache[6] = ($event) => $data.userForm.password = $event),
                  class: "form-input",
                  placeholder: "请输入密码"
                },
                null,
                512
                /* NEED_PATCH */
              ), [
                [vue.vModelText, $data.userForm.password]
              ])
            ])) : vue.createCommentVNode("v-if", true),
            $data.isEdit ? (vue.openBlock(), vue.createElementBlock("view", {
              key: 1,
              class: "form-group"
            }, [
              vue.createElementVNode("text", { class: "form-label" }, "新密码"),
              vue.withDirectives(vue.createElementVNode(
                "input",
                {
                  type: "password",
                  "onUpdate:modelValue": _cache[7] || (_cache[7] = ($event) => $data.userForm.newPassword = $event),
                  class: "form-input",
                  placeholder: "留空则不修改密码"
                },
                null,
                512
                /* NEED_PATCH */
              ), [
                [vue.vModelText, $data.userForm.newPassword]
              ])
            ])) : vue.createCommentVNode("v-if", true),
            vue.createElementVNode("view", { class: "form-group" }, [
              vue.createElementVNode("text", { class: "form-label" }, "真实姓名 *"),
              vue.withDirectives(vue.createElementVNode(
                "input",
                {
                  type: "text",
                  "onUpdate:modelValue": _cache[8] || (_cache[8] = ($event) => $data.userForm.realName = $event),
                  class: "form-input",
                  placeholder: "请输入真实姓名"
                },
                null,
                512
                /* NEED_PATCH */
              ), [
                [vue.vModelText, $data.userForm.realName]
              ])
            ]),
            vue.createElementVNode("view", { class: "form-group" }, [
              vue.createElementVNode("text", { class: "form-label" }, "角色 *"),
              vue.createElementVNode("view", { class: "role-selector" }, [
                (vue.openBlock(true), vue.createElementBlock(
                  vue.Fragment,
                  null,
                  vue.renderList($data.roleOptions, (role) => {
                    return vue.openBlock(), vue.createElementBlock("label", {
                      class: "role-option",
                      key: role.value
                    }, [
                      vue.withDirectives(vue.createElementVNode("input", {
                        type: "radio",
                        name: role.value,
                        value: role.value,
                        "onUpdate:modelValue": ($event) => $data.userForm.role = $event,
                        checked: $data.userForm.role === role.value
                      }, null, 8, ["name", "value", "onUpdate:modelValue", "checked"]), [
                        [vue.vModelRadio, $data.userForm.role]
                      ]),
                      vue.createElementVNode(
                        "text",
                        { class: "role-option-text" },
                        vue.toDisplayString(role.label),
                        1
                        /* TEXT */
                      )
                    ]);
                  }),
                  128
                  /* KEYED_FRAGMENT */
                ))
              ])
            ]),
            vue.createCommentVNode(" 学生特定字段 "),
            $data.userForm.role === "student" ? (vue.openBlock(), vue.createElementBlock(
              vue.Fragment,
              { key: 2 },
              [
                vue.createElementVNode("view", { class: "form-group" }, [
                  vue.createElementVNode("text", { class: "form-label" }, "学号"),
                  vue.withDirectives(vue.createElementVNode(
                    "input",
                    {
                      type: "number",
                      "onUpdate:modelValue": _cache[9] || (_cache[9] = ($event) => $data.userForm.studentId = $event),
                      class: "form-input",
                      placeholder: "请输入学号"
                    },
                    null,
                    512
                    /* NEED_PATCH */
                  ), [
                    [vue.vModelText, $data.userForm.studentId]
                  ])
                ]),
                vue.createElementVNode("view", { class: "form-group" }, [
                  vue.createElementVNode("text", { class: "form-label" }, "年级"),
                  vue.withDirectives(vue.createElementVNode(
                    "input",
                    {
                      type: "text",
                      "onUpdate:modelValue": _cache[10] || (_cache[10] = ($event) => $data.userForm.grade = $event),
                      class: "form-input",
                      placeholder: "如：2023级"
                    },
                    null,
                    512
                    /* NEED_PATCH */
                  ), [
                    [vue.vModelText, $data.userForm.grade]
                  ])
                ]),
                vue.createElementVNode("view", { class: "form-group" }, [
                  vue.createElementVNode("text", { class: "form-label" }, "班级"),
                  vue.withDirectives(vue.createElementVNode(
                    "input",
                    {
                      type: "text",
                      "onUpdate:modelValue": _cache[11] || (_cache[11] = ($event) => $data.userForm.className = $event),
                      class: "form-input",
                      placeholder: "如：计算机科学与技术1班"
                    },
                    null,
                    512
                    /* NEED_PATCH */
                  ), [
                    [vue.vModelText, $data.userForm.className]
                  ])
                ])
              ],
              64
              /* STABLE_FRAGMENT */
            )) : vue.createCommentVNode("v-if", true),
            vue.createCommentVNode(" 教师特定字段 "),
            $data.userForm.role === "teacher" ? (vue.openBlock(), vue.createElementBlock(
              vue.Fragment,
              { key: 3 },
              [
                vue.createElementVNode("view", { class: "form-group" }, [
                  vue.createElementVNode("text", { class: "form-label" }, "教师编号"),
                  vue.withDirectives(vue.createElementVNode(
                    "input",
                    {
                      type: "number",
                      "onUpdate:modelValue": _cache[12] || (_cache[12] = ($event) => $data.userForm.teacherId = $event),
                      class: "form-input",
                      placeholder: "请输入教师编号"
                    },
                    null,
                    512
                    /* NEED_PATCH */
                  ), [
                    [vue.vModelText, $data.userForm.teacherId]
                  ])
                ]),
                vue.createElementVNode("view", { class: "form-group" }, [
                  vue.createElementVNode("text", { class: "form-label" }, "职称"),
                  vue.withDirectives(vue.createElementVNode(
                    "input",
                    {
                      type: "text",
                      "onUpdate:modelValue": _cache[13] || (_cache[13] = ($event) => $data.userForm.title = $event),
                      class: "form-input",
                      placeholder: "如：教授、副教授、讲师"
                    },
                    null,
                    512
                    /* NEED_PATCH */
                  ), [
                    [vue.vModelText, $data.userForm.title]
                  ])
                ]),
                vue.createElementVNode("view", { class: "form-group" }, [
                  vue.createElementVNode("text", { class: "form-label" }, "院系"),
                  vue.withDirectives(vue.createElementVNode(
                    "input",
                    {
                      type: "text",
                      "onUpdate:modelValue": _cache[14] || (_cache[14] = ($event) => $data.userForm.department = $event),
                      class: "form-input",
                      placeholder: "如：计算机科学与技术学院"
                    },
                    null,
                    512
                    /* NEED_PATCH */
                  ), [
                    [vue.vModelText, $data.userForm.department]
                  ])
                ])
              ],
              64
              /* STABLE_FRAGMENT */
            )) : vue.createCommentVNode("v-if", true)
          ]),
          vue.createElementVNode("view", { class: "modal-footer" }, [
            vue.createElementVNode("button", {
              class: "cancel-btn",
              onClick: _cache[15] || (_cache[15] = (...args) => $options.closeUserDialog && $options.closeUserDialog(...args))
            }, "取消"),
            vue.createElementVNode("button", {
              class: "confirm-btn",
              onClick: _cache[16] || (_cache[16] = (...args) => $options.submitUser && $options.submitUser(...args)),
              disabled: $data.submitting
            }, [
              vue.createElementVNode(
                "text",
                null,
                vue.toDisplayString($data.submitting ? "提交中..." : "确定"),
                1
                /* TEXT */
              )
            ], 8, ["disabled"])
          ])
        ])
      ])) : vue.createCommentVNode("v-if", true)
    ]);
  }
  const PagesAdminUsers = /* @__PURE__ */ _export_sfc(_sfc_main$7, [["render", _sfc_render$7], ["__scopeId", "data-v-eccda3f5"], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/admin/users.vue"]]);
  const _sfc_main$6 = {
    data() {
      return {
        courses: [],
        loading: false,
        showDialog: false,
        isEdit: false,
        submitting: false,
        form: {
          id: null,
          courseName: "",
          classCode: "",
          description: "",
          credit: 1,
          hours: 16
        }
      };
    },
    onLoad() {
      this.fetchCourses();
    },
    methods: {
      // 获取课程列表
      async fetchCourses() {
        try {
          this.loading = true;
          const courses = await adminApi.getCourses();
          this.courses = courses;
        } catch (error) {
          formatAppLog("error", "at pages/admin/courses.vue:145", "获取课程列表失败:", error);
          uni.showToast({
            title: "获取课程列表失败",
            icon: "error"
          });
        } finally {
          this.loading = false;
        }
      },
      // 打开新增对话框
      openAddDialog() {
        this.isEdit = false;
        this.resetForm();
        this.showDialog = true;
      },
      // 打开编辑对话框
      openEditDialog(course) {
        this.isEdit = true;
        this.form = { ...course };
        this.showDialog = true;
      },
      // 关闭对话框
      closeDialog() {
        this.showDialog = false;
        this.resetForm();
      },
      // 重置表单
      resetForm() {
        this.form = {
          id: null,
          courseName: "",
          classCode: "",
          description: "",
          credit: 1,
          hours: 16
        };
      },
      // 提交表单
      async submitForm() {
        if (!this.form.courseName.trim()) {
          uni.showToast({
            title: "请输入课程名称",
            icon: "error"
          });
          return;
        }
        if (!this.form.classCode.trim()) {
          uni.showToast({
            title: "请输入课程代码",
            icon: "error"
          });
          return;
        }
        if (!this.form.credit || this.form.credit <= 0) {
          uni.showToast({
            title: "请输入有效学分",
            icon: "error"
          });
          return;
        }
        if (!this.form.hours || this.form.hours <= 0) {
          uni.showToast({
            title: "请输入有效学时",
            icon: "error"
          });
          return;
        }
        try {
          this.submitting = true;
          if (this.isEdit) {
            await adminApi.updateCourse(this.form.id, this.form);
            uni.showToast({
              title: "更新成功",
              icon: "success"
            });
          } else {
            await adminApi.createCourse(this.form);
            uni.showToast({
              title: "添加成功",
              icon: "success"
            });
          }
          this.closeDialog();
          this.fetchCourses();
        } catch (error) {
          formatAppLog("error", "at pages/admin/courses.vue:243", "操作失败:", error);
          uni.showToast({
            title: "操作失败",
            icon: "error"
          });
        } finally {
          this.submitting = false;
        }
      },
      // 确认删除
      confirmDelete(course) {
        uni.showModal({
          title: "确认删除",
          content: `确定要删除课程"${course.courseName}"吗？此操作不可恢复。`,
          confirmText: "删除",
          confirmColor: "#ff4757",
          success: (res) => {
            if (res.confirm) {
              this.deleteCourse(course.id);
            }
          }
        });
      },
      // 删除课程
      async deleteCourse(courseId) {
        try {
          await adminApi.deleteCourse(courseId);
          uni.showToast({
            title: "删除成功",
            icon: "success"
          });
          this.fetchCourses();
        } catch (error) {
          formatAppLog("error", "at pages/admin/courses.vue:278", "删除失败:", error);
          uni.showToast({
            title: "删除失败",
            icon: "error"
          });
        }
      }
    }
  };
  function _sfc_render$6(_ctx, _cache, $props, $setup, $data, $options) {
    return vue.openBlock(), vue.createElementBlock("view", { class: "admin-courses" }, [
      vue.createCommentVNode(" 头部操作栏 "),
      vue.createElementVNode("view", { class: "header" }, [
        vue.createElementVNode("text", { class: "title" }, "课程管理"),
        vue.createElementVNode("button", {
          class: "add-btn",
          onClick: _cache[0] || (_cache[0] = (...args) => $options.openAddDialog && $options.openAddDialog(...args))
        }, "新增课程")
      ]),
      vue.createCommentVNode(" 课程列表 "),
      !$data.loading ? (vue.openBlock(), vue.createElementBlock("view", {
        key: 0,
        class: "course-list"
      }, [
        (vue.openBlock(true), vue.createElementBlock(
          vue.Fragment,
          null,
          vue.renderList($data.courses, (course) => {
            return vue.openBlock(), vue.createElementBlock("view", {
              key: course.id,
              class: "course-item"
            }, [
              vue.createElementVNode("view", { class: "course-info" }, [
                vue.createElementVNode(
                  "text",
                  { class: "course-name" },
                  vue.toDisplayString(course.courseName),
                  1
                  /* TEXT */
                ),
                vue.createElementVNode(
                  "text",
                  { class: "course-code" },
                  "课程代码：" + vue.toDisplayString(course.classCode),
                  1
                  /* TEXT */
                ),
                course.description ? (vue.openBlock(), vue.createElementBlock(
                  "text",
                  {
                    key: 0,
                    class: "course-desc"
                  },
                  vue.toDisplayString(course.description),
                  1
                  /* TEXT */
                )) : vue.createCommentVNode("v-if", true),
                vue.createElementVNode("view", { class: "course-meta" }, [
                  vue.createElementVNode(
                    "text",
                    { class: "meta-item" },
                    "学分：" + vue.toDisplayString(course.credit),
                    1
                    /* TEXT */
                  ),
                  vue.createElementVNode(
                    "text",
                    { class: "meta-item" },
                    "学时：" + vue.toDisplayString(course.hours),
                    1
                    /* TEXT */
                  )
                ])
              ]),
              vue.createElementVNode("view", { class: "actions" }, [
                vue.createElementVNode("button", {
                  class: "edit-btn",
                  onClick: ($event) => $options.openEditDialog(course)
                }, "编辑", 8, ["onClick"]),
                vue.createElementVNode("button", {
                  class: "delete-btn",
                  onClick: ($event) => $options.confirmDelete(course)
                }, "删除", 8, ["onClick"])
              ])
            ]);
          }),
          128
          /* KEYED_FRAGMENT */
        )),
        vue.createCommentVNode(" 空状态 "),
        $data.courses.length === 0 ? (vue.openBlock(), vue.createElementBlock("view", {
          key: 0,
          class: "empty-state"
        }, [
          vue.createElementVNode("text", { class: "empty-text" }, "暂无课程数据")
        ])) : vue.createCommentVNode("v-if", true)
      ])) : vue.createCommentVNode("v-if", true),
      vue.createCommentVNode(" 加载状态 "),
      $data.loading ? (vue.openBlock(), vue.createElementBlock("view", {
        key: 1,
        class: "loading"
      }, [
        vue.createElementVNode("text", { class: "loading-text" }, "加载中...")
      ])) : vue.createCommentVNode("v-if", true),
      vue.createCommentVNode(" 添加/编辑课程弹窗 "),
      $data.showDialog ? (vue.openBlock(), vue.createElementBlock("view", {
        key: 2,
        class: "dialog-overlay",
        onClick: _cache[10] || (_cache[10] = (...args) => $options.closeDialog && $options.closeDialog(...args))
      }, [
        vue.createElementVNode("view", {
          class: "dialog",
          onClick: _cache[9] || (_cache[9] = vue.withModifiers(() => {
          }, ["stop"]))
        }, [
          vue.createElementVNode("view", { class: "dialog-header" }, [
            vue.createElementVNode(
              "text",
              { class: "dialog-title" },
              vue.toDisplayString($data.isEdit ? "编辑课程" : "新增课程"),
              1
              /* TEXT */
            ),
            vue.createElementVNode("button", {
              class: "close-btn",
              onClick: _cache[1] || (_cache[1] = (...args) => $options.closeDialog && $options.closeDialog(...args))
            }, "×")
          ]),
          vue.createElementVNode("view", { class: "dialog-body" }, [
            vue.createElementVNode("view", { class: "form-item" }, [
              vue.createElementVNode("text", { class: "label" }, "课程名称 *"),
              vue.withDirectives(vue.createElementVNode(
                "input",
                {
                  class: "input",
                  "onUpdate:modelValue": _cache[2] || (_cache[2] = ($event) => $data.form.courseName = $event),
                  placeholder: "请输入课程名称",
                  maxlength: "50"
                },
                null,
                512
                /* NEED_PATCH */
              ), [
                [vue.vModelText, $data.form.courseName]
              ])
            ]),
            vue.createElementVNode("view", { class: "form-item" }, [
              vue.createElementVNode("text", { class: "label" }, "课程代码 *"),
              vue.withDirectives(vue.createElementVNode(
                "input",
                {
                  class: "input",
                  "onUpdate:modelValue": _cache[3] || (_cache[3] = ($event) => $data.form.classCode = $event),
                  placeholder: "例如：CS101",
                  maxlength: "20"
                },
                null,
                512
                /* NEED_PATCH */
              ), [
                [vue.vModelText, $data.form.classCode]
              ])
            ]),
            vue.createElementVNode("view", { class: "form-item" }, [
              vue.createElementVNode("text", { class: "label" }, "课程描述"),
              vue.withDirectives(vue.createElementVNode(
                "textarea",
                {
                  class: "textarea",
                  "onUpdate:modelValue": _cache[4] || (_cache[4] = ($event) => $data.form.description = $event),
                  placeholder: "请输入课程描述",
                  maxlength: "200"
                },
                null,
                512
                /* NEED_PATCH */
              ), [
                [vue.vModelText, $data.form.description]
              ])
            ]),
            vue.createElementVNode("view", { class: "form-row" }, [
              vue.createElementVNode("view", { class: "form-item half" }, [
                vue.createElementVNode("text", { class: "label" }, "学分 *"),
                vue.withDirectives(vue.createElementVNode(
                  "input",
                  {
                    class: "input",
                    "onUpdate:modelValue": _cache[5] || (_cache[5] = ($event) => $data.form.credit = $event),
                    type: "digit",
                    placeholder: "1.0"
                  },
                  null,
                  512
                  /* NEED_PATCH */
                ), [
                  [vue.vModelText, $data.form.credit]
                ])
              ]),
              vue.createElementVNode("view", { class: "form-item half" }, [
                vue.createElementVNode("text", { class: "label" }, "学时 *"),
                vue.withDirectives(vue.createElementVNode(
                  "input",
                  {
                    class: "input",
                    "onUpdate:modelValue": _cache[6] || (_cache[6] = ($event) => $data.form.hours = $event),
                    type: "number",
                    placeholder: "16"
                  },
                  null,
                  512
                  /* NEED_PATCH */
                ), [
                  [vue.vModelText, $data.form.hours]
                ])
              ])
            ])
          ]),
          vue.createElementVNode("view", { class: "dialog-footer" }, [
            vue.createElementVNode("button", {
              class: "cancel-btn",
              onClick: _cache[7] || (_cache[7] = (...args) => $options.closeDialog && $options.closeDialog(...args))
            }, "取消"),
            vue.createElementVNode("button", {
              class: "confirm-btn",
              onClick: _cache[8] || (_cache[8] = (...args) => $options.submitForm && $options.submitForm(...args)),
              disabled: $data.submitting
            }, vue.toDisplayString($data.submitting ? "提交中..." : "确定"), 9, ["disabled"])
          ])
        ])
      ])) : vue.createCommentVNode("v-if", true)
    ]);
  }
  const PagesAdminCourses = /* @__PURE__ */ _export_sfc(_sfc_main$6, [["render", _sfc_render$6], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/admin/courses.vue"]]);
  const _sfc_main$5 = {
    data() {
      return {
        loading: false,
        isGridView: true,
        // 数据
        teachingClasses: [],
        schedules: [],
        classrooms: [],
        // 选择状态
        selectedTeachingClass: null,
        selectedWeek: 1,
        // 时间段配置
        timeSlots: [
          { start: "08:00", end: "08:45" },
          { start: "08:55", end: "09:40" },
          { start: "10:00", end: "10:45" },
          { start: "14:00", end: "14:45" },
          { start: "15:00", end: "15:45" }
        ],
        // 网格数据 - grid[timeSlotIndex][dayOfWeek]
        grid: {},
        // 弹窗状态
        showDialog: false,
        isEdit: false,
        submitting: false,
        currentSchedule: null,
        // 表单数据
        form: {
          dayOfWeek: 1,
          timeSlotIndex: 0,
          classroomId: null,
          classroomIndex: 0,
          startTime: "",
          endTime: ""
        }
      };
    },
    computed: {
      teachingClassOptions() {
        return this.teachingClasses.map((tc) => ({
          value: tc.id,
          label: tc.classCode || `教学班${tc.id}`
        }));
      },
      selectedTeachingClassLabel() {
        const selected = this.teachingClassOptions.find((option) => option.value === this.selectedTeachingClass);
        return selected == null ? void 0 : selected.label;
      },
      weekOptions() {
        return Array.from({ length: 20 }, (_, i) => ({
          value: i + 1,
          label: `第${i + 1}周`
        }));
      },
      selectedWeekLabel() {
        return `第${this.selectedWeek}周`;
      },
      dayOptions() {
        return [
          { value: 1, label: "星期一" },
          { value: 2, label: "星期二" },
          { value: 3, label: "星期三" },
          { value: 4, label: "星期四" },
          { value: 5, label: "星期五" },
          { value: 6, label: "星期六" },
          { value: 7, label: "星期日" }
        ];
      },
      timeSlotOptions() {
        return this.timeSlots.map((slot, index) => ({
          value: index,
          label: `第${index + 1}节 (${slot.start}-${slot.end})`
        }));
      },
      classroomOptions() {
        return this.classrooms.map((classroom) => ({
          value: classroom.id,
          label: `${classroom.building || "教学楼"}-${classroom.classroomName || "未命名教室"}`
        }));
      }
    },
    onLoad() {
      this.initData();
    },
    methods: {
      // 初始化数据
      async initData() {
        this.loading = true;
        try {
          await Promise.all([
            this.loadTeachingClasses(),
            this.loadClassrooms()
          ]);
        } catch (error) {
          formatAppLog("error", "at pages/admin/manual-schedule.vue:280", "初始化数据失败:", error);
          uni.showToast({
            title: "初始化失败",
            icon: "error"
          });
        } finally {
          this.loading = false;
        }
      },
      // 加载教学班列表
      async loadTeachingClasses() {
        try {
          const teachingClasses = await adminApi.getTeachingClasses();
          this.teachingClasses = teachingClasses || [];
          if (this.teachingClasses.length > 0) {
            this.selectedTeachingClass = this.teachingClasses[0].id;
            this.loadSchedules();
          }
        } catch (error) {
          formatAppLog("error", "at pages/admin/manual-schedule.vue:302", "加载教学班失败:", error);
        }
      },
      // 加载教室列表
      async loadClassrooms() {
        try {
          const classrooms = await adminApi.getClassrooms();
          this.classrooms = classrooms || [];
          formatAppLog("log", "at pages/admin/manual-schedule.vue:311", "加载的教室数据:", this.classrooms);
          formatAppLog("log", "at pages/admin/manual-schedule.vue:312", "教室选项:", this.classroomOptions);
        } catch (error) {
          formatAppLog("error", "at pages/admin/manual-schedule.vue:314", "加载教室失败:", error);
          uni.showToast({
            title: "加载教室失败",
            icon: "error"
          });
        }
      },
      // 加载课程安排
      async loadSchedules() {
        if (!this.selectedTeachingClass)
          return;
        this.loading = true;
        try {
          const schedules = await this.getSchedulesByTeachingClass(this.selectedTeachingClass);
          this.schedules = schedules || [];
          this.buildGrid();
          uni.showToast({
            title: "加载完成",
            icon: "success"
          });
        } catch (error) {
          formatAppLog("error", "at pages/admin/manual-schedule.vue:338", "加载课程安排失败:", error);
          uni.showToast({
            title: "加载失败",
            icon: "error"
          });
        } finally {
          this.loading = false;
        }
      },
      // 构建网格数据
      buildGrid() {
        this.grid = {};
        for (let timeIndex = 0; timeIndex < this.timeSlots.length; timeIndex++) {
          this.grid[timeIndex] = {};
          for (let day = 1; day <= 7; day++) {
            this.grid[timeIndex][day] = null;
          }
        }
        this.schedules.forEach((schedule) => {
          const timeIndex = this.getTimeSlotIndex(schedule.startTime);
          if (timeIndex >= 0) {
            this.grid[timeIndex][schedule.dayOfWeek] = schedule;
          }
        });
      },
      // 获取时间段索引
      getTimeSlotIndex(startTime) {
        return this.timeSlots.findIndex((slot) => slot.start === startTime);
      },
      // 获取指定单元格的课程安排
      getScheduleForCell(day, timeIndex) {
        return this.grid[timeIndex] && this.grid[timeIndex][day];
      },
      // 获取星期标签
      getDayLabel(day) {
        const labels = ["", "一", "二", "三", "四", "五", "六", "日"];
        return labels[day] || day;
      },
      // 切换视图
      toggleView() {
        this.isGridView = !this.isGridView;
      },
      // 事件处理
      onTeachingClassChange(e) {
        const index = e.detail.value;
        this.selectedTeachingClass = this.teachingClassOptions[index].value;
        this.loadSchedules();
      },
      onWeekChange(e) {
        const index = e.detail.value;
        this.selectedWeek = this.weekOptions[index].value;
      },
      onDayChange(e) {
        const index = e.detail.value;
        this.form.dayOfWeek = this.dayOptions[index].value;
      },
      onTimeSlotChange(e) {
        const index = e.detail.value;
        this.form.timeSlotIndex = index;
        const timeSlot = this.timeSlots[index];
        this.form.startTime = timeSlot.start;
        this.form.endTime = timeSlot.end;
      },
      onClassroomChange(e) {
        var _a;
        const index = e.detail.value;
        this.form.classroomIndex = index;
        this.form.classroomId = ((_a = this.classroomOptions[index]) == null ? void 0 : _a.value) || null;
        formatAppLog("log", "at pages/admin/manual-schedule.vue:419", "选择教室:", index, this.classroomOptions[index]);
        if (this.classroomOptions[index]) {
          uni.showToast({
            title: `已选择: ${this.classroomOptions[index].label}`,
            icon: "none"
          });
        }
      },
      // 单元格点击
      onCellClick(day, timeIndex) {
        const existingSchedule = this.getScheduleForCell(day, timeIndex);
        if (existingSchedule) {
          this.editSchedule(existingSchedule);
        } else {
          this.openAddDialog(day, timeIndex);
        }
      },
      // 打开添加对话框
      openAddDialog(day = 1, timeIndex = 0) {
        this.isEdit = false;
        this.currentSchedule = null;
        this.form = {
          dayOfWeek: day,
          timeSlotIndex: timeIndex,
          classroomId: null,
          classroomIndex: 0,
          startTime: this.timeSlots[timeIndex].start,
          endTime: this.timeSlots[timeIndex].end
        };
        this.showDialog = true;
      },
      // 编辑课程安排
      editSchedule(schedule) {
        this.isEdit = true;
        this.currentSchedule = schedule;
        const timeIndex = this.getTimeSlotIndex(schedule.startTime);
        const classroom = this.classrooms.find(
          (c) => c.building === schedule.building && c.classroomName === schedule.classroomName
        );
        const classroomIndex = this.classroomOptions.findIndex((option) => option.value === (classroom == null ? void 0 : classroom.id));
        this.form = {
          dayOfWeek: schedule.dayOfWeek,
          timeSlotIndex: timeIndex >= 0 ? timeIndex : 0,
          classroomId: (classroom == null ? void 0 : classroom.id) || null,
          classroomIndex: classroomIndex >= 0 ? classroomIndex : 0,
          startTime: schedule.startTime,
          endTime: schedule.endTime
        };
        this.showDialog = true;
      },
      // 删除课程安排
      deleteSchedule(schedule) {
        uni.showModal({
          title: "确认删除",
          content: `确定要删除星期${this.getDayLabel(schedule.dayOfWeek)} ${schedule.startTime}的课程安排吗？`,
          confirmText: "删除",
          confirmColor: "#ff4757",
          success: async (res) => {
            if (res.confirm) {
              try {
                await this.deleteScheduleApi(schedule.id);
                uni.showToast({
                  title: "删除成功",
                  icon: "success"
                });
                this.loadSchedules();
              } catch (error) {
                formatAppLog("error", "at pages/admin/manual-schedule.vue:497", "删除失败:", error);
                uni.showToast({
                  title: "删除失败",
                  icon: "error"
                });
              }
            }
          }
        });
      },
      // 关闭对话框
      closeDialog() {
        this.showDialog = false;
        this.isEdit = false;
        this.currentSchedule = null;
      },
      // 提交表单
      async submitForm() {
        if (!this.form.classroomId) {
          uni.showToast({
            title: "请选择教室",
            icon: "error"
          });
          return;
        }
        if (!this.selectedTeachingClass) {
          uni.showToast({
            title: "请选择教学班",
            icon: "error"
          });
          return;
        }
        this.submitting = true;
        try {
          const classroom = this.classrooms.find((c) => c.id === this.form.classroomId);
          const payload = {
            dayOfWeek: this.form.dayOfWeek,
            startTime: this.form.startTime,
            endTime: this.form.endTime,
            classroomId: this.form.classroomId,
            building: (classroom == null ? void 0 : classroom.building) || "",
            classroomName: (classroom == null ? void 0 : classroom.classroomName) || "",
            week: this.selectedWeek
          };
          if (!this.isEdit) {
            const conflictResult = await this.checkConflict(payload);
            if (conflictResult.hasConflict && conflictResult.conflicts.length > 0) {
              const conflictInfo = conflictResult.conflicts.map(
                (c) => `星期${this.getDayLabel(c.dayOfWeek)} ${c.startTime}-${c.endTime}`
              ).join(", ");
              const confirmed = await this.showConfirmDialog(
                "课程冲突",
                `发现时间冲突：${conflictInfo}。是否继续添加？`
              );
              if (!confirmed) {
                this.submitting = false;
                return;
              }
            }
          }
          if (this.isEdit && this.currentSchedule) {
            await this.updateScheduleApi(this.currentSchedule.id, payload);
            uni.showToast({
              title: "更新成功",
              icon: "success"
            });
          } else {
            await this.addScheduleApi(this.selectedTeachingClass, payload);
            uni.showToast({
              title: "添加成功",
              icon: "success"
            });
          }
          this.closeDialog();
          this.loadSchedules();
        } catch (error) {
          formatAppLog("error", "at pages/admin/manual-schedule.vue:585", "提交失败:", error);
          uni.showToast({
            title: "操作失败",
            icon: "error"
          });
        } finally {
          this.submitting = false;
        }
      },
      // 显示确认对话框
      showConfirmDialog(title, content) {
        return new Promise((resolve) => {
          uni.showModal({
            title,
            content,
            confirmText: "继续",
            cancelText: "取消",
            success: (res) => {
              resolve(res.confirm);
            }
          });
        });
      },
      // 获取教室索引
      getClassroomIndex() {
        const index = this.classroomOptions.findIndex((option) => option.value === this.form.classroomId);
        return index >= 0 ? index : 0;
      },
      // 获取选中教室标签
      getSelectedClassroomLabel() {
        if (this.form.classroomIndex >= 0 && this.classroomOptions[this.form.classroomIndex]) {
          return this.classroomOptions[this.form.classroomIndex].label;
        }
        return "请选择教室";
      },
      // API 方法
      async getSchedulesByTeachingClass(teachingClassId) {
        try {
          const schedules = await adminApi.getSchedulesByTeachingClass(teachingClassId, {
            week: this.selectedWeek
          });
          return schedules || [];
        } catch (error) {
          formatAppLog("error", "at pages/admin/manual-schedule.vue:632", "获取课程安排失败:", error);
          return [];
        }
      },
      async addScheduleApi(teachingClassId, payload) {
        try {
          await adminApi.addSchedule(teachingClassId, payload);
        } catch (error) {
          formatAppLog("error", "at pages/admin/manual-schedule.vue:641", "添加课程安排失败:", error);
          throw error;
        }
      },
      async updateScheduleApi(scheduleId, payload) {
        try {
          await adminApi.updateSchedule(scheduleId, payload);
        } catch (error) {
          formatAppLog("error", "at pages/admin/manual-schedule.vue:650", "更新课程安排失败:", error);
          throw error;
        }
      },
      async deleteScheduleApi(scheduleId) {
        try {
          await adminApi.deleteSchedule(scheduleId);
        } catch (error) {
          formatAppLog("error", "at pages/admin/manual-schedule.vue:659", "删除课程安排失败:", error);
          throw error;
        }
      },
      // 检查课程安排冲突
      async checkConflict(payload) {
        try {
          const result = await adminApi.checkScheduleConflict(payload);
          return result;
        } catch (error) {
          formatAppLog("error", "at pages/admin/manual-schedule.vue:670", "检查冲突失败:", error);
          return { hasConflict: false, conflicts: [] };
        }
      }
    }
  };
  function _sfc_render$5(_ctx, _cache, $props, $setup, $data, $options) {
    var _a;
    return vue.openBlock(), vue.createElementBlock("view", { class: "manual-schedule" }, [
      vue.createCommentVNode(" 顶部筛选栏 "),
      vue.createElementVNode("view", { class: "top-bar" }, [
        vue.createElementVNode("view", { class: "filter-section" }, [
          vue.createElementVNode("picker", {
            mode: "selector",
            range: $options.teachingClassOptions,
            "range-key": "label",
            onChange: _cache[0] || (_cache[0] = (...args) => $options.onTeachingClassChange && $options.onTeachingClassChange(...args))
          }, [
            vue.createElementVNode("view", { class: "picker-input" }, [
              vue.createElementVNode(
                "text",
                null,
                vue.toDisplayString($options.selectedTeachingClassLabel || "选择教学班"),
                1
                /* TEXT */
              ),
              vue.createElementVNode("text", { class: "arrow" }, "▼")
            ])
          ], 40, ["range"]),
          vue.createElementVNode("picker", {
            mode: "selector",
            range: $options.weekOptions,
            "range-key": "label",
            onChange: _cache[1] || (_cache[1] = (...args) => $options.onWeekChange && $options.onWeekChange(...args))
          }, [
            vue.createElementVNode("view", { class: "picker-input" }, [
              vue.createElementVNode(
                "text",
                null,
                vue.toDisplayString($options.selectedWeekLabel || "选择周次"),
                1
                /* TEXT */
              ),
              vue.createElementVNode("text", { class: "arrow" }, "▼")
            ])
          ], 40, ["range"])
        ]),
        vue.createElementVNode("view", { class: "action-section" }, [
          vue.createElementVNode("button", {
            class: "refresh-btn",
            onClick: _cache[2] || (_cache[2] = (...args) => $options.loadSchedules && $options.loadSchedules(...args))
          }, "刷新"),
          vue.createElementVNode(
            "button",
            {
              class: "view-toggle-btn",
              onClick: _cache[3] || (_cache[3] = (...args) => $options.toggleView && $options.toggleView(...args))
            },
            vue.toDisplayString($data.isGridView ? "列表" : "网格"),
            1
            /* TEXT */
          )
        ])
      ]),
      vue.createCommentVNode(" 网格视图 - 课表 "),
      $data.isGridView ? (vue.openBlock(), vue.createElementBlock("view", {
        key: 0,
        class: "schedule-grid"
      }, [
        vue.createElementVNode("view", { class: "grid-header" }, [
          vue.createElementVNode("view", { class: "time-header" }, "时间"),
          (vue.openBlock(), vue.createElementBlock(
            vue.Fragment,
            null,
            vue.renderList(7, (day) => {
              return vue.createElementVNode(
                "view",
                {
                  key: day,
                  class: "day-header"
                },
                vue.toDisplayString($options.getDayLabel(day)),
                1
                /* TEXT */
              );
            }),
            64
            /* STABLE_FRAGMENT */
          ))
        ]),
        (vue.openBlock(true), vue.createElementBlock(
          vue.Fragment,
          null,
          vue.renderList($data.timeSlots, (timeSlot, index) => {
            return vue.openBlock(), vue.createElementBlock("view", {
              key: index,
              class: "grid-row"
            }, [
              vue.createElementVNode("view", { class: "time-cell" }, [
                vue.createElementVNode(
                  "text",
                  { class: "slot-number" },
                  "第" + vue.toDisplayString(index + 1) + "节",
                  1
                  /* TEXT */
                ),
                vue.createElementVNode(
                  "text",
                  { class: "slot-time" },
                  vue.toDisplayString(timeSlot.start) + "-" + vue.toDisplayString(timeSlot.end),
                  1
                  /* TEXT */
                )
              ]),
              (vue.openBlock(), vue.createElementBlock(
                vue.Fragment,
                null,
                vue.renderList(7, (day) => {
                  return vue.createElementVNode("view", {
                    key: day,
                    class: "schedule-cell",
                    onClick: ($event) => $options.onCellClick(day, index)
                  }, [
                    $options.getScheduleForCell(day, index) ? (vue.openBlock(), vue.createElementBlock("view", {
                      key: 0,
                      class: "schedule-content"
                    }, [
                      vue.createElementVNode(
                        "text",
                        { class: "classroom" },
                        vue.toDisplayString($options.getScheduleForCell(day, index).building) + "-" + vue.toDisplayString($options.getScheduleForCell(day, index).classroomName),
                        1
                        /* TEXT */
                      ),
                      vue.createElementVNode(
                        "text",
                        { class: "time" },
                        vue.toDisplayString($options.getScheduleForCell(day, index).startTime),
                        1
                        /* TEXT */
                      )
                    ])) : (vue.openBlock(), vue.createElementBlock("view", {
                      key: 1,
                      class: "empty-cell"
                    }, [
                      vue.createElementVNode("text", { class: "plus-icon" }, "+")
                    ]))
                  ], 8, ["onClick"]);
                }),
                64
                /* STABLE_FRAGMENT */
              ))
            ]);
          }),
          128
          /* KEYED_FRAGMENT */
        ))
      ])) : (vue.openBlock(), vue.createElementBlock(
        vue.Fragment,
        { key: 1 },
        [
          vue.createCommentVNode(" 列表视图 "),
          vue.createElementVNode("view", { class: "schedule-list" }, [
            $data.schedules.length === 0 ? (vue.openBlock(), vue.createElementBlock("view", {
              key: 0,
              class: "empty-state"
            }, [
              vue.createElementVNode("text", { class: "empty-text" }, "暂无课程安排"),
              vue.createElementVNode("button", {
                class: "add-btn",
                onClick: _cache[4] || (_cache[4] = (...args) => $options.openAddDialog && $options.openAddDialog(...args))
              }, "添加安排")
            ])) : (vue.openBlock(), vue.createElementBlock("view", { key: 1 }, [
              (vue.openBlock(true), vue.createElementBlock(
                vue.Fragment,
                null,
                vue.renderList($data.schedules, (schedule) => {
                  return vue.openBlock(), vue.createElementBlock("view", {
                    key: schedule.id,
                    class: "schedule-item"
                  }, [
                    vue.createElementVNode("view", { class: "schedule-info" }, [
                      vue.createElementVNode("view", { class: "schedule-header" }, [
                        vue.createElementVNode(
                          "text",
                          { class: "day" },
                          "星期" + vue.toDisplayString($options.getDayLabel(schedule.dayOfWeek)),
                          1
                          /* TEXT */
                        ),
                        vue.createElementVNode(
                          "text",
                          { class: "time" },
                          vue.toDisplayString(schedule.startTime) + "-" + vue.toDisplayString(schedule.endTime),
                          1
                          /* TEXT */
                        )
                      ]),
                      vue.createElementVNode("view", { class: "schedule-detail" }, [
                        vue.createElementVNode(
                          "text",
                          { class: "classroom" },
                          "教室：" + vue.toDisplayString(schedule.building) + "-" + vue.toDisplayString(schedule.classroomName),
                          1
                          /* TEXT */
                        )
                      ])
                    ]),
                    vue.createElementVNode("view", { class: "schedule-actions" }, [
                      vue.createElementVNode("button", {
                        class: "edit-btn",
                        onClick: ($event) => $options.editSchedule(schedule)
                      }, "编辑", 8, ["onClick"]),
                      vue.createElementVNode("button", {
                        class: "delete-btn",
                        onClick: ($event) => $options.deleteSchedule(schedule)
                      }, "删除", 8, ["onClick"])
                    ])
                  ]);
                }),
                128
                /* KEYED_FRAGMENT */
              ))
            ]))
          ])
        ],
        2112
        /* STABLE_FRAGMENT, DEV_ROOT_FRAGMENT */
      )),
      vue.createCommentVNode(" 添加/编辑弹窗 "),
      $data.showDialog ? (vue.openBlock(), vue.createElementBlock("view", {
        key: 2,
        class: "dialog-overlay",
        onClick: _cache[12] || (_cache[12] = (...args) => $options.closeDialog && $options.closeDialog(...args))
      }, [
        vue.createElementVNode("view", {
          class: "dialog",
          onClick: _cache[11] || (_cache[11] = vue.withModifiers(() => {
          }, ["stop"]))
        }, [
          vue.createElementVNode("view", { class: "dialog-header" }, [
            vue.createElementVNode(
              "text",
              { class: "dialog-title" },
              vue.toDisplayString($data.isEdit ? "编辑课程安排" : "添加课程安排"),
              1
              /* TEXT */
            ),
            vue.createElementVNode("button", {
              class: "close-btn",
              onClick: _cache[5] || (_cache[5] = (...args) => $options.closeDialog && $options.closeDialog(...args))
            }, "×")
          ]),
          vue.createElementVNode("view", { class: "dialog-body" }, [
            vue.createElementVNode("view", { class: "form-item" }, [
              vue.createElementVNode("text", { class: "label" }, "星期 *"),
              vue.createElementVNode("picker", {
                mode: "selector",
                range: $options.dayOptions,
                "range-key": "label",
                value: $data.form.dayOfWeek - 1,
                onChange: _cache[6] || (_cache[6] = (...args) => $options.onDayChange && $options.onDayChange(...args))
              }, [
                vue.createElementVNode("view", { class: "picker-input" }, [
                  vue.createElementVNode(
                    "text",
                    null,
                    vue.toDisplayString($options.getDayLabel($data.form.dayOfWeek)),
                    1
                    /* TEXT */
                  ),
                  vue.createElementVNode("text", { class: "arrow" }, "▼")
                ])
              ], 40, ["range", "value"])
            ]),
            vue.createElementVNode("view", { class: "form-item" }, [
              vue.createElementVNode("text", { class: "label" }, "时间段 *"),
              vue.createElementVNode("picker", {
                mode: "selector",
                range: $options.timeSlotOptions,
                "range-key": "label",
                value: $data.form.timeSlotIndex,
                onChange: _cache[7] || (_cache[7] = (...args) => $options.onTimeSlotChange && $options.onTimeSlotChange(...args))
              }, [
                vue.createElementVNode("view", { class: "picker-input" }, [
                  vue.createElementVNode(
                    "text",
                    null,
                    vue.toDisplayString(((_a = $options.timeSlotOptions[$data.form.timeSlotIndex]) == null ? void 0 : _a.label) || "选择时间段"),
                    1
                    /* TEXT */
                  ),
                  vue.createElementVNode("text", { class: "arrow" }, "▼")
                ])
              ], 40, ["range", "value"])
            ]),
            vue.createElementVNode("view", { class: "form-item" }, [
              vue.createElementVNode("text", { class: "label" }, "教室 *"),
              vue.createElementVNode("picker", {
                mode: "selector",
                range: $options.classroomOptions,
                "range-key": "label",
                value: $data.form.classroomIndex || 0,
                onChange: _cache[8] || (_cache[8] = (...args) => $options.onClassroomChange && $options.onClassroomChange(...args)),
                disabled: $options.classroomOptions.length === 0
              }, [
                vue.createElementVNode(
                  "view",
                  {
                    class: vue.normalizeClass(["picker-input", { disabled: $options.classroomOptions.length === 0 }])
                  },
                  [
                    vue.createElementVNode(
                      "text",
                      null,
                      vue.toDisplayString($options.classroomOptions.length === 0 ? "加载教室中..." : $options.getSelectedClassroomLabel()),
                      1
                      /* TEXT */
                    ),
                    vue.createElementVNode("text", { class: "arrow" }, "▼")
                  ],
                  2
                  /* CLASS */
                )
              ], 40, ["range", "value", "disabled"])
            ])
          ]),
          vue.createElementVNode("view", { class: "dialog-footer" }, [
            vue.createElementVNode("button", {
              class: "cancel-btn",
              onClick: _cache[9] || (_cache[9] = (...args) => $options.closeDialog && $options.closeDialog(...args))
            }, "取消"),
            vue.createElementVNode("button", {
              class: "confirm-btn",
              onClick: _cache[10] || (_cache[10] = (...args) => $options.submitForm && $options.submitForm(...args)),
              disabled: $data.submitting
            }, vue.toDisplayString($data.submitting ? "提交中..." : "确定"), 9, ["disabled"])
          ])
        ])
      ])) : vue.createCommentVNode("v-if", true),
      vue.createCommentVNode(" 加载状态 "),
      $data.loading ? (vue.openBlock(), vue.createElementBlock("view", {
        key: 3,
        class: "loading-overlay"
      }, [
        vue.createElementVNode("text", { class: "loading-text" }, "加载中...")
      ])) : vue.createCommentVNode("v-if", true)
    ]);
  }
  const PagesAdminManualSchedule = /* @__PURE__ */ _export_sfc(_sfc_main$5, [["render", _sfc_render$5], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/admin/manual-schedule.vue"]]);
  const _sfc_main$4 = {
    data() {
      return {
        config: {
          semester: "",
          startDate: ""
        },
        scheduleResult: null
      };
    },
    methods: {
      startAutoSchedule() {
        formatAppLog("log", "at pages/admin/auto-schedule.vue:42", "开始自动排课", this.config);
        this.scheduleResult = { totalCourses: 10 };
      }
    }
  };
  function _sfc_render$4(_ctx, _cache, $props, $setup, $data, $options) {
    const _component_u_input = vue.resolveComponent("u-input");
    const _component_u_button = vue.resolveComponent("u-button");
    return vue.openBlock(), vue.createElementBlock("view", { class: "auto-schedule" }, [
      vue.createElementVNode("text", { class: "title" }, "自动排课"),
      vue.createElementVNode("view", { class: "config-section" }, [
        vue.createElementVNode("text", { class: "section-title" }, "排课配置"),
        vue.createElementVNode("view", { class: "config-item" }, [
          vue.createElementVNode("text", null, "学期："),
          vue.createVNode(_component_u_input, {
            modelValue: $data.config.semester,
            "onUpdate:modelValue": _cache[0] || (_cache[0] = ($event) => $data.config.semester = $event),
            placeholder: "请输入学期"
          }, null, 8, ["modelValue"])
        ]),
        vue.createElementVNode("view", { class: "config-item" }, [
          vue.createElementVNode("text", null, "开始日期："),
          vue.createVNode(_component_u_input, {
            modelValue: $data.config.startDate,
            "onUpdate:modelValue": _cache[1] || (_cache[1] = ($event) => $data.config.startDate = $event),
            placeholder: "请选择开始日期"
          }, null, 8, ["modelValue"])
        ])
      ]),
      vue.createElementVNode("view", { class: "action-section" }, [
        vue.createVNode(_component_u_button, {
          type: "primary",
          onClick: $options.startAutoSchedule
        }, {
          default: vue.withCtx(() => [
            vue.createTextVNode("开始自动排课")
          ]),
          _: 1
          /* STABLE */
        }, 8, ["onClick"])
      ]),
      $data.scheduleResult ? (vue.openBlock(), vue.createElementBlock("view", {
        key: 0,
        class: "result-section"
      }, [
        vue.createElementVNode("text", { class: "section-title" }, "排课结果"),
        vue.createElementVNode(
          "text",
          null,
          "排课成功！共排课 " + vue.toDisplayString($data.scheduleResult.totalCourses) + " 门课程",
          1
          /* TEXT */
        )
      ])) : vue.createCommentVNode("v-if", true)
    ]);
  }
  const PagesAdminAutoSchedule = /* @__PURE__ */ _export_sfc(_sfc_main$4, [["render", _sfc_render$4], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/admin/auto-schedule.vue"]]);
  const _sfc_main$3 = {
    data() {
      return {
        loading: false,
        // 用户统计
        studentCount: 0,
        teacherCount: 0,
        adminCount: 0,
        totalUserCount: 0,
        // 课程统计
        courseCount: 0,
        teachingClassCount: 0,
        classroomCount: 0
      };
    },
    computed: {
      stats() {
        return [
          { key: "users", label: "用户总数", value: this.totalUserCount },
          { key: "students", label: "学生总数", value: this.studentCount },
          { key: "teachers", label: "教师总数", value: this.teacherCount },
          { key: "courses", label: "课程总数", value: this.courseCount },
          { key: "classes", label: "教学班总数", value: this.teachingClassCount },
          { key: "classrooms", label: "教室总数", value: this.classroomCount }
        ];
      }
    },
    onLoad() {
      this.fetchStats();
    },
    methods: {
      // 获取所有统计数据
      async fetchStats() {
        this.loading = true;
        try {
          await Promise.all([
            this.fetchUserStats(),
            this.fetchCourseStats(),
            this.fetchTeachingClassStats(),
            this.fetchClassroomStats()
          ]);
          uni.showToast({
            title: "统计完成",
            icon: "success"
          });
        } catch (error) {
          formatAppLog("error", "at pages/admin/stats.vue:110", "获取统计数据失败:", error);
          uni.showToast({
            title: "统计失败",
            icon: "error"
          });
        } finally {
          this.loading = false;
        }
      },
      // 统计用户数据
      async fetchUserStats() {
        try {
          let allUsers = [];
          let page = 0;
          let hasMore = true;
          while (hasMore) {
            const response = await adminApi.getUsers({ page, size: 100 });
            allUsers = allUsers.concat(response.content);
            page++;
            hasMore = response.content.length === 100 && page < response.totalPages;
          }
          this.studentCount = allUsers.filter((user) => user.role === "student").length;
          this.teacherCount = allUsers.filter((user) => user.role === "teacher").length;
          this.adminCount = allUsers.filter((user) => user.role === "admin").length;
          this.totalUserCount = allUsers.length;
        } catch (error) {
          formatAppLog("error", "at pages/admin/stats.vue:142", "获取用户统计失败:", error);
          try {
            const response = await adminApi.getUsers({ page: 0, size: 100 });
            this.totalUserCount = response.totalElements || response.content.length;
            this.studentCount = response.content.filter((user) => user.role === "student").length;
            this.teacherCount = response.content.filter((user) => user.role === "teacher").length;
            this.adminCount = response.content.filter((user) => user.role === "admin").length;
          } catch (fallbackError) {
            formatAppLog("error", "at pages/admin/stats.vue:151", "获取用户统计完全失败:", fallbackError);
          }
        }
      },
      // 统计课程数据
      async fetchCourseStats() {
        try {
          const courses = await adminApi.getCourses();
          this.courseCount = courses.length;
        } catch (error) {
          formatAppLog("error", "at pages/admin/stats.vue:162", "获取课程统计失败:", error);
          this.courseCount = 0;
        }
      },
      // 统计教学班数据
      async fetchTeachingClassStats() {
        try {
          const teachingClasses = await adminApi.getTeachingClasses();
          this.teachingClassCount = teachingClasses.length;
        } catch (error) {
          formatAppLog("error", "at pages/admin/stats.vue:173", "获取教学班统计失败:", error);
          this.teachingClassCount = 0;
        }
      },
      // 统计教室数据
      async fetchClassroomStats() {
        try {
          const classrooms = await adminApi.getClassrooms();
          this.classroomCount = classrooms.length;
        } catch (error) {
          formatAppLog("error", "at pages/admin/stats.vue:184", "获取教室统计失败:", error);
          this.classroomCount = 0;
        }
      },
      // 刷新统计数据
      refreshStats() {
        this.fetchStats();
      }
    }
  };
  function _sfc_render$3(_ctx, _cache, $props, $setup, $data, $options) {
    return vue.openBlock(), vue.createElementBlock("view", { class: "admin-stats" }, [
      vue.createElementVNode("text", { class: "title" }, "数据统计"),
      vue.createCommentVNode(" 加载状态 "),
      $data.loading ? (vue.openBlock(), vue.createElementBlock("view", {
        key: 0,
        class: "loading"
      }, [
        vue.createElementVNode("text", { class: "loading-text" }, "正在统计数据...")
      ])) : (vue.openBlock(), vue.createElementBlock(
        vue.Fragment,
        { key: 1 },
        [
          vue.createCommentVNode(" 统计数据网格 "),
          vue.createElementVNode("view", { class: "stats-grid" }, [
            (vue.openBlock(true), vue.createElementBlock(
              vue.Fragment,
              null,
              vue.renderList($options.stats, (stat) => {
                return vue.openBlock(), vue.createElementBlock("view", {
                  key: stat.key,
                  class: "stat-item"
                }, [
                  vue.createElementVNode(
                    "text",
                    { class: "stat-value" },
                    vue.toDisplayString(stat.value),
                    1
                    /* TEXT */
                  ),
                  vue.createElementVNode(
                    "text",
                    { class: "stat-label" },
                    vue.toDisplayString(stat.label),
                    1
                    /* TEXT */
                  )
                ]);
              }),
              128
              /* KEYED_FRAGMENT */
            ))
          ])
        ],
        2112
        /* STABLE_FRAGMENT, DEV_ROOT_FRAGMENT */
      )),
      vue.createCommentVNode(" 刷新按钮 "),
      vue.createElementVNode("view", { class: "refresh-section" }, [
        vue.createElementVNode("button", {
          class: "refresh-btn",
          onClick: _cache[0] || (_cache[0] = (...args) => $options.refreshStats && $options.refreshStats(...args)),
          disabled: $data.loading
        }, vue.toDisplayString($data.loading ? "统计中..." : "刷新统计"), 9, ["disabled"])
      ]),
      vue.createCommentVNode(" 详细信息 "),
      !$data.loading ? (vue.openBlock(), vue.createElementBlock("view", {
        key: 2,
        class: "detail-section"
      }, [
        vue.createElementVNode("text", { class: "section-title" }, "详细统计"),
        vue.createElementVNode("view", { class: "detail-item" }, [
          vue.createElementVNode("text", { class: "detail-label" }, "学生用户："),
          vue.createElementVNode(
            "text",
            { class: "detail-value" },
            vue.toDisplayString($data.studentCount) + " 人",
            1
            /* TEXT */
          )
        ]),
        vue.createElementVNode("view", { class: "detail-item" }, [
          vue.createElementVNode("text", { class: "detail-label" }, "教师用户："),
          vue.createElementVNode(
            "text",
            { class: "detail-value" },
            vue.toDisplayString($data.teacherCount) + " 人",
            1
            /* TEXT */
          )
        ]),
        vue.createElementVNode("view", { class: "detail-item" }, [
          vue.createElementVNode("text", { class: "detail-label" }, "管理员用户："),
          vue.createElementVNode(
            "text",
            { class: "detail-value" },
            vue.toDisplayString($data.adminCount) + " 人",
            1
            /* TEXT */
          )
        ]),
        vue.createElementVNode("view", { class: "detail-item" }, [
          vue.createElementVNode("text", { class: "detail-label" }, "课程总数："),
          vue.createElementVNode(
            "text",
            { class: "detail-value" },
            vue.toDisplayString($data.courseCount) + " 门",
            1
            /* TEXT */
          )
        ]),
        vue.createElementVNode("view", { class: "detail-item" }, [
          vue.createElementVNode("text", { class: "detail-label" }, "教学班总数："),
          vue.createElementVNode(
            "text",
            { class: "detail-value" },
            vue.toDisplayString($data.teachingClassCount) + " 个",
            1
            /* TEXT */
          )
        ]),
        vue.createElementVNode("view", { class: "detail-item" }, [
          vue.createElementVNode("text", { class: "detail-label" }, "教室总数："),
          vue.createElementVNode(
            "text",
            { class: "detail-value" },
            vue.toDisplayString($data.classroomCount) + " 间",
            1
            /* TEXT */
          )
        ])
      ])) : vue.createCommentVNode("v-if", true)
    ]);
  }
  const PagesAdminStats = /* @__PURE__ */ _export_sfc(_sfc_main$3, [["render", _sfc_render$3], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/admin/stats.vue"]]);
  const _sfc_main$2 = {
    __name: "index",
    setup(__props, { expose: __expose }) {
      __expose();
      const userInfo = vue.computed(() => {
        const user = authApi.getCurrentUser();
        return user || { realName: "用户", role: "student" };
      });
      const roleText = vue.computed(() => {
        const roleMap = {
          student: "学生",
          teacher: "教师",
          admin: "管理员"
        };
        return roleMap[userInfo.value.role] || "用户";
      });
      const roleIdLabel = vue.computed(() => {
        const labelMap = {
          student: "学号",
          teacher: "教师编号"
        };
        return labelMap[userInfo.value.role] || "ID";
      });
      const avatarColor = vue.computed(() => {
        var _a;
        const colors = ["#FF7875", "#FFC069", "#95DE64", "#597EF7", "#AD6800"];
        const charCodeSum = ((_a = userInfo.value.realName) == null ? void 0 : _a.charCodeAt(0)) || 0;
        return colors[charCodeSum % colors.length];
      });
      const editProfile = () => {
        uni.showToast({
          title: "功能开发中",
          icon: "none"
        });
      };
      const goHome = () => {
        uni.reLaunch({ url: "/pages/dashboard/index" });
      };
      const goToSchedule = () => {
        uni.navigateTo({ url: "/pages/schedule/index" });
      };
      const goToMyCourses = () => {
        uni.navigateTo({ url: "/pages/student/my-courses" });
      };
      const goToTeacherCourses = () => {
        uni.navigateTo({ url: "/pages/teacher/courses" });
      };
      const goToUserManagement = () => {
        uni.navigateTo({ url: "/pages/admin/users" });
      };
      const showAbout = () => {
        uni.showModal({
          title: "关于系统",
          content: "Course Scheduler v1.0\n智能课程调度管理系统\n\n© 2024 Course Schedule Team",
          showCancel: false,
          confirmText: "确定"
        });
      };
      const clearCache = () => {
        uni.showModal({
          title: "清除缓存",
          content: "确定要清除所有缓存数据吗？",
          success: (res) => {
            if (res.confirm) {
              try {
                uni.clearStorageSync();
                uni.showToast({
                  title: "缓存已清除",
                  icon: "success"
                });
                setTimeout(() => {
                  uni.reLaunch({ url: "/pages/auth/login" });
                }, 1500);
              } catch (error) {
                uni.showToast({
                  title: "清除失败",
                  icon: "error"
                });
              }
            }
          }
        });
      };
      const logout = () => {
        uni.showModal({
          title: "退出登录",
          content: "确定要退出登录吗？",
          success: (res) => {
            if (res.confirm) {
              authApi.logout();
              uni.showToast({
                title: "已退出登录",
                icon: "success"
              });
              setTimeout(() => {
                uni.reLaunch({ url: "/pages/auth/login" });
              }, 1e3);
            }
          }
        });
      };
      vue.onMounted(() => {
        if (!authApi.isAuthenticated()) {
          uni.reLaunch({ url: "/pages/auth/login" });
          return;
        }
      });
      const __returned__ = { userInfo, roleText, roleIdLabel, avatarColor, editProfile, goHome, goToSchedule, goToMyCourses, goToTeacherCourses, goToUserManagement, showAbout, clearCache, logout, ref: vue.ref, computed: vue.computed, onMounted: vue.onMounted, get authApi() {
        return authApi;
      } };
      Object.defineProperty(__returned__, "__isScriptSetup", { enumerable: false, value: true });
      return __returned__;
    }
  };
  function _sfc_render$2(_ctx, _cache, $props, $setup, $data, $options) {
    var _a;
    return vue.openBlock(), vue.createElementBlock("view", { class: "profile-page" }, [
      vue.createElementVNode("view", { class: "profile-header" }, [
        vue.createElementVNode(
          "view",
          {
            class: "user-avatar",
            style: vue.normalizeStyle({ backgroundColor: $setup.avatarColor })
          },
          vue.toDisplayString((_a = $setup.userInfo.realName) == null ? void 0 : _a.charAt(0)),
          5
          /* TEXT, STYLE */
        ),
        vue.createElementVNode("view", { class: "user-info" }, [
          vue.createElementVNode(
            "text",
            { class: "user-name" },
            vue.toDisplayString($setup.userInfo.realName),
            1
            /* TEXT */
          ),
          vue.createElementVNode(
            "text",
            { class: "user-role" },
            vue.toDisplayString($setup.roleText),
            1
            /* TEXT */
          ),
          vue.createElementVNode(
            "text",
            { class: "user-id" },
            "ID: " + vue.toDisplayString($setup.userInfo.id),
            1
            /* TEXT */
          )
        ])
      ]),
      vue.createElementVNode("view", { class: "profile-content" }, [
        vue.createCommentVNode(" 个人信息卡片 "),
        vue.createElementVNode("view", { class: "info-card" }, [
          vue.createElementVNode("view", { class: "card-header" }, [
            vue.createElementVNode("text", { class: "card-title" }, "个人信息"),
            vue.createElementVNode("view", {
              class: "edit-btn",
              onClick: $setup.editProfile
            }, [
              vue.createElementVNode("text", null, "编辑")
            ])
          ]),
          vue.createElementVNode("view", { class: "info-list" }, [
            vue.createElementVNode("view", { class: "info-item" }, [
              vue.createElementVNode("text", { class: "info-label" }, "用户名"),
              vue.createElementVNode(
                "text",
                { class: "info-value" },
                vue.toDisplayString($setup.userInfo.username),
                1
                /* TEXT */
              )
            ]),
            vue.createElementVNode("view", { class: "info-item" }, [
              vue.createElementVNode("text", { class: "info-label" }, "真实姓名"),
              vue.createElementVNode(
                "text",
                { class: "info-value" },
                vue.toDisplayString($setup.userInfo.realName),
                1
                /* TEXT */
              )
            ]),
            vue.createElementVNode("view", { class: "info-item" }, [
              vue.createElementVNode("text", { class: "info-label" }, "角色"),
              vue.createElementVNode(
                "text",
                { class: "info-value" },
                vue.toDisplayString($setup.roleText),
                1
                /* TEXT */
              )
            ]),
            $setup.userInfo.roleId ? (vue.openBlock(), vue.createElementBlock("view", {
              key: 0,
              class: "info-item"
            }, [
              vue.createElementVNode(
                "text",
                { class: "info-label" },
                vue.toDisplayString($setup.roleIdLabel),
                1
                /* TEXT */
              ),
              vue.createElementVNode(
                "text",
                { class: "info-value" },
                vue.toDisplayString($setup.userInfo.roleId),
                1
                /* TEXT */
              )
            ])) : vue.createCommentVNode("v-if", true)
          ])
        ]),
        vue.createCommentVNode(" 功能菜单 "),
        vue.createElementVNode("view", { class: "menu-card" }, [
          vue.createElementVNode("view", { class: "menu-header" }, [
            vue.createElementVNode("text", { class: "card-title" }, "功能菜单")
          ]),
          vue.createElementVNode("view", { class: "menu-list" }, [
            vue.createElementVNode("view", {
              class: "menu-item",
              onClick: $setup.goToSchedule
            }, [
              vue.createElementVNode("view", { class: "menu-icon" }, "📅"),
              vue.createElementVNode("text", { class: "menu-text" }, "课表查看"),
              vue.createElementVNode("text", { class: "menu-arrow" }, "→")
            ]),
            $setup.userInfo.role === "student" ? (vue.openBlock(), vue.createElementBlock("view", {
              key: 0,
              class: "menu-item",
              onClick: $setup.goToMyCourses
            }, [
              vue.createElementVNode("view", { class: "menu-icon" }, "📚"),
              vue.createElementVNode("text", { class: "menu-text" }, "我的课程"),
              vue.createElementVNode("text", { class: "menu-arrow" }, "→")
            ])) : vue.createCommentVNode("v-if", true),
            $setup.userInfo.role === "teacher" ? (vue.openBlock(), vue.createElementBlock("view", {
              key: 1,
              class: "menu-item",
              onClick: $setup.goToTeacherCourses
            }, [
              vue.createElementVNode("view", { class: "menu-icon" }, "📖"),
              vue.createElementVNode("text", { class: "menu-text" }, "课程管理"),
              vue.createElementVNode("text", { class: "menu-arrow" }, "→")
            ])) : vue.createCommentVNode("v-if", true),
            $setup.userInfo.role === "admin" ? (vue.openBlock(), vue.createElementBlock("view", {
              key: 2,
              class: "menu-item",
              onClick: $setup.goToUserManagement
            }, [
              vue.createElementVNode("view", { class: "menu-icon" }, "👥"),
              vue.createElementVNode("text", { class: "menu-text" }, "用户管理"),
              vue.createElementVNode("text", { class: "menu-arrow" }, "→")
            ])) : vue.createCommentVNode("v-if", true),
            vue.createElementVNode("view", {
              class: "menu-item",
              onClick: $setup.showAbout
            }, [
              vue.createElementVNode("view", { class: "menu-icon" }, "ℹ️"),
              vue.createElementVNode("text", { class: "menu-text" }, "关于系统"),
              vue.createElementVNode("text", { class: "menu-arrow" }, "→")
            ])
          ])
        ]),
        vue.createCommentVNode(" 系统设置 "),
        vue.createElementVNode("view", { class: "settings-card" }, [
          vue.createElementVNode("view", { class: "card-header" }, [
            vue.createElementVNode("text", { class: "card-title" }, "系统设置")
          ]),
          vue.createElementVNode("view", { class: "settings-list" }, [
            vue.createElementVNode("view", {
              class: "setting-item",
              onClick: $setup.clearCache
            }, [
              vue.createElementVNode("view", { class: "setting-icon" }, "🗑️"),
              vue.createElementVNode("text", { class: "setting-text" }, "清除缓存")
            ]),
            vue.createElementVNode("view", {
              class: "setting-item danger",
              onClick: $setup.logout
            }, [
              vue.createElementVNode("view", { class: "setting-icon" }, "🚪"),
              vue.createElementVNode("text", { class: "setting-text" }, "退出登录")
            ])
          ])
        ])
      ]),
      vue.createCommentVNode(" 底部导航 "),
      vue.createElementVNode("view", { class: "bottom-nav" }, [
        vue.createElementVNode("view", {
          class: "nav-item",
          onClick: $setup.goHome
        }, [
          vue.createElementVNode("text", { class: "nav-icon" }, "🏠"),
          vue.createElementVNode("text", { class: "nav-text" }, "首页")
        ]),
        $setup.userInfo.role !== "admin" ? (vue.openBlock(), vue.createElementBlock("view", {
          key: 0,
          class: "nav-item",
          onClick: $setup.goToSchedule
        }, [
          vue.createElementVNode("text", { class: "nav-icon" }, "📅"),
          vue.createElementVNode("text", { class: "nav-text" }, "课表")
        ])) : vue.createCommentVNode("v-if", true),
        vue.createElementVNode("view", { class: "nav-item active" }, [
          vue.createElementVNode("text", { class: "nav-icon" }, "👤"),
          vue.createElementVNode("text", { class: "nav-text" }, "我的")
        ])
      ])
    ]);
  }
  const PagesProfileIndex = /* @__PURE__ */ _export_sfc(_sfc_main$2, [["render", _sfc_render$2], ["__scopeId", "data-v-f97f9319"], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/profile/index.vue"]]);
  const _sfc_main$1 = {
    data() {
      return {
        testValue: "",
        clicked: false
      };
    },
    methods: {
      handleClick() {
        this.clicked = true;
        formatAppLog("log", "at pages/test/uview.vue:23", "按钮被点击了");
      }
    }
  };
  function _sfc_render$1(_ctx, _cache, $props, $setup, $data, $options) {
    return vue.openBlock(), vue.createElementBlock("view", { class: "test-container" }, [
      vue.createElementVNode("view", { class: "basic-test" }, [
        vue.createElementVNode("text", { class: "title" }, "基础页面测试"),
        vue.createElementVNode("text", { class: "description" }, "如果你能看到这段文字，说明页面可以正常渲染"),
        vue.createElementVNode("button", {
          class: "test-button",
          onClick: _cache[0] || (_cache[0] = (...args) => $options.handleClick && $options.handleClick(...args))
        }, "点击测试"),
        $data.clicked ? (vue.openBlock(), vue.createElementBlock("text", { key: 0 }, "按钮点击成功！")) : vue.createCommentVNode("v-if", true)
      ])
    ]);
  }
  const PagesTestUview = /* @__PURE__ */ _export_sfc(_sfc_main$1, [["render", _sfc_render$1], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/pages/test/uview.vue"]]);
  __definePage("pages/auth/login", PagesAuthLogin);
  __definePage("pages/index/index", PagesIndexIndex);
  __definePage("pages/dashboard/index", PagesDashboardIndex);
  __definePage("pages/test/api", PagesTestApi);
  __definePage("pages/test/simple", PagesTestSimple);
  __definePage("pages/student/select-course", PagesStudentSelectCourse);
  __definePage("pages/student/my-courses", PagesStudentMyCourses);
  __definePage("pages/student/schedule", PagesStudentSchedule);
  __definePage("pages/teacher/courses", PagesTeacherCourses);
  __definePage("pages/teacher/teaching-classes", PagesTeacherTeachingClasses);
  __definePage("pages/admin/users", PagesAdminUsers);
  __definePage("pages/admin/courses", PagesAdminCourses);
  __definePage("pages/admin/manual-schedule", PagesAdminManualSchedule);
  __definePage("pages/admin/auto-schedule", PagesAdminAutoSchedule);
  __definePage("pages/admin/stats", PagesAdminStats);
  __definePage("pages/profile/index", PagesProfileIndex);
  __definePage("pages/test/uview", PagesTestUview);
  const _sfc_main = /* @__PURE__ */ vue.defineComponent({
    __name: "App",
    setup(__props, { expose: __expose }) {
      __expose();
      onLaunch(() => {
        formatAppLog("log", "at App.vue:4", "App Launch");
      });
      onShow(() => {
        formatAppLog("log", "at App.vue:7", "App Show");
      });
      onHide(() => {
        formatAppLog("log", "at App.vue:10", "App Hide");
      });
      const __returned__ = {};
      Object.defineProperty(__returned__, "__isScriptSetup", { enumerable: false, value: true });
      return __returned__;
    }
  });
  function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
    return vue.createCommentVNode(" App.vue是应用的主组件，所有页面都是在App.vue下进行切换的 ");
  }
  const App = /* @__PURE__ */ _export_sfc(_sfc_main, [["render", _sfc_render], ["__file", "D:/ai-course-apply-feature-branch2/vue01/course-schedule-uniapp/src/App.vue"]]);
  function createApp() {
    const app = vue.createVueApp(App);
    return {
      app
    };
  }
  const { app: __app__, Vuex: __Vuex__, Pinia: __Pinia__ } = createApp();
  uni.Vuex = __Vuex__;
  uni.Pinia = __Pinia__;
  __app__.provide("__globalStyles", __uniConfig.styles);
  __app__._component.mpType = "app";
  __app__._component.render = () => {
  };
  __app__.mount("#app");
})(Vue);
