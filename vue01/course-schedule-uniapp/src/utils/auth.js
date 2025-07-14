/**
 * 认证工具类 - 模仿admin认证方式
 * 使用uni.setStorageSync/uni.getStorageSync管理认证信息
 */

// 认证信息键名
const AUTH_KEY = 'STUDENT_AUTH_INFO';
const TOKEN_KEY = 'STUDENT_TOKEN';

/**
 * 获取认证信息
 * @returns {Object|null} 认证信息对象
 */
export function getAuthInfo() {
  try {
    const authStr = uni.getStorageSync(AUTH_KEY);
    if (authStr) {
      return JSON.parse(authStr);
    }
    return null;
  } catch (error) {
    console.error('获取认证信息失败:', error);
    return null;
  }
}

/**
 * 保存认证信息
 * @param {Object} authInfo 认证信息
 */
export function setAuthInfo(authInfo) {
  try {
    uni.setStorageSync(AUTH_KEY, JSON.stringify(authInfo));
  } catch (error) {
    console.error('保存认证信息失败:', error);
  }
}

/**
 * 保存token
 * @param {string} token JWT token
 */
export function setToken(token) {
  try {
    uni.setStorageSync(TOKEN_KEY, token);
  } catch (error) {
    console.error('保存token失败:', error);
  }
}

/**
 * 获取token
 * @returns {string|null} token字符串
 */
export function getToken() {
  try {
    return uni.getStorageSync(TOKEN_KEY) || null;
  } catch (error) {
    console.error('获取token失败:', error);
    return null;
  }
}

/**
 * 清除认证信息
 */
export function clearAuth() {
  try {
    uni.removeStorageSync(AUTH_KEY);
    uni.removeStorageSync(TOKEN_KEY);
  } catch (error) {
    console.error('清除认证信息失败:', error);
  }
}

/**
 * 检查是否已登录
 * @returns {boolean} 是否已登录
 */
export function isLoggedIn() {
  const authInfo = getAuthInfo();
  return authInfo && authInfo.userId && authInfo.token;
}

/**
 * 获取请求头中的认证信息
 * @returns {Object} 请求头对象
 */
export function getAuthHeader() {
  const token = getToken();
  return token ? { 'Authorization': `Bearer ${token}` } : {};
}

/**
 * 更新用户信息
 * @param {Object} userInfo 用户信息
 */
export function updateUserInfo(userInfo) {
  const authInfo = getAuthInfo() || {};
  Object.assign(authInfo, userInfo);
  setAuthInfo(authInfo);
}

/**
 * 获取当前用户ID
 * @returns {number|null} 用户ID
 */
export function getCurrentUserId() {
  const authInfo = getAuthInfo();
  return authInfo ? authInfo.userId : null;
}

/**
 * 获取当前用户角色
 * @returns {string|null} 用户角色
 */
export function getCurrentUserRole() {
  const authInfo = getAuthInfo();
  return authInfo ? authInfo.role : null;
}

/**
 * 检查是否有特定角色
 * @param {string} role 角色名称
 * @returns {boolean} 是否有该角色
 */
export function hasRole(role) {
  const currentRole = getCurrentUserRole();
  return currentRole === role;
}

/**
 * 获取用户姓名
 * @returns {string|null} 用户姓名
 */
export function getUserName() {
  const authInfo = getAuthInfo();
  return authInfo ? authInfo.name : null;
}

/**
 * 验证token是否有效
 * @returns {Promise<boolean>} token是否有效
 */
export async function validateToken() {
  const token = getToken();
  if (!token) return false;
  
  // 这里可以添加实际的token验证逻辑
  // 例如：调用后端验证接口
  return true;
}